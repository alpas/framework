package dev.alpas.session

import dev.alpas.isOneOf
import org.eclipse.jetty.server.session.Session
import jakarta.servlet.http.HttpServletRequest

const val CSRF_SESSION_KEY = "_csrf"
const val VALIDATION_ERRORS_KEY = "_validation_errors"
const val OLD_INPUTS_KEY = "_old_inputs"
const val INTENDED_URL_KEY = "_intended_url"
const val PREVIOUS_URL_KEY = "_previous_url"
const val PREVIOUS_FLASH_BAG_KEY = "_previous_flash_bag"
const val NEXT_FLASH_BAG_KEY = "_next_flash_bag"

@Suppress("UNCHECKED_CAST", "unused")
class Session(private val request: HttpServletRequest) {
    private val session = request.session as Session
    operator fun <T> invoke(key: String): T? {
        return get<T>(key)
    }

    operator fun <T> invoke(key: String, default: T): T {
        return get(key, default)
    }

    operator fun <T> invoke(key: String, default: () -> T): T {
        return get(key, default)
    }

    operator fun invoke(map: Map<String, Any>): Map<String, Any> {
        return map.apply {
            forEach { key, value -> session.setAttribute(key, value) }
        }
    }

    operator fun <T> get(key: String): T? {
        return session.getAttribute(key) as? T
    }

    operator fun get(key: String): String? {
        return session.getAttribute(key)?.toString()
    }

    operator fun <T> get(key: String, default: T): T {
        return this.invoke(key) as? T ?: default
    }

    operator fun <T> get(key: String, default: () -> T): T {
        return this.invoke(key) as? T ?: default()
    }

    fun has(key: String) = session.getAttribute(key) != null

    fun exists(key: String) = session.attributeNames.asSequence().contains(key)

    fun put(key: String, value: Any?) {
        session.setAttribute(key, value)
    }

    fun put(values: Map<String, Any?>) {
        values.forEach { (key, value) ->
            put(key, value)
        }
    }

    operator fun set(key: String, value: Any?) {
        session.setAttribute(key, value)
    }

    fun <T : Any> pull(key: String): T? {
        return this.invoke<T?>(key)?.also {
            forget(key)
        }
    }

    fun <T : Any> pull(key: String, default: T): T {
        return this.pull(key) ?: default
    }

    fun <T : Any> pull(key: String, default: () -> T): T {
        return this.pull(key) ?: default()
    }

    fun forget(key: String, vararg keys: String) {
        listOf(key, *keys).forEach {
            session.removeAttribute(it)
        }
    }

    internal fun flush() {
        session.attributeNames.toList().forEach {
            forget(it)
        }
    }

    internal fun nextFlashBag(): MutableMap<String, Any?> {
        return getOrCreate(NEXT_FLASH_BAG_KEY, mutableMapOf()) ?: mutableMapOf()
    }

    private fun previousFlashBag(default: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        return getOrCreate(PREVIOUS_FLASH_BAG_KEY) {
            default
        } ?: emptyMap()
    }

    internal fun copyPreviousFlashBag() {
        put(PREVIOUS_FLASH_BAG_KEY, pull(NEXT_FLASH_BAG_KEY))
    }

    fun flashBag(): Map<String, Any?> {
        return previousFlashBag()
    }

    fun userFlashBag(): Map<String, Any?> {
        return flashBag().filterNot { it.key.isOneOf(OLD_INPUTS_KEY, VALIDATION_ERRORS_KEY) }
    }

    fun reflash() {
        nextFlashBag().putAll(previousFlashBag())
    }

    fun flash(name: String, payload: Any?) {
        nextFlashBag()[name] = payload
    }

    fun isValid(): Boolean {
        return session.isValid
    }

    internal fun clearPreviousFlashBag() {
        forget(PREVIOUS_FLASH_BAG_KEY)
    }

    fun invalidate() {
        session.invalidate()
    }

    fun regenerate(flushCurrent: Boolean = false) {
        if (flushCurrent) {
            flush()
        }
        request.changeSessionId()
    }

    fun <T> getOrCreate(key: String, default: () -> T?): T? {
        if (exists(key)) {
            return get<T>(key)
        }
        return default().also {
            put(key, it)
        }
    }

    fun <T> getOrCreate(key: String, default: T?): T? {
        if (exists(key)) {
            return get<T>(key)
        }
        put(key, default)
        return default
    }

    fun csrfToken(): String? {
        return this[CSRF_SESSION_KEY]
    }

    fun savePreviousUrl(url: String) {
        this.put(PREVIOUS_URL_KEY, url)
    }

    fun saveIntendedUrl(url: String) {
        this.put(INTENDED_URL_KEY, url)
    }

    fun old(): Map<String, List<Any>> {
        val flash = flashBag()
        return flash[OLD_INPUTS_KEY] as? Map<String, List<Any>> ?: emptyMap()
    }

    fun errors(): Map<String, List<Any>> {
        val flash = flashBag()
        return flash[VALIDATION_ERRORS_KEY] as? Map<String, List<Any>> ?: emptyMap()
    }

    fun intendedUrl(): String? {
        return get(INTENDED_URL_KEY)
    }

    fun previousUrl(): String? {
        return get(PREVIOUS_URL_KEY)
    }
}
