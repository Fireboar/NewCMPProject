package ch.hslu.newcmpproject.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    @SerialName("username")
    val username: String,
    @SerialName("userId")
    val userId: Long? = null // optional, nur für Admins
)

@Serializable
data class UpdatePasswordRequest(
    val userId: Long? = null,
    val oldPassword: String? = null,
    val newPassword: String
)