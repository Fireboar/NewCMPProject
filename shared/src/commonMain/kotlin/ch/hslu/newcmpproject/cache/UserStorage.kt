package ch.hslu.newcmpproject.cache


import ch.hslu.newcmpproject.domain.entity.serverRequests.UserSimple
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class UserStorage {
    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    fun saveUser(user: UserSimple) {
        settings.putString("user", json.encodeToString(
            UserSimple.serializer(), user))
    }

    fun loadUser(): UserSimple? {
        val data = settings.getStringOrNull("user") ?: return null
        return json.decodeFromString(UserSimple.serializer(), data)
    }

    fun clearUser() {
        settings.remove("user")
    }
}