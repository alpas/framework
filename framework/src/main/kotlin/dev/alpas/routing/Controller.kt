package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.auth.Authenticatable
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification
import dev.alpas.notifications.NotificationDispatcher
import dev.alpas.queue.QueueDispatcher
import dev.alpas.queue.job.Job
import dev.alpas.validation.ValidationGuard
import java.time.Duration
import jakarta.servlet.http.Cookie
import kotlin.reflect.KClass

open class Controller {
    lateinit var call: HttpCall
    open operator fun invoke(call: HttpCall) {}

    fun middlewareFor(methodName: String, call: HttpCall): List<KClass<out Middleware<HttpCall>>> {
        return middleware(call)
            .filter { it.matches(methodName) }
            .flatMap { it.all }
    }

    open fun middleware(call: HttpCall): Iterable<ControllerMiddleware> {
        return listOf()
    }

    protected fun route(name: String, params: Map<String, Any>? = null, absolute: Boolean = true): String {
        return call.urlGenerator.route(name, params, absolute)
    }

    protected fun flash(name: String, payload: Any?) {
        call.session.flash(name, payload)
    }

    protected fun queue(job: Job, onQueue: String? = null, connection: String? = null) {
        call.make<QueueDispatcher>().dispatch(job, onQueue, connection)
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiable: T, vararg notifiables: T) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiable, *notifiables)
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiables: List<T>) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiables)
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiable: T) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiable)
    }

    protected fun <T : Authenticatable> caller(): T = call.caller()

    protected fun auth() = call.authChannel

    protected fun addCookie(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ) {
        call.cookie.add(name, value, lifetime, path, domain, secure, httpOnly)
    }

    protected fun add(cookie: Cookie) {
        call.cookie.add(cookie)
    }

    fun forget(name: String, path: String? = null, domain: String? = null) {
        call.cookie.forget(name, path, domain)
    }

    fun <T : ValidationGuard> guard(validator: KClass<out T>, afterSuccessBlock: T.() -> Unit = {}): T {
        return call.validateUsing(validator, afterSuccessBlock)
    }
}
