package dev.alpas.http.jetty

import dev.alpas.*
import dev.alpas.exceptions.MethodNotAllowedException
import dev.alpas.exceptions.NotFoundHttpException
import dev.alpas.http.*
import dev.alpas.routing.BaseRouteLoader
import dev.alpas.routing.Route
import dev.alpas.routing.RouteMatchStatus
import dev.alpas.routing.Router
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class AlpasServlet(
    private val app: Application,
    private val routeEntryMiddlewareGroups: Map<String, Iterable<KClass<out Middleware<HttpCall>>>>,
    private val serverEntryMiddleware: Iterable<KClass<out Middleware<HttpCall>>>
) : HttpServlet() {
    private val staticHandler by lazy { StaticAssetHandler(app) }
    private val corsHandler by lazy { CorsHandler(app) }

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        if (corsHandler.handle(req, resp)) return
        if (staticHandler.handle(req, resp)) return

        val router = app.make<Router>()
        val container = ChildContainer(app)

        if (app.env.isDev) {
            app.tryMake<BaseRouteLoader>()?.let {
                app.logger.warn { "Reloading the router. This has a big performance hit!" }
                it.load(router)
                router.forceCompile(PackageClassLoader(app.srcPackage))
            }
        }

        val appConfig = app.config<AppConfig>()
        val allowMethodSpoofing = appConfig.allowMethodSpoofing
        val route = router.routeFor(methodName = req.method(allowMethodSpoofing), uri = req.requestURI)
        val callHooks = app.callHooks.map {
            it.createInstance()
        }

        val call = HttpCall(container, req, resp, route, callHooks).also {
            if (appConfig.combineJsonBodyWithParams) {
                it.combineJsonBodyWithParams()
            }
            if (app.env.inTestMode) {
                app.recordLastCall(it)
            }
        }

        try {
            call.logger.debug { "Registering ${callHooks.size} HttpCall hooks" }
            callHooks.forEach { it.register(call) }

            call.logger.debug { "Booting ${callHooks.size} HttpCall hooks" }
            callHooks.forEach { it.boot(call) }

            call.sendCallThroughServerEntryMiddleware(app).then { matchRoute(app, it, callHooks) }

            call.logger.debug { "Clean closing ${callHooks.size} HttpCall hooks" }
            callHooks.forEach { it.beforeClose(call, true) }
            call.close()
        } catch (e: Exception) {
            call.logger.debug { "Unclean closing ${callHooks.size} HttpCall hooks" }
            callHooks.forEach { it.beforeClose(call, false) }
            call.drop(e)
        }
    }

    private fun matchRoute(app: Application, call: HttpCall, callHooks: List<HttpCallHook>) {
        when (call.route.status()) {
            RouteMatchStatus.SUCCESS -> call.dispatchToRouteHandler(app, call.route.target(), callHooks)
            RouteMatchStatus.METHOD_NOT_ALLOWED -> {
                throw MethodNotAllowedException(
                    "Method ${call.method} is not allowed for this operation. Only ${call.route.allowedMethods()
                        .joinToString(
                            ", "
                        ).toUpperCase()} methods are allowed."
                )
            }
            else -> throw NotFoundHttpException()
        }
    }

    private fun HttpCall.dispatchToRouteHandler(app: Application, route: Route, callHooks: List<HttpCallHook>) {
        val groupMiddleware = mutableListOf<Middleware<HttpCall>>()
        route.middlewareGroups.forEach {
            groupMiddleware.addAll(makeMiddleware(app, routeEntryMiddlewareGroups[it]))
        }
        val middleware = groupMiddleware.plus(makeMiddleware(app, route.middleware))
        Pipeline<HttpCall>().send(this).through(middleware).then { call ->
            logger.debug { "Calling before route handle hook for ${callHooks.size} hooks" }
            callHooks.forEach { it -> it.beforeRouteHandle(call, route) }
            route.handle(call)
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun makeMiddleware(
        app: Application,
        middleware: Iterable<KClass<out Middleware<HttpCall>>>?
    ): Iterable<Middleware<HttpCall>> {
        return middleware?.filter { app.shouldLoadMiddleware(it) }?.map { it.createInstance() } ?: emptyList()
    }

    private fun HttpCall.sendCallThroughServerEntryMiddleware(app: Application): Pipeline<HttpCall> {
        return Pipeline<HttpCall>().send(this).through(makeMiddleware(app, serverEntryMiddleware))
    }

    private fun HttpServletRequest.method(allowMethodSpoofing: Boolean): String {
        // If method is not a POST method, no need to check it further. We just return the method as it is.
        // If method spoofing is not allowed we return the method name as it is.
        if (method != Method.POST.name || !allowMethodSpoofing) {
            return method
        }
        return parameterMap["_method"]?.firstOrNull()?.toUpperCase() ?: method
    }
}
