@file:Suppress("MemberVisibilityCanBePrivate")

package dev.alpas.pulsar

import dev.alpas.*
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.Authenticatable
import dev.alpas.http.HttpCall
import dev.alpas.http.ViewResponse
import dev.alpas.http.middleware.VerifyCsrfToken
import dev.alpas.routing.Router
import io.restassured.RestAssured
import io.restassured.config.SessionConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.client.utils.URIBuilder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.reflect.KClass

lateinit var app: Alpas
    private set

open class AlpasTest(entryClass: Class<*>) : Alpas(emptyArray(), entryClass) {
    lateinit var lastCall: HttpCall
    private val routeMiddlewareToSkip = mutableListOf<String>()

    override fun recordLastCall(call: HttpCall) {
        lastCall = call
    }

    override fun shouldLoadMiddleware(middleware: KClass<out Middleware<HttpCall>>): Boolean {
        return !routeMiddlewareToSkip.contains(middleware.qualifiedName)
    }

    override fun shouldLoadConsoleCommands(): Boolean {
        return true;
    }

    fun skipMiddleware(qualifiedName: String?, others: List<String?> = emptyList()) {
        qualifiedName?.let {
            routeMiddlewareToSkip.add(qualifiedName)
        }
        others.filterNotNull().forEach {
            routeMiddlewareToSkip.add(it)
        }
    }

    fun lastCallIsInitialized(): Boolean {
        return this::lastCall.isInitialized
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestBase(entryClass: Class<*>) {
    open val configureSession = true
    protected val app: AlpasTest by lazy {
        System.setProperty(RUN_MODE, "test")
        AlpasTest(entryClass).also {
            dev.alpas.pulsar.app = it
        }
    }

    protected fun runApp() {
        app.apply {
            app.make<Router>().loadRoutes()
            ignite()
            RestAssured.port = env("APP_PORT", 8090)
            if (configureSession) {
                setupSessionConfig()
            }
        }
    }

    protected fun setupSessionConfig() {
        RestAssured.config = RestAssured.config()
            .sessionConfig(SessionConfig().sessionIdName(make<dev.alpas.session.SessionConfig>().cookieName))
    }

    protected fun stopApp() {
        app.stop()
    }

    protected inline fun <reified T : Any> make() = container().make<T>()

    protected fun call() = app.lastCall

    protected fun container(): Container {
        if (app.lastCallIsInitialized()) {
            return app.lastCall
        }
        return app
    }

    protected fun skipMiddleware(
        middleware: KClass<out Middleware<HttpCall>>, vararg otherMiddleware: KClass<out Middleware<HttpCall>>
    ) {
        app.skipMiddleware(middleware.qualifiedName, otherMiddleware.map { it.qualifiedName })
    }

    protected fun noCSRFMiddleware() {
        skipMiddleware(VerifyCsrfToken::class)
    }

    protected fun RequestSpecification.noCSRFMiddleware() = apply {
        this@TestBase.noCSRFMiddleware()
    }

    protected fun assertRedirect(location: String, status: Int? = null) {
        if (!call().isBeingRedirected()) {
            fail("Call wasn't redirected.")
        }
        val response = call().redirect().redirectResponse
        assertEquals(location, response.location)
        if (status != null) {
            assertEquals(status, response.statusCode)
        }
    }

    protected fun assertRedirectToRoute(
        name: String,
        params: Map<String, Any>? = null,
        absolute: Boolean = true,
        status: Int? = null
    ) {
        assertRedirect(routeNamed(name, params, absolute), status)
    }

    protected fun assertRedirectExternal(location: String, status: Int? = null) {
        val redirect = call().redirect().redirectResponse
        val uri = URIBuilder(redirect.location).clearParameters().build().toString()
        assertEquals(location, uri)
        if (status != null) {
            assertEquals(status, redirect.statusCode)
        }
    }

    protected fun assertResponseHasErrors(map: Map<String, String?>) {
        val errorMap = call().errorBag.asMap()
        map.forEach {
            assertTrue(errorMap.containsKey(it.key))
            it.value?.let { msg ->
                assertTrue(errorMap[it.key]?.contains(msg) ?: false)
            }
        }
    }

    protected fun assertResponseHasErrors(attributesToCheck: List<String>) {
        val errorMap = call().errorBag.asMap()
        attributesToCheck.forEach {
            assertTrue(errorMap.containsKey(it))
        }
    }

    protected fun assertResponseHasNoErrors(attributesToCheck: List<String>) {
        val errorMap = call().errorBag.asMap()
        attributesToCheck.forEach {
            assertTrue(!errorMap.containsKey(it))
        }
    }

    protected fun assertViewIs(viewName: String) {
        when (val response = call().response) {
            is ViewResponse -> assertEquals(viewName, response.name)
            else -> fail("Response is not a view. But is ${response.javaClass.name}")
        }
    }

    protected fun assertViewHas(expectedArgs: Map<String, Any?>, argsSizeShouldMatch: Boolean = false) {
        val actualArgs = viewArgs()
        if (argsSizeShouldMatch) {
            assertEquals(expectedArgs.size, actualArgs?.size)
        }
        expectedArgs.forEach {
            assertEquals(it.value, actualArgs?.get(it.key))
        }
    }

    protected fun viewArgs(): Map<String, Any?>? {
        return when (val response = call().response) {
            is ViewResponse -> response.args
            else -> emptyMap()
        }
    }

    protected fun assertAuthenticated(user: Authenticatable? = null) {
        assertTrue(call().isAuthenticated)
        if (user != null) {
            assertEquals(user.id, call().user.id)
        }
    }

    @BeforeAll
    open fun beforeAll() {
        runApp()
        afterAppStart()
    }

    @AfterAll
    open fun afterAll() {
        beforeAppStop()
        stopApp()
    }

    open fun afterAppStart() {
    }

    open fun beforeAppStop() {
    }

    private fun executeImplementingMethod(name: String): Boolean {
        return try {
            val container = container()
            this.javaClass.getMethod(name, Container::class.java).invoke(this, container)
            true
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    @BeforeEach
    open fun beforeEach() {
        runDatabasePreActions()
    }

    private fun runDatabasePreActions() {
        executeImplementingMethod(RefreshDatabase::refreshDatabase.name)
    }

    @AfterEach
    open fun afterEach() {
    }

    fun becomeUser(user: Authenticatable, justOnce: Boolean = false) = apply {
        var alreadySet = false
        make<AuthConfig>().channelFilter.set {
            if (!justOnce || !alreadySet) {
                it.user = user
            }
            alreadySet = true
        }
    }

    fun RequestSpecification.asUser(user: Authenticatable) = apply {
        becomeUser(user, true)
    }

    fun <T> asUser(user: Authenticatable, block: () -> T): T {
        becomeUser(user, true)
        return block()
    }

    fun routeNamed(name: String, params: Map<String, Any>? = null, absolute: Boolean = true): String {
        return call().routeNamed(name, params, absolute)
    }

    abstract fun Router.loadRoutes()
}

fun RequestSpecification.wantsJson() = apply {
    accept(ContentType.JSON)
}

fun RequestSpecification.bearerToken(token: String) = apply {
    header("Authorization", "Bearer $token")
}

fun RequestSpecification.trapRedirects() = apply {
    redirects().follow(false)
}

