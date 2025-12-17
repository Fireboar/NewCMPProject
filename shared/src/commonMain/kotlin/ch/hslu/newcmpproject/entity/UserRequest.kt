package ch.hslu.newcmpproject.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    @SerialName("username")
    val username: String
)

@Serializable
data class UpdatePasswordRequest(
    @SerialName("oldPassword")
    val oldPassword: String,
    @SerialName("newPassword")
    val newPassword: String
)