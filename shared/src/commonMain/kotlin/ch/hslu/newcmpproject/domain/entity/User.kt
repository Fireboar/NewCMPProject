package ch.hslu.newcmpproject.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Long,
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val passwordHash: String,
    @SerialName("salt")
    val salt: String,
    @SerialName("role")
    val role: String
)