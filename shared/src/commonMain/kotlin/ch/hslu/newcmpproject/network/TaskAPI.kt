package ch.hslu.newcmpproject.network

import ch.hslu.newcmpproject.entity.Task
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TaskApi() {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getTasks(): List<Task> {
        return httpClient.get("http://192.168.1.22:8080").body()
    }
}

