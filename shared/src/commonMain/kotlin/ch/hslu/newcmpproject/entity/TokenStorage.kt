package ch.hslu.newcmpproject.entity

import com.russhwolf.settings.Settings

class TokenStorage {

    private val settings = Settings()

    fun saveToken(token: String) {
        settings.putString("jwt_token", token)
    }

    fun loadToken(): String? {
        return settings.getStringOrNull("jwt_token")
    }

    fun clearToken() {
        settings.remove("jwt_token")
    }

    fun saveUsername(username: String) {
        settings.putString("username", username)
    }

    fun loadUsername(): String {
        return settings.getString("username", "")
    }

    fun saveUserId(userId: Long) {
        settings.putLong("userId", userId)
    }

    fun loadUserId(): Long {
        return settings.getLong("userId", 0)
    }
}