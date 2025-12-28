package ch.hslu.newcmpproject.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {

    private const val SERVER_IP = "http://192.168.1.22:8080"

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 3000
            requestTimeoutMillis = 10000
        }
    }

    fun url(path: String) = "$SERVER_IP$path"
}
