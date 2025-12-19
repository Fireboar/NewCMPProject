package ch.hslu.newcmpproject.network

import ch.hslu.newcmpproject.entity.CreateUserRequest
import ch.hslu.newcmpproject.entity.LoginRequest
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.entity.UpdatePasswordRequest
import ch.hslu.newcmpproject.entity.UpdateUsernameRequest
import ch.hslu.newcmpproject.entity.UserSimple
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.headers
class TaskApi() {

    private val serverIP = "http://192.168.1.22:8080"
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 3000   // 3 Sekunden für TCP-Verbindung
            socketTimeoutMillis = 3000    // 3 Sekunden für Antwortdaten
            requestTimeoutMillis = 10000  // 10 Sekunden für den gesamten Request
        }
    }
    // Admin

    // POST /users (Admin erstellt neuen User)
    suspend fun addUser(
        token: Token,
        username: String,
        password: String,
        role: String = "USER"
    ): Boolean = try {
        println("ADDUSER username: $username password: $password Role: $role")
        val response = httpClient.post("$serverIP/users") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(username, password, role))
        }
        println("Add user response status: ${response.status}")
        response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }


    // GET /users
    suspend fun getAllUsers(token: Token): List<UserSimple> {
        return try {
            httpClient.get("$serverIP/users") {
                 header("Authorization", "Bearer ${token.value}")
            }.body<List<UserSimple>>()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserWithId(
        token: Token,
        userId: Long
    ): UserSimple? {
        return try {
            httpClient.get("$serverIP/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }.body<UserSimple>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class UpdateUsernameResult(val isSuccessful: Boolean, val token: String?)

    suspend fun updateUsername(
        token: Token,
        userId: Long?,
        newUsername: String
    ): UpdateUsernameResult {
        val response = httpClient.put("$serverIP/user/username") {
            header("Authorization", "Bearer ${token.value}")
            contentType(ContentType.Application.Json)
            setBody(UpdateUsernameRequest(userId = userId, username = newUsername))
        }

        if (response.status != HttpStatusCode.OK) return UpdateUsernameResult(false, null)

        val json = response.body<Map<String, String>>()
        return UpdateUsernameResult(true, json["token"])
    }

    suspend fun updatePassword(
        token: Token,
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean {
        val response = httpClient.put("$serverIP/user/password") {
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


    // DELETE /users/{id}
    suspend fun deleteUser(token: Token, userId: Long): Boolean {
        return try {
            val response = httpClient.delete("$serverIP/users/$userId") {
                header("Authorization", "Bearer ${token.value}")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }




    // User
    suspend fun login(
        username: String,
        password: String
    ): Token {
        return try {
            val response = httpClient.post("$serverIP/login") {
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


    suspend fun getTasks(token: Token): List<Task> {
        return try {
            httpClient.get("$serverIP/tasks") {
                header("Authorization", "Bearer ${token.value}")
            }.body<List<Task>>()  // Typ explizit angeben
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun addTask(token:Token, task: Task):Boolean {
        return try {
            val response = httpClient.post("$serverIP/tasks") {
                header("Authorization", "Bearer ${token.value}")
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            return response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTask(token:Token, task: Task) :Boolean {
        return try {
            val response = httpClient.put("$serverIP/tasks/${task.id}") {
                header("Authorization", "Bearer ${token.value}")
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(token:Token, id: Long): Boolean {
        return try {
            val response = httpClient.delete("$serverIP/tasks/$id"){
                header("Authorization", "Bearer ${token.value}")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun replaceTasks(token:Token, tasks: List<Task>): Boolean {
        return try {
            val response = httpClient.post("$serverIP/tasks/replace") {
                header("Authorization", "Bearer ${token.value}")
                contentType(ContentType.Application.Json)
                setBody(tasks)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isServerOnline(): Boolean {
        return try {
            val response = httpClient.get("$serverIP/health")
            return response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            false
        }
    }

}

