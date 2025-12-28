package ch.hslu.newcmpproject.network.api

import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.network.SERVER_IP
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class TaskApi() {

    suspend fun getTasks(token: Token): List<Task> {
        return try {
            ApiClient.client.get("${SERVER_IP}/tasks") {
                header("Authorization", "Bearer ${token.value}")
            }.body<List<Task>>()  // Typ explizit angeben
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Folgender Code

    suspend fun addTask(token:Token, task: Task):Boolean {
        return try {
            val response = ApiClient.client.post("${SERVER_IP}/tasks") {
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

    // Folgender Code

    suspend fun updateTask(token:Token, task: Task) :Boolean {
        return try {
            val response = ApiClient.client.put("${SERVER_IP}/tasks/${task.id}") {
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

    // Folgender Code

    suspend fun deleteTask(token:Token, id: Long): Boolean {
        return try {
            val response = ApiClient.client.delete("${SERVER_IP}/tasks/$id"){
                header("Authorization", "Bearer ${token.value}")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    // Folgender Code

    suspend fun replaceTasks(token:Token, tasks: List<Task>): Boolean {
        return try {
            val response = ApiClient.client.post("${SERVER_IP}/tasks/replace") {
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

}

