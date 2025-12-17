package ch.hslu.newcmpproject

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.entity.LoginRequest
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.UpdatePasswordRequest
import ch.hslu.newcmpproject.entity.UpdateUsernameRequest
import ch.hslu.newcmpproject.security.generateSalt
import ch.hslu.newcmpproject.security.hashPasswordWithSalt
import ch.hslu.newcmpproject.security.verifyPassword
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

object JwtConfig {
    private const val secret = "super-secret-key" // sicherer Schlüssel, z.B. aus env
    private const val issuer = "ch.hslu.newcmpproject"
    private const val audience = "ch.hslu.newcmpproject.audience"
    const val realm = "Access to tasks"

    fun generateToken(userId: Long, username: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("userName", username)
            .sign(Algorithm.HMAC256(secret))
    }

    fun verifier() = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}


suspend fun Application.module() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                if (userId != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

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
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
    }

    // SQLDelight initialisieren
    val driver = JdbcSqliteDriver("jdbc:sqlite:server.db")
    AppDatabase.Schema.create(driver).await()
    val database = Database(driver)


    // Prüfen, ob schon ein User existiert
    val existingUser = database.getUserByUsername("admin")
    val thisId = existingUser?.id ?: database.insertUser("admin", "123")

    // Prüfen, ob Tasks für diesen User existieren
    if (database.getTasks(thisId).isEmpty()) {
        val defaultTask = Task(
            userId = thisId,
            title = "Server-Task",
            description = "Dies ist ein Default-Task",
            dueDate = "12.12.2025",
            dueTime = "12:00",
            status = "To Do"
        )
        database.insertTask(defaultTask)
    }

    routing {
        post("/login") {
            val loginRequest = call.receive<LoginRequest>() // z.B. username + password
            val user = database.getUserByUsername(loginRequest.username)

            if (user == null || !verifyPassword(loginRequest.password, user)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token = JwtConfig.generateToken( user.id, user.username)
            call.respond(mapOf("token" to token))
        }

        // HEALTH
        get("/health") {
            call.respondText("OK")
        }

        authenticate("auth-jwt") {
            // User

            // UPDATE USERNAME
            put("/user/username") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.payload.getClaim("userId").asLong()
                val request = call.receive<UpdateUsernameRequest>()

                if (request.username.isBlank()) {
                    return@put call.respond(HttpStatusCode.BadRequest, "Username empty")
                }

                database.updateUsername(userId, request.username)

                // ⚠️ neues JWT mit neuem Username
                val newToken = JwtConfig.generateToken(userId, request.username)

                call.respond(HttpStatusCode.OK, mapOf("token" to newToken))
            }

            put("/user/password") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.payload.getClaim("userId").asLong()
                val request = call.receive<UpdatePasswordRequest>()

                val user = database.getUserById(userId)
                    ?: return@put call.respond(HttpStatusCode.NotFound)

                // Prüfen, ob altes Passwort korrekt ist
                if (!verifyPassword(request.oldPassword, user)) {
                    return@put call.respond(HttpStatusCode.Unauthorized, "Wrong password")
                }

                // neuen Salt erzeugen
                val newSalt = generateSalt()
                val newPasswordHash = hashPasswordWithSalt(request.newPassword, newSalt)

                // in DB speichern
                database.updatePassword(userId, newPasswordHash, newSalt.joinToString("") { "%02x".format(it) })

                call.respond(HttpStatusCode.OK)
            }


            // CREATE
            post("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.payload.getClaim("userId").asLong()

                val taskFromClient = call.receive<Task>()

                // 🔐 userId erzwingen
                val task = taskFromClient.copy(userId = userId)

                val insertedTask = database.upsertTask(task)
                call.respond(HttpStatusCode.OK, insertedTask)
            }

            // READ ALL
            get("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asLong()

                val tasks = database.getTasks(userId = userId)
                call.respond(tasks)
            }


            // UPDATE
            put("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid task ID")

                // Task aus Request Body
                val updatedTaskData = call.receive<Task>()

                // Existenz prüfen
                val existingTask = database.getTaskById(userId, taskId)
                if (existingTask == null) {
                    return@put call.respond(HttpStatusCode.NotFound, "Task with id=$taskId not found")
                }

                // userId aus JWT erzwingen & ID aus URL setzen
                val taskToUpdate = updatedTaskData.copy(
                    id = taskId,
                    userId = userId
                )

                // Update in DB
                database.updateTask(taskToUpdate)

                call.respond(HttpStatusCode.OK, taskToUpdate)
            }

            // DELETE
            delete("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL prüfen
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid task ID")

                // Existenz prüfen
                val existingTask = database.getTaskById(userId, taskId)
                if (existingTask == null) {
                    return@delete call.respond(HttpStatusCode.NotFound, "Task not found")
                }

                // Task löschen
                database.deleteTask(existingTask)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Deleted task $taskId"))
            }


            // REPLACE
            post("/tasks/replace") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // Tasks aus Request Body
                val tasksFromClient = call.receive<List<Task>>()

                // userId erzwingen
                val safeTasks = tasksFromClient.map { it.copy(userId = userId) }

                // Alle Tasks für diesen User ersetzen
                database.replaceTasks(userId, safeTasks)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Tasks replaced successfully"))
            }
        }



    }


}

