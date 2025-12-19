package ch.hslu.newcmpproject.entity

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

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
}