package ch.hslu.newcmpproject.network

import ch.hslu.newcmpproject.entity.Task
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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

    suspend fun getTasks(): List<Task> {
        return try {
            httpClient.get("$serverIP/tasks").body()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addTask(task: Task):Boolean {
        return try {
            val response = httpClient.post("$serverIP/tasks") {
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            return response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTask(task: Task) :Boolean {
        return try {
            val response = httpClient.put("$serverIP/tasks/${task.id}") {
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(id: Long): Boolean {
        return try {
            val response = httpClient.delete("$serverIP/tasks/$id")
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun replaceTasks(tasks: List<Task>): Boolean {
        return try {
            val response = httpClient.post("$serverIP/tasks/replace") {
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

