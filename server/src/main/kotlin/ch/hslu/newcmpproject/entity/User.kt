package ch.hslu.newcmpproject.entity

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
    val salt: String
)


