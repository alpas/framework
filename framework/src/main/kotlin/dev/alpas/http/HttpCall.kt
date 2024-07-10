package dev.alpas.http

import dev.alpas.*
import dev.alpas.auth.AuthChannel
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.Authenticatable
import dev.alpas.auth.UserProvider
import dev.alpas.exceptions.*
import dev.alpas.routing.Route
import dev.alpas.routing.RouteResult
import dev.alpas.routing.UrlGenerator
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.Rule
import dev.alpas.validation.SharedDataBag
import dev.alpas.validation.ValidationGuard
import mu.KotlinLogging
import org.eclipse.jetty.http.HttpStatus
import java.net.URI
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.atomic.AtomicBoolean
import jakarta.servlet.AsyncContext
import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Suppress("unused")
class HttpCall internal constructor(
    private val container: Container,
    val servletRequest: HttpServletRequest,
    val servletResponse: HttpServletResponse,
    private val requestableCall: RequestableCall,
    internal val route: RouteResult,
    private val callHooks: List<HttpCallHook>
) : Container by container,
    RequestableCall by requestableCall,
    RequestParamsBagContract by RequestParamsBag(requestableCall, route) {

    internal constructor(
        container: Container,
        request: HttpServletRequest,
        response: HttpServletResponse,
        route: RouteResult,
        callHooks: List<HttpCallHook>
    ) : this(container, request, response, Requestable(request), route, callHooks)

    val logger by lazy { KotlinLogging.logger {} }
    val errorBag: ErrorBag by lazy { ErrorBag() }
    var isDropped = false
        private set

    internal val exceptionHandler by lazy { makeElse { ExceptionHandler() } }
    val authChannel: AuthChannel by lazy { config<AuthConfig>().channel(this, route.target().authChannel) }
    internal val userProvider: UserProvider? by lazy { authChannel.userProvider }
    val isAuthenticated by lazy { authChannel.isLoggedIn() }
    val isFromGuest by lazy { !isAuthenticated }
    val user: Authenticatable by lazy { authChannel.user.orAbort("User is not authenticated") }
    val env by lazy { make<Environment>() }
    private val redirector by lazy { Redirector(requestableCall, urlGenerator) }
    val urlGenerator: UrlGenerator by lazy { container.make<UrlGenerator>() }
    internal var validateUsingJsonBody: AtomicBoolean = AtomicBoolean(false)
        private set

    private var headers = mutableMapOf<String, String>()
    private var future: CompletableFuture<*>? = null
    lateinit var response: Response
        private set

    private val sharedData by lazy { SharedDataBag() }

    fun charset() = servletResponse.charset()

    fun isSigned(url: String? = null): Boolean {
        return urlGenerator.checkSignature(url ?: fullUrl)
    }

    fun route(): Route? {
        if (route.isSuccess) {
            return route.target()
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Authenticatable> caller(): T {
        return user as T
    }

    @Suppress("UNCHECKED_CAST")
    fun callerId(): Long {
        return caller<Authenticatable>().id
    }

    fun applyRules(
        attribute: String,
        failfast: Boolean = false,
        inJsonBody: Boolean = false,
        rules: ValidationGuard.() -> Unit
    ): HttpCall {
        ValidationGuard(failfast, inJsonBody).also {
            it.call = this
            it.rules()
            it.validate(attribute, errorBag)
            if (it.shouldFailFast) {
                checkValidationErrors { errorBag ->
                    it.handleError(errorBag)
                }
            }
        }
        return this
    }

    fun applyRules(
        rules: Map<String, Iterable<Rule>>,
        failfast: Boolean = false,
        inJsonBody: Boolean = false
    ): HttpCall {
        ValidationGuard(failfast, inJsonBody).also {
            it.call = this
            it.validate(rules, errorBag)
            checkValidationErrors { errorBag ->
                it.handleError(errorBag)
            }
        }
        return this
    }

    fun validate() {
        checkValidationErrors()
    }

    fun <T : ValidationGuard> validateUsing(validator: KClass<out T>, afterSuccessBlock: T.() -> Unit = {}): T {
        return this.validateUsing(validator.createInstance(), afterSuccessBlock)
    }

    fun <T : ValidationGuard> validateUsing(validator: T, afterSuccessBlock: T.() -> Unit = {}): T {
        return validator.also {
            it.call = this
            it.validate(errorBag)
            checkValidationErrors { errorBag ->
                it.handleError(errorBag)
            }
            if (errorBag.isEmpty()) {
                it.afterSuccessfulValidation()
                it.afterSuccessBlock()
            }
        }
    }

    private fun checkValidationErrors(onErrorHandler: (ErrorBag) -> Boolean = fun(_: ErrorBag) = false) {
        if (!errorBag.isEmpty()) {
            if (!onErrorHandler(errorBag)) {
                throw ValidationException(errorBag = errorBag)
            }
        }
    }

    fun routeNamed(name: String, params: Map<String, Any>? = null, absolute: Boolean = true): String {
        return urlGenerator.route(name, params, absolute)
    }

    fun timezone(): ZoneOffset {
        return make<AppConfig>().timezone
    }

    fun nowInCurrentTimezone(): ZonedDateTime {
        return ZonedDateTime.now(timezone())
    }

    fun redirect(
        to: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ) {
        redirect().to(to, status, headers)
    }

    fun redirect(): Redirectable {
        return redirector
    }

    fun isBeingRedirected(): Boolean {
        return redirect().isBeingRedirected()
    }

    fun drop(e: Exception) {
        isDropped = true
        handleException(e)
        close()
    }

    internal fun sessionIsValid() = env.supportsSession && !servletResponse.isCommitted && session.isValid()

    operator fun <T> invoke(block: HttpCall.() -> T): T {
        return this.block()
    }

    private fun onBeforeRender(context: RenderContext) {
        if (isDropped) {
            logger.debug { "Calling beforeErrorRender hook for ${callHooks.size} hooks" }
            callHooks.forEach { it.beforeErrorRender(context) }
        } else {
            logger.debug { "Calling beforeRender hook for ${callHooks.size} hooks" }
            callHooks.forEach { it.beforeRender(context) }
        }
    }

    internal fun onBeforeAuthCheck() {
        callHooks.forEach { it.beforeAuthCheck(this) }
    }

    internal fun afterAuthCheck(isAuthenticated: Boolean) {
        callHooks.forEach { it.afterAuthCheck(this, isAuthenticated) }
    }

    fun validateUsingJsonBody() {
        validateUsingJsonBody.set(true)
    }

    fun saveReferrerAsIntendedUrl(default: String = "/"): String {
        return (referrer ?: default).apply {
            session.saveIntendedUrl(this)
        }
    }

    fun url(
        path: String,
        params: Map<String, Any> = emptyMap(),
        forceSecure: Boolean = false
    ): String {
        return uri(path, params, forceSecure).toString()
    }

    fun uri(
        path: String,
        params: Map<String, Any> = emptyMap(),
        forceSecure: Boolean = false
    ): URI {
        return urlGenerator.url(path, params, forceSecure)
    }

    fun intendedUrl(): String? {
        return session.intendedUrl()
    }

    fun previousUrl(): String? {
        return session.previousUrl()
    }

    fun hold(future: CompletableFuture<*>): HttpCall {
        this.future = future
        return this
    }

    fun <T : Response> hold(suspendingFunction: suspend () -> T): HttpCall {
        val response = startInFuture {
            suspendingFunction()
        }
        return hold(response)
    }

    fun close() {
        if (future == null) syncClose() else asyncClose()
    }

    private fun handleException(e: Throwable) {
        val exceptionHandler = exceptionHandler
        // Check if the top layer is an HTTP exception type since most of the times the exception is thrown from
        // a controller, the controller will be wrapped in an InvocationTargetException object. Hence, we'd have to
        // check the cause of this exception to see whether the cause is an actual HTTP exception or not.
        if (HttpException::class.java.isAssignableFrom(e::class.java)) {
            exceptionHandler.handle(e as HttpException, this)
            return
        } else {
            e.cause?.let {
                return handleException(it)
            }
        }
        exceptionHandler.handle(e, this)
        return
    }

    private fun asyncClose() {
        val asyncContext = startAsync(config<AppConfig>().asyncTimeout)
        future!!.exceptionally { throwable ->
            future = null
            if (throwable is CompletionException && throwable.cause is Exception) {
                drop(throwable.cause as Exception)
            } else if (throwable is Exception) {
                drop(throwable)
            }
            asyncContext.complete()
            null
        }.thenAccept {
            future = null
            val response = when (it) {
                is ErrorResponse -> {
                    drop(it.exception)
                    null
                }
                is Response -> it
                else -> StringResponse(it.toString())
            }
            response?.let {
                reply(response)
                syncClose()
            }
            asyncContext.complete()
        }
    }

    private fun syncClose() {
        if (isEventStream) {
            jettyRequest.isHandled = true
            return
        }
        if (isBeingRedirected()) {
            sendResponseBack(redirector.redirectResponse)
        } else {
            if (!isEventStream && !::response.isInitialized) {
                throw InternalServerException("A response has not been set. Make sure to set a response in ${route.target().handler}.")
            }
            saveCookies()
            copyHeaders()
            sendResponseBack(response)
            clearFlash()
        }
        jettyRequest.isHandled = true
    }

    private fun clearFlash() {
        // By this time we have sent a response back to the client and now we need to clear the
        // previous flash messages to avoid showing these messages again in the next request.
        if (sessionIsValid()) {
            session.clearPreviousFlashBag()
        }
    }

    fun addHeaders(headers: Map<String, String>): HttpCall {
        this.headers.putAll(headers)
        return this
    }

    fun addHeader(key: String, value: String): HttpCall {
        this.headers[key] = value
        return this
    }

    private fun saveCookies() {
        cookie.outgoingCookies.forEach {
            servletResponse.addCookie(it)
        }
    }

    private fun sendResponseBack(response: Response) {
        val context = RenderContext(this, sharedData).also {
            onBeforeRender(it)
        }
        try {
            response.render(context)
        } catch (e: Exception) {
            response.renderException(e, context)
        }
    }

    private fun copyHeaders() {
        headers.forEach { (key, value) ->
            servletResponse.addHeader(key, value)
        }
    }

    fun share(pair: Pair<String, Any?>, vararg pairs: Pair<String, Any>) {
        sharedData.add(pair, *pairs)
    }

    fun shared(key: String): Any? {
        return sharedData[key]
    }

    fun status(code: Int): HttpCall {
        if (::response.isInitialized) {
            response.statusCode(code)
            return this
        } else {
            throw IllegalStateException("Status Code can't be set before the response. Make sure to set a response first.")
        }
    }

    fun contentType(type: String): HttpCall {
        if (::response.isInitialized) {
            response.contentType(type)
            return this
        } else {
            throw IllegalStateException("Content type can't be set before the response. Make sure to set a response first.")
        }
    }

    fun asHtml() = contentType(HTML_CONTENT_TYPE)
    fun asJson() = contentType(JSON_CONTENT_TYPE)

    fun abort(statusCode: Int, message: String? = null, headers: Map<String, String> = emptyMap()): Nothing {
        throw httpExceptionFor(statusCode, message, headers)
    }

    fun abortUnless(
        condition: Boolean,
        statusCode: Int,
        message: String? = null,
        headers: Map<String, String> = emptyMap()
    ) {
        if (!condition) {
            abort(statusCode, message, headers)
        }
    }

    fun abortIf(
        condition: Boolean,
        statusCode: Int,
        message: String? = null,
        headers: Map<String, String> = emptyMap()
    ) {
        if (condition) {
            abort(statusCode, message, headers)
        }
    }

    fun addHeader(header: Pair<String, String>, vararg headers: Pair<String, String>): HttpCall {
        return addHeaders(mapOf(header) + headers)
    }

    fun <T> reply(payload: T? = null, statusCode: Int = HttpStatus.OK_200): HttpCall {
        response = StringResponse(payload?.toString(), statusCode)
        return this
    }

    fun <T : Map<*, *>> replyAsJson(payload: T, statusCode: Int = HttpStatus.OK_200): HttpCall {
        response = JsonResponse(payload, statusCode)
        return this
    }

    fun replyAsJson(payload: JsonSerializable, statusCode: Int = HttpStatus.OK_200): HttpCall {
        response = JsonResponse(payload, statusCode)
        return this
    }

    fun reply(response: Response): HttpCall {
        this.response = response
        return this
    }

    fun render(templateName: String, arg: Pair<String, Any?>, statusCode: Int = HttpStatus.OK_200): ViewResponse {
        return render(templateName, mapOf(arg), statusCode)
    }

    fun render(
        templateName: String,
        args: Map<String, Any?>? = null,
        statusCode: Int = HttpStatus.OK_200
    ): ViewResponse {
        return ViewResponse(templateName.replace(".", "/"), args, statusCode)
            .also { this.response = it }
    }

    fun acknowledge(statusCode: Int = HttpStatus.NO_CONTENT_204): HttpCall {
        return reply(AcknowledgementResponse(statusCode))
    }

    fun render(
        templateName: String,
        args: MutableMap<String, Any?>? = null,
        statusCode: Int = 200,
        block: ArgsBuilder.() -> Unit
    ): ViewResponse {
        val builder = ArgsBuilder(args ?: mutableMapOf()).also(block)
        return render(templateName, builder.map(), statusCode)
    }

    fun json(args: MutableMap<String, Any?>? = null, statusCode: Int = 200, block: ArgsBuilder.() -> Unit): HttpCall {
        val builder = ArgsBuilder(args ?: mutableMapOf()).also(block)
        return replyAsJson(builder.map(), statusCode)
    }

    internal fun startAsync(timeoutDuration: Duration = Duration.ofSeconds(0)): AsyncContext {
        return servletRequest.startAsync(servletRequest, servletResponse).apply {
            timeout = timeoutDuration.toMillis()
        }.apply {
            addListener(object : AsyncListener {
                override fun onComplete(event: AsyncEvent?) {}
                override fun onStartAsync(event: AsyncEvent?) {}
                override fun onTimeout(event: AsyncEvent?) {
                    event?.asyncContext?.complete()
                }

                override fun onError(event: AsyncEvent?) {
                    event?.asyncContext?.complete()
                }
            })
        }
    }
}
