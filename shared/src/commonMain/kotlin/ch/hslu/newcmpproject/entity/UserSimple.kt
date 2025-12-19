package ch.hslu.newcmpproject.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSimple(
    @SerialName("userId")
    val userId: Long,
    @SerialName("userName")
    val userName: String,
    @SerialName("role")
    val role: String
)


@Serializable
data class CreateUserRequest(
    @SerialName("username")
    val username: String,
    @SerialName("passwoprd")
    val password: String,
    @SerialName("role")
    val role: String
)