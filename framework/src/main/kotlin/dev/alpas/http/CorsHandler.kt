package dev.alpas.http

import dev.alpas.Application
import dev.alpas.appConfig
import dev.alpas.config
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

private val HttpServletRequest.isPreflight: Boolean
    get() {
        return method == "OPTIONS"
                && getHeader("Access-Control-Request-Headers") != null
                && getHeader("Origin") != null
    }

class CorsHandler(app: Application) {
    private val corsConfig = app.config<CorsConfig>()
    private val enableCors = app.appConfig().enableCors
    fun handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean {
        if (!enableCors || !req.isPreflight) {
            // todo: check and skip for same host
            return false
        }
        if (!confirmOrigin(req)) {
            // todo: send appropriate response - 403 Origin Not Allowed
            return false
        }
        if (!confirmMethod(req)) {
            // todo: send appropriate response - 405 Method Not Allowed
            return false
        }
        if (!addRequestHeaders(req, resp)) {
            // todo: send appropriate response - 403 Header Not Allowed
            return false
        }

        addHeaders(req, resp)

        resp.setContentLength(0)
        resp.contentType = "text/plain charset=UTF-8"
        resp.status = 204
        return true
    }

    private fun addHeaders(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"))
        if (corsConfig.supportsCredentials) {
            resp.addHeader("Access-Control-Allow-Credentials", "true")
        }
        val allowedMethods = corsConfig.allowedMethods
        val method = if (allowedMethods.contains("*")) {
            req.getHeader("Access-Control-Request-Method")
        } else {
            allowedMethods.joinToString(", ")
        }
        resp.addHeader("Access-Control-Allow-Methods", method)

        corsConfig.maxAge?.let {
            resp.addHeader("Access-Control-Max-Age", "${it.toSeconds()}")
        }

        corsConfig.exposedHeaders?.let {
            resp.addHeader("Access-Control-Expose-Headers", it.joinToString(", "))
        }

        resp.addHeader("Vary", "Origin")
    }

    private fun addRequestHeaders(req: HttpServletRequest, resp: HttpServletResponse): Boolean {
        // attach request headers back as long as it is in the allowedHeadersArray
        val allowedHeaders = corsConfig.allowedHeaders
        return req.getHeader("Access-Control-Request-Headers")?.let { header ->
            val shouldAddHeader = if (allowedHeaders.contains("*")) {
                true
            } else {
                // make sure each header is in the allowed list
                header.split(",").all {
                    allowedHeaders.contains(it.trim())
                }
            }
            if (shouldAddHeader) {
                resp.addHeader("Access-Control-Allow-Headers", header)
            }
            shouldAddHeader
        } ?: true

    }

    private fun confirmOrigin(req: HttpServletRequest): Boolean {
        val allowedOrigins = corsConfig.allowedOrigins
        if (allowedOrigins.contains("*")) return true
        val origin = req.getHeader("Origin")
        if (allowedOrigins.contains(origin)) return true

        return false
    }

    private fun confirmMethod(req: HttpServletRequest): Boolean {
        val allowedMethods = corsConfig.allowedMethods
        if (allowedMethods.contains("*")) return true
        return allowedMethods.contains(req.method.toUpperCase())
    }
}
