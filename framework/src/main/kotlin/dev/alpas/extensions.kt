@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package dev.alpas

import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.mordant.TermColors
import dev.alpas.exceptions.httpExceptionFor
import io.github.classgraph.ClassInfo
import org.eclipse.jetty.http.HttpStatus
import org.sagebionetworks.url.UrlData
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import jakarta.servlet.http.HttpServletResponse
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.reflect.full.createInstance

val terminalColors = TermColors(TermColors.Level.ANSI256)

fun TermColors.deleteLastLine() {
    print(String.format("\u001b[%dA", 1)) // Move up
    print("\u001b[2K")
}

fun LocalDateTime.format(pattern: String): String? {
    return format(DateTimeFormatter.ofPattern(pattern))
}

fun String.relativize(other: String) = this.relativize(other.toPath())
fun String.relativize(other: Path) = this.toPath().relativize(other).toString()

fun String.now(): String? {
    return LocalDateTime.now().format(this)
}

fun String.asFile() = File(this)

fun HttpServletResponse.charset(): Charset = try {
    Charset.forName(characterEncoding)
} catch (e: Exception) {
    Charset.defaultCharset()
}

fun secureByteArray(size: Int): ByteArray {
    return ByteArray(size).apply {
        SecureRandom().nextBytes(this)
    }
}

fun ByteArray.base64Encoded(): String {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(this)
}

fun String.base64Encoded(): String {
    return this.toByteArray(Charset.defaultCharset()).base64Encoded()
}

fun String.base64Decoded(): String {
    return this.base64DecodedBytes().toString(Charset.defaultCharset())
}

fun String.base64DecodedBytes(): ByteArray {
    return Base64.getUrlDecoder().decode(this)
}

fun <T> T?.isOneOf(item: T?, vararg items: T): Boolean {
    if (this == item) return true
    return items.isNotEmpty() && items.contains(this)
}

fun String.urlDecode(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun secureRandomString(length: Int = 16): String {
    return secureByteArray(length).base64Encoded()
}

fun ZonedDateTime.toDate(): Date {
    return Date.from(this.toInstant())
}

fun Any?.printAsError() {
    echo(terminalColors.red(this.toString()))
}

fun Any?.printAsSuccess() {
    echo(terminalColors.green(this.toString()))
}

fun Any?.printAsWarning() {
    echo(terminalColors.yellow(this.toString()))
}

fun Any?.printAsInfo() {
    echo(terminalColors.blue(this.toString()))
}

fun Any.asGray() = terminalColors.gray.invoke(this.toString())
fun Any.toPath(): Path = Paths.get(this.toString())
fun Any.asYellow() = terminalColors.yellow.invoke(this.toString())
fun Any.asMagenta() = terminalColors.magenta.invoke(this.toString())
fun Any.asCyan() = terminalColors.cyan.invoke(this.toString())
fun Any.asBlue() = terminalColors.blue.invoke(this.toString())

val Throwable.stackTraceString: String
    get() {
        StringWriter().use { sw ->
            PrintWriter(sw).use { pw ->
                printStackTrace(pw)
                return sw.toString()
            }
        }
    }

fun String.md5(): String {
    return this.hash("MD5")
}

fun String.sha1(): String {
    return this.hash("SHA-1")
}

fun String.hash(algorithm: String): String {
    return MessageDigest.getInstance(algorithm).let {
        it.update(this.toByteArray())
        it.digest()
    }.toHexString()
}

fun Map<*, *>.toJson(wrapItWith: String? = null): String {
    return if (wrapItWith != null) {
        JsonSerializer.serialize(mapOf(wrapItWith to this))
    } else {
        JsonSerializer.serialize(this)
    }
}

fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

@ExperimentalUnsignedTypes
fun String.sign(secret: String, algo: String = "HmacSHA256"): String {
    return Mac.getInstance(algo).let {
        it.init(SecretKeySpec(secret.toByteArray(), algo))
        it.doFinal(toByteArray())
    }.toHexString()
}

fun String?.parseQueryParams(): Map<String, String> {
    return UrlData.parseQueryString(this)
}

/**
 * When something isn't null do something, kinda the opposite of ?:
 */
inline infix fun <T : Any, R : Any> T?.whenNotNull(thenDo: (T) -> R?): R? = if (this == null) null else thenDo(this)

inline infix fun String?.whenNotNullOrEmpty(thenDo: (String) -> String?): String? =
    if (this.isNullOrEmpty()) this else thenDo(this)

inline fun String?.ifNotBlank(defaultValue: (String) -> String): String =
    if (!isNullOrBlank()) defaultValue(this!!) else ""

inline infix fun <R : Any> String?.whenNotNullOrEmpty(thenDo: (String) -> R): R? =
    if (this.isNullOrEmpty()) null else thenDo(this)

/**
 * When something isn't null do something, kinda the opposite of ?:
 */
inline infix fun <T : Any, R : Any> T?.withNotNull(thenDo: T.() -> R?): R? = this?.thenDo()

fun String.mustStartWith(prefix: Char): String {
    return mustStartWith(prefix)
}

fun String.mustStartWith(prefix: String): String {
    return mustStartWith(prefix)
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>

inline fun <R> executeAndMeasureTimeMillis(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}

fun <E> E?.orAbort(message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): E {
    return this ?: throw httpExceptionFor(statusCode, message)
}

inline fun <reified T : Any> ClassInfo.load(): T {
    return loadClass().kotlin.let { it.objectInstance ?: it.createInstance() } as T
}

fun hashEquals(strA: String, strB: String): Boolean {
    return MessageDigest.isEqual(strA.toByteArray(), strB.toByteArray())
}

fun <T : Any> startInFuture(suspendingFunction: suspend () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    suspendingFunction.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            if (result.isSuccess) {
                future.complete(result.getOrNull())
            } else {
                future.completeExceptionally(result.exceptionOrNull())
            }
        }
    })
    return future
}
