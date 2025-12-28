package ch.hslu.newcmpproject.domain.entity.serverRequests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    @SerialName("username")
    val username: String,
    @SerialName("userId")
    val userId: Long? = null
)