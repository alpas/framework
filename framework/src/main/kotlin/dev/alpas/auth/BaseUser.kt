package dev.alpas.auth

import dev.alpas.md5
import dev.alpas.ozone.OzoneEntity
import java.time.Instant
import java.util.*

interface BaseUser<E : BaseUser<E>> : OzoneEntity<E>, Authenticatable {
    override var id: Long
    override var password: String
    override var email: String?
    var name: String?
    override var rememberToken: String?
    val emailVerifiedAt: Instant?
    var createdAt: Instant?
    var updatedAt: Instant?

    @ExperimentalUnsignedTypes
    fun gravatarUrl(): String {
        val hash = email?.trim()?.lowercase(Locale.getDefault())?.md5() ?: ""
        return "//www.gravatar.com/avatar/$hash?s=160&d=robohash"
    }

    override fun isEmailVerified() = emailVerifiedAt != null
}
