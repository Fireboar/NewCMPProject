package ch.hslu.newcmpproject

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cache.Database
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.entity.Task
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

suspend fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    // SQLDelight initialisieren
    val driver = JdbcSqliteDriver("jdbc:sqlite:server.db")
    AppDatabase.Schema.create(driver).await()
    val database = Database(driver)

    // Beispieltask
    if (database.getTasks().isEmpty()) {
        val defaultTask = Task(
            id = 0, // ID wird in insertTask automatisch gesetzt, falls nötig
            title = "Server-Task",
            description = "Dies ist ein Default-Task",
            dueDate = "12.12.2025",
            dueTime = "12:00",
            status = "To Do"
        )
        database.insertTask(defaultTask)
    }

    routing {
        // CREATE
        post("/tasks") {
            val task = call.receive<Task>()
            val insertedTask = database.insertTask(task)
            call.respond(HttpStatusCode.OK, insertedTask)
        }

        // HEALTH
        get("/health") {
            call.respondText("OK")
        }

        // READ ALL
        get("/tasks") {
            val tasks = database.getTasks()
            call.respond(tasks)
        }


        // UPDATE
        put("/tasks/{id}") {
            // Nullcheck
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }

            // Task aus call
            val updatedTaskData = call.receive<Task>()

            // Task Existenz-Prüfung
            val existingTask = database.getTaskById(id)
            if (existingTask == null) {
                call.respond(HttpStatusCode.NotFound, "Task with id=$id not found")
                return@put
            }

            // ID aus URL nehmen
            val taskToUpdate = updatedTaskData.copy(id = id)

            // Update
            database.updateTask(taskToUpdate)
            call.respond(taskToUpdate)
        }

        // DELETE
        delete("/tasks/{id}") {
            // Nullcheck
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }

            // Task Existenz-Prüfung
            val existingTask = database.getTaskById(id)
            if (existingTask == null) {
                call.respond(HttpStatusCode.NotFound, "Task not found")
                return@delete
            }

            // Delete
            database.deleteTask(existingTask)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Deleted task $id"))
        }

        // REPLACE
        post("/tasks/replace") {
            val tasks = call.receive<List<Task>>()
            database.replaceTasks(tasks)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Tasks replaced successfully"))
        }

    }


}

