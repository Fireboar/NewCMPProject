package ch.hslu.newcmpproject.data.remote.api

import ch.hslu.newcmpproject.domain.entity.Token
import ch.hslu.newcmpproject.domain.entity.serverRequests.CreateUserRequest
import ch.hslu.newcmpproject.domain.entity.serverRequests.LoginRequest
import ch.hslu.newcmpproject.domain.entity.serverRequests.UpdatePasswordRequest
import ch.hslu.newcmpproject.domain.entity.serverRequests.UpdateUsernameRequest
import ch.hslu.newcmpproject.domain.entity.serverRequests.UserSimple
import ch.hslu.newcmpproject.network.SERVER_IP
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class UserApi {

    /* ADMIN */
    // CREATE
    suspend fun addUser(
        token: Token,
        username: String,
        password: String,
        role: String = "USER"
    ): Boolean = try {
        val response = ApiClient.client.post("${SERVER_IP}/users") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username, password, role))
        }

        response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    // GET ALL USERS
    suspend fun getAllUsers(token: Token): List<UserSimple> {
        return try {
            ApiClient.client.get("${SERVER_IP}/users") {
                header("Authorization", "Bearer ${token.value}")
            }.body<List<UserSimple>>()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }


    // GET SINGLE USER
    suspend fun getUserWithId(
        token: Token,
        userId: Long
    ): UserSimple? {
        return try {
            ApiClient.client.get("${SERVER_IP}/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }.body<UserSimple>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Folgender Code

    // UPDATE USERNAME
    data class UpdateUsernameResult(
        val isSuccessful: Boolean,
        val token: String?
    )
    suspend fun updateUsername(
        token: Token,
        userId: Long?,
        newUsername: String
    ): UpdateUsernameResult
    {
        val response = ApiClient.client.put("${SERVER_IP}/user/username") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(
                UpdateUsernameRequest(
                    userId = userId,
                    username = newUsername
                )
            )
        }

        if (response.status != HttpStatusCode.OK)
            return UpdateUsernameResult(false, null)

        val json = response.body<Map<String, String>>()
        return UpdateUsernameResult(true, json["token"])
    }

    // UPDATE PASSWORD
    suspend fun updatePassword(
        token: Token,
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean {
        val response = ApiClient.client.put("${SERVER_IP}/user/password") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(UpdatePasswordRequest(
                userId = userId,
                newPassword = newPassword,
                oldPassword = oldPassword
            ))
        }
        return response.status == HttpStatusCode.OK
    }


    // DELETE USER
    suspend fun deleteUser(token: Token, userId: Long): Boolean {
        return try {
            val response = ApiClient.client.delete("${SERVER_IP}/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }


    /* USER */
    // LOGIN
    suspend fun login(
        username: String,
        password: String
    ): Token {
        return try {
            val response = ApiClient.client.post("${SERVER_IP}/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status != HttpStatusCode.OK) {
                return Token("")
            }

            val json = response.body<Map<String, String>>()
            val tokenString = json["token"] ?: return Token("")

            Token(tokenString)

        } catch (e: Throwable) {
            e.printStackTrace()
            Token("")
        }
    }
}
