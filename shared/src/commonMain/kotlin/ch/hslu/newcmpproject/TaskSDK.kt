package ch.hslu.newcmpproject

import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.entity.TokenPayload
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.entity.decodeBase64UrlToString
import ch.hslu.newcmpproject.network.TaskApi
import kotlinx.serialization.json.Json


class TaskSDK(val database: Database, val api: TaskApi) {

    val tokenStorage = TokenStorage()

    val token: Token?
        get() = tokenStorage.loadToken()?.let { Token(it) }

    val userId: Long
        get() = tokenStorage.loadUserId() ?: throw Exception("User not logged in")

    val username: String
        get() = tokenStorage.loadUsername() ?: throw Exception("User not logged in")



    private fun extractUserIdFromToken(token: String): Long {
        val payloadBase64 = token.split(".")[1]
        val decodedJson = decodeBase64UrlToString(payloadBase64)
        return Json.decodeFromString<TokenPayload>(decodedJson).userId
    }

    private fun extractUsernameFromToken(token: String): String {
        val payloadBase64 = token.split(".")[1]
        val decodedJson = decodeBase64UrlToString(payloadBase64)
        return Json.decodeFromString<TokenPayload>(decodedJson).userName
    }


    suspend fun login(username: String, password: String): Boolean {
        val token = api.login(username, password)

        return if (token.value.isNotBlank()) {
            tokenStorage.saveToken(token.value)
            tokenStorage.saveUserId(extractUserIdFromToken(token.value))
            tokenStorage.saveUsername(extractUsernameFromToken(token.value))
            true
        } else {
            false
        }
    }

    fun logout() {
        tokenStorage.clearToken()
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        val currentToken = token ?: return false
        val updatedToken = api.updateUsername(currentToken, newUsername)

        tokenStorage.saveToken(updatedToken.value)
        tokenStorage.saveUsername(extractUsernameFromToken(updatedToken.value))
        return true
    }


    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val currentToken = token ?: return false

        // API-Aufruf
        val success = api.updatePassword(currentToken, oldPassword, newPassword)
        return success
    }

    suspend fun getTasks(): List<Task> {
        return database.getTasks(userId)
    }

    suspend fun addTask(task: Task, isServerOnline: Boolean): Boolean {
        val taskWithUserID = task.copy(userId=userId)
        val newTask = database.insertTask(taskWithUserID)

        val currentToken = token ?: return false
        return if (isServerOnline) api.addTask(currentToken, newTask) else false
    }

    suspend fun updateTask(task: Task, isServerOnline: Boolean): Boolean {
        val taskWithUserID = task.copy(userId=userId)
        database.updateTask(taskWithUserID)

        val currentToken = token ?: return false
        return if (isServerOnline) api.updateTask(currentToken, taskWithUserID) else false
    }

    suspend fun deleteTask(task: Task, isServerOnline: Boolean): Boolean {
        val taskWithUserID = task.copy(userId=userId)

        database.deleteTask(taskWithUserID)
        val currentToken = token ?: return false
        return if (isServerOnline) api.deleteTask(currentToken, task.id.toLong()) else false
    }

    suspend fun isServerOnline(): Boolean {
        return api.isServerOnline()
    }

    suspend fun isInSync(): Boolean {
        val currentToken = token ?: return false
        println("USING TOKEN: $currentToken")
        val serverTasks = api.getTasks(currentToken)
        if (serverTasks.isEmpty()) return false
        val localTasks = database.getTasks(userId)
        return localTasks == serverTasks
    }

    suspend fun postTasks(isServerOnline: Boolean): Boolean {
        val currentToken = token ?: return false
        return if (isServerOnline) api.replaceTasks(currentToken, database.getTasks(userId)) else false
    }

    suspend fun pullTasks(isServerOnline: Boolean): Boolean {
        val currentToken = token ?: return false
        if (isServerOnline) {
            val serverTasks = api.getTasks(currentToken)
            if (serverTasks.isNotEmpty()) {
                database.replaceTasks(userId, serverTasks)
                return true
            }
        }
        return false
    }

    suspend fun mergeTasks(isServerOnline: Boolean): Boolean {
        val currentToken = token ?: return false
        if (isServerOnline) {
            val serverTasks = api.getTasks(currentToken)
            val localTasks = database.getTasks(userId)

            val mergedTasks = (localTasks + serverTasks)
                .distinctBy { it.id }

            database.replaceTasks(userId, mergedTasks)
            return api.replaceTasks(currentToken, mergedTasks)
        }
        return false
    }
}
