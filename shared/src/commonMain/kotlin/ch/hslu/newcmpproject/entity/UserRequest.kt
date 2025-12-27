package ch.hslu.newcmpproject.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    @SerialName("username")
    val username: String,
    @SerialName("userId")
    val userId: Long? = null
)

@Serializable
data class UpdatePasswordRequest(
    @SerialName("userId")
    val userId: Long? = null,
    @SerialName("oldPassword")
    val oldPassword: String? = null,
    @SerialName("newPassword")
    val newPassword: String
)