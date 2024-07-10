package dev.alpas.http

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import dev.alpas.JsonSerializer
import dev.alpas.cookie.CookieJar
import dev.alpas.ifNotBlank
import dev.alpas.session.Session
import org.eclipse.jetty.server.Request
import java.nio.charset.Charset
import jakarta.servlet.MultipartConfigElement
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.Part

const val CONTENT_TYPE_KEY = "Content-Type"
const val X_CSRF_TOKEN_KEY = "X-CSRF-TOKEN"
const val X_XSRF_TOKEN_KEY = "X-XSRF-TOKEN"

@Suppress("unused")
interface RequestableCall {
    val session: Session
    val acceptableContentTypes: List<String>
    val method: Method
    val uri: String
    val url: String
    val fullUri: String
    val remoteAddr: String
    val referrer get() = header("referer")
    val rootUrl: String
    val fullUrl: String
    val queryString: String
    val isXmlHttpRequest get() = header("X-Requested-With") == "XMLHttpRequest"
    val isAjax get() = isXmlHttpRequest
    val isPjax get() = header("X-PJAX") == "true"
    val isJson get() = contentTypeIs("application/json")
    val isEventStream get() = accepts("text/event-stream")
    val isMultipartFormData get() = contentTypeIs("multipart/form-data")
    val isUrlEncodedForm get() = contentTypeIs("application/x-www-form-urlencoded")
    val isPrefetch get() = header("X-Purpose") == "preview" || header("X-moz") == "prefetch"
    val expectsJson get() = (isAjax && !isPjax && acceptsAnyContentType) || wantsJson
    val wantsJson get() = acceptableContentTypes.contains("application/json")
    val acceptsHtml get() = accepts("text/html", "application/xhtml+xml")
    val acceptsAnyContentType
        get() = acceptableContentTypes.isEmpty() || acceptableContentTypes.firstOrNull() in listOf("*/*", "*")
    val isGet get() = methodIs(Method.GET)
    val isPost get() = methodIs(Method.POST)
    val isPut get() = methodIs(Method.PUT)
    val isPatch get() = methodIs(Method.PATCH)
    val isDelete get() = methodIs(Method.DELETE)
    val isReading get() = this.method.isReading()
    val isPreflight
        get() = methodIs(Method.OPTIONS)
                && header("Access-Control-Request-Headers") != null
                && header("Origin") != null
    val cookie: CookieJar
    val jettyRequest: Request
    val body: String
    val jsonBody: Map<String, Any>?
    val multiparts: Iterable<Part>
    val multipartFiles: Iterable<Part>
    val multipartFields: Iterable<Part>
    val multipartParams: Map<String, List<String>>
    val encoding: Charset

    fun header(name: String): String?
    fun headers(name: String): List<String>?
    fun accepts(contentType: String, vararg contentTypes: String): Boolean {
//        if (acceptsAnyContentType) {
//            return true
//        }

        val types = listOf(contentType, *contentTypes)
        return acceptableContentTypes.firstOrNull() { accept ->
            accept in types
        } != null
    }

    fun methodIs(method: Method) = this.method == method

    fun contentTypeIs(key: String): Boolean {
        return header(CONTENT_TYPE_KEY)?.split(";")?.contains(key) ?: false
    }
}

class Requestable(private val servletRequest: HttpServletRequest) : RequestableCall {
    override val jettyRequest by lazy { servletRequest.getAttribute("jetty-request") as Request }
    override val cookie: CookieJar by lazy { CookieJar(servletRequest.cookies ?: emptyArray()) }
    override fun header(name: String): String? = servletRequest.getHeader(name)
    override fun headers(name: String): List<String>? = servletRequest.getHeaders(name).toList()
    override val method by lazy { Method.valueOf(servletRequest.method.toUpperCase()) }
    override val remoteAddr: String get() = servletRequest.remoteAddr
    override val queryString get() = servletRequest.queryString.ifNotBlank { query -> "?$query" }
    override val uri: String get() = servletRequest.requestURI
    override val fullUri get() = "$uri$queryString"
    override val url get() = servletRequest.requestURL.toString()
    override val fullUrl get() = servletRequest.requestURL.append(queryString).toString()
    override val rootUrl get() = jettyRequest.httpURI.toString()
    override val session by lazy { Session(servletRequest) }
    override val multiparts: Iterable<Part> by lazy { servletRequest.parts }
    override val encoding: Charset get() = Charset.forName(servletRequest.characterEncoding ?: Charsets.UTF_8.name())
    override val multipartFiles by lazy {
        prepareForMultipart()
        multiparts.filter { it.isFile }
    }

    override val multipartFields by lazy {
        prepareForMultipart()
        multiparts.filter { it.isFormField }
    }


    override val multipartParams by lazy {
        prepareForMultipart()
        multipartFields.associate { part -> part.name to formFieldValues(part.name) }
    }

    override val acceptableContentTypes by lazy {
        header("Accept")?.split(',')?.map { it.trim() } ?: emptyList()
    }

    override val body: String by lazy {
        servletRequest.inputStream.readBytes()
            .toString(encoding)
    }

    @Suppress("RemoveExplicitTypeArguments")
    override val jsonBody by lazy {
        if (body.isBlank()) {
            mapOf<String, Any>()
        } else {
            try {
                JsonSerializer.deserialize<HashMap<String, Any>>(body)
            } catch (e: MismatchedInputException) {
                null
            } catch (ex: JsonParseException) {
                null
            }
        }
    }

    private fun formFieldValues(name: String): List<String> {
        return multipartFields.filter { it.name == name }.map { part ->
            String(part.inputStream.readBytes(), Charsets.UTF_8)
        }
    }

    private fun prepareForMultipart() {
        jettyRequest.setAttribute(
            "org.eclipse.jetty.multipartConfig",
            MultipartConfigElement(System.getProperty("java.io.tmpdir"))
        )
    }
}

private val Part.isFormField get() = submittedFileName == null
private val Part.isFile get() = !isFormField
