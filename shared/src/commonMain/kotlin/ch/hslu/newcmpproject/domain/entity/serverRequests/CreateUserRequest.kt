package ch.hslu.newcmpproject.domain.entity.serverRequests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    @SerialName("username")
    val username: String,
    @SerialName("passwoprd")
    val password: String,
    @SerialName("role")
    val role: String
)