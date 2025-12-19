package ch.hslu.newcmpproject

import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.entity.UserSimple
import ch.hslu.newcmpproject.entity.UserStorage
import ch.hslu.newcmpproject.entity.decodeBase64UrlToString
import ch.hslu.newcmpproject.network.TaskApi
import kotlinx.serialization.json.Json


class TaskSDK(val database: Database, val api: TaskApi) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val tokenStorage = TokenStorage()
    val token: Token?
        get() = tokenStorage.loadToken()?.let { Token(it) }

    // User
    val userStorage = UserStorage()

    val currentUser: UserSimple?
        get() = userStorage.loadUser()


    val userId: Long?
        get() = currentUser?.userId

    val username: String?
        get() = currentUser?.userName


    private fun extractUserFromToken(token: String): UserSimple? {
        val parts = token.split(".")
        if (parts.size < 2) {
            return null
        }

        val payloadBase64 = parts[1]
        val decodedJson = decodeBase64UrlToString(payloadBase64)
        val payload = try {
            json.decodeFromString<UserSimple>(decodedJson)
        } catch (e: Exception) {
            return null
        }

        return UserSimple(
            userId = payload.userId,
            userName = payload.userName,
            role = payload.role
        )
    }

    suspend fun isServerOnline():Boolean{
        return api.isServerOnline()
    }

    suspend fun login(username: String, password: String): Boolean {
        val token = api.login(username, password)
        if (token.value.isBlank()) return false

        tokenStorage.saveToken(token.value)

        val user = extractUserFromToken(token.value) ?: return false
        userStorage.saveUser(user)

        return true
    }

    fun logout() {
        tokenStorage.clearToken()
        userStorage.clearUser()
    }

    suspend fun updateUsername(userId: Long, newUsername: String): Boolean {
        val currentToken = token ?: return false
        val isAdmin = currentUser?.role == "ADMIN"

        // PUT-Request
        val response = api.updateUsername(
            token = currentToken,
            userId = if (isAdmin) userId else null, // null = eigener User
            newUsername = newUsername
        )

        // Prüfen, ob Server OK gesendet hat
        if (!response.isSuccessful) return false

        // Neues Token nur speichern, falls Self-Update
        response.token?.let { returnedToken ->
            tokenStorage.saveToken(returnedToken)
            extractUserFromToken(returnedToken)?.let { userStorage.saveUser(it) }
        }

        return true
    }



    suspend fun updatePassword(
        userId: Long,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        val currentToken = token ?: return false
        val isAdmin = currentUser?.role == "ADMIN"

        return api.updatePassword(
            token = currentToken,
            userId = if (isAdmin) userId else null,
            oldPassword = oldPassword,
            newPassword = newPassword
        )
    }


    // User Admin

    suspend fun addUser(username: String, password: String, role: String = "USER"): Boolean {
        val currentToken = token ?: return false
        return api.addUser(currentToken, username, password, role)
    }
    suspend fun getAllUsers(): List<UserSimple> {
        val currentToken = token ?: return emptyList()
        return api.getAllUsers(currentToken)
    }

    suspend fun getUserWithId(userId: Long): UserSimple? {
        val currentToken = token ?: return null
        return api.getUserWithId(currentToken, userId)
    }

    suspend fun deleteUser(userId: Long): Boolean {
        val currentToken = token ?: return false
        return api.deleteUser(currentToken, userId)
    }


    // TASKS
    suspend fun getTasks(): List<Task> {
        val id = userId ?: return emptyList() // Kein User → keine Tasks laden
        return database.getTasks(id)
    }


    suspend fun addTask(task: Task, isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val taskWithUserID = task.copy(userId = id)
        val newTask = database.insertTask(taskWithUserID)

        val currentToken = token ?: return false
        return if (isServerOnline) api.addTask(currentToken, newTask) else false
    }

    suspend fun updateTask(task: Task, isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val taskWithUserID = task.copy(userId = id)
        database.updateTask(taskWithUserID)

        val currentToken = token ?: return false
        return if (isServerOnline) api.updateTask(currentToken, taskWithUserID) else false
    }

    suspend fun deleteTask(task: Task, isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val taskWithUserID = task.copy(userId = id)
        database.deleteTask(taskWithUserID)

        val currentToken = token ?: return false
        return if (isServerOnline) api.deleteTask(currentToken, task.id.toLong()) else false
    }

    suspend fun isInSync(): Boolean {
        val id = userId ?: return false
        val currentToken = token ?: return false
        val serverTasks = api.getTasks(currentToken)
        if (serverTasks.isEmpty()) return false
        val localTasks = database.getTasks(id)
        return localTasks == serverTasks
    }

    suspend fun postTasks(isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val currentToken = token ?: return false
        return if (isServerOnline) api.replaceTasks(currentToken, database.getTasks(id)) else false
    }

    suspend fun pullTasks(isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val currentToken = token ?: return false
        if (isServerOnline) {
            val serverTasks = api.getTasks(currentToken)
            if (serverTasks.isNotEmpty()) {
                database.replaceTasks(id, serverTasks)
                return true
            }
        }
        return false
    }

    suspend fun mergeTasks(isServerOnline: Boolean): Boolean {
        val id = userId ?: return false
        val currentToken = token ?: return false
        if (isServerOnline) {
            val serverTasks = api.getTasks(currentToken)
            val localTasks = database.getTasks(id)

            val mergedTasks = (localTasks + serverTasks)
                .distinctBy { it.id }

            database.replaceTasks(id, mergedTasks)
            return api.replaceTasks(currentToken, mergedTasks)
        }
        return false
    }
}
