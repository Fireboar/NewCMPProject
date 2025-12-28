package ch.hslu.newcmpproject.domain.entity.serverRequests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePasswordRequest(
    @SerialName("userId")
    val userId: Long? = null,
    @SerialName("oldPassword")
    val oldPassword: String? = null,
    @SerialName("newPassword")
    val newPassword: String
)