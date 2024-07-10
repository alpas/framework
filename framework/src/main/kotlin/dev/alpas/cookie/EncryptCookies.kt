package dev.alpas.cookie

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.encryption.Encrypter
import dev.alpas.http.HttpCall
import dev.alpas.http.X_CSRF_TOKEN_KEY
import dev.alpas.make
import dev.alpas.makeElse
import dev.alpas.session.SessionConfig
import jakarta.servlet.http.Cookie

class EncryptCookies : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        val incomingCookies = cookiesToEncrypt(passable, passable.cookie.incomingCookies)
        val encrypter = passable.make<Encrypter>()
        if (incomingCookies.count() > 0) {
            // decrypt incoming cookies
            incomingCookies.forEach {
                try {
                    it.value = encrypter.decrypt(it.value)
                } catch (e: Exception) {
                    it.value = ""
                }
            }
        }

        forward(passable)

        val outgoingCookies = cookiesToEncrypt(passable, passable.cookie.outgoingCookies.toTypedArray())
        if (outgoingCookies.count() > 0) {
            // encrypt outgoing cookies
            outgoingCookies.forEach {
                it.value = encrypter.encrypt(it.value)
            }
        }
    }

    private fun cookiesToEncrypt(call: HttpCall, cookies: Array<Cookie>): Iterable<Cookie> {
        val config = call.makeElse { SessionConfig(call.env) }
        return cookies.filter {
            it.name !in listOf(
                config.cookieName,
                "JSESSIONID",
                X_CSRF_TOKEN_KEY,
                *config.encryptExcept.toTypedArray()
            )
        }
    }
}
