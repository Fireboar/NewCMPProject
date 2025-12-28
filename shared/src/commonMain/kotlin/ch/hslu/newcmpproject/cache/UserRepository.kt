package ch.hslu.newcmpproject.cache

import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.entity.UserSimple
import ch.hslu.newcmpproject.network.auth.AuthService
import ch.hslu.newcmpproject.network.api.UserApi

class UserRepository(
    private val userApi: UserApi,
    private val authService: AuthService
) {
    private val token: Token?
        get() = authService.token

    suspend fun addUser(username: String, password: String, role: String): Boolean {
        val currentToken = token ?: return false
        return userApi.addUser(currentToken, username, password, role)
    }

    // Folgdender Code

    suspend fun getAllUsers(): List<UserSimple> {
        val currentToken = token ?: return emptyList()
        return userApi.getAllUsers(currentToken)
    }

    suspend fun getUserWithId(userId: Long): UserSimple? {
        val currentToken = token ?: return null
        return userApi.getUserWithId(currentToken, userId)
    }

    // Folgdender Code

    suspend fun updateUsername(
        userId: Long?,
        newUsername: String
    ): UserApi.UpdateUsernameResult
    {
        val currentToken = token ?: return UserApi.UpdateUsernameResult(false, null)
        val result = userApi.updateUsername(currentToken, userId, newUsername)

        // Self-update: Token speichern
        result.token?.let { newToken -> authService.updateToken(newToken) }

        return result
    }

    suspend fun updatePassword(
        userId: Long?,
        oldPassword: String?,
        newPassword: String
    ): Boolean {
        val currentToken = token ?: return false
        return userApi.updatePassword(currentToken, userId, oldPassword, newPassword)
    }

    // Folgdender Code

    suspend fun deleteUser(userId: Long): Boolean {
        val currentToken = token ?: return false
        return userApi.deleteUser(currentToken, userId)
    }
}

