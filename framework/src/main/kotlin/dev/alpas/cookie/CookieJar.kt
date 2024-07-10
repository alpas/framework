package dev.alpas.cookie

import java.time.Duration
import jakarta.servlet.http.Cookie

class CookieJar(internal val incomingCookies: Array<Cookie> = emptyArray()) {
    val outgoingCookies = mutableListOf<Cookie>()

    operator fun invoke(name: String) = get(name)

    operator fun get(name: String): String? {
        return incomingCookies.firstOrNull {
            it.name == name
        }?.value
    }

    operator fun set(name: String, value: String?) {
        add(name, value)
    }

    fun immortal(
        name: String,
        value: String?,
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ): Cookie {
        return makeCookie(name, value, Duration.ofDays(2190 /*5 years*/), path, domain, secure, httpOnly)
    }

    fun add(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ) = apply {
        return add(makeCookie(name, value, lifetime, path, domain, secure, httpOnly))
    }

    fun add(cookie: Cookie) = apply {
        outgoingCookies.add(cookie)
    }

    fun forget(name: String, path: String? = null, domain: String? = null) = apply {
        add(name, "", lifetime = Duration.ZERO, path = path, domain = domain)
    }

    private fun makeCookie(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ): Cookie {
        return Cookie(name, value).also { cookie ->
            cookie.maxAge = lifetime.seconds.toInt()
            path?.let { cookie.path = it }
            domain?.let { cookie.domain = it }
            cookie.secure = secure
            cookie.isHttpOnly = httpOnly
        }
    }
}
