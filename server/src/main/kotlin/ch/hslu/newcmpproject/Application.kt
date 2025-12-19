package ch.hslu.newcmpproject

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.entity.CreateUserRequest
import ch.hslu.newcmpproject.entity.LoginRequest
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.UpdatePasswordRequest
import ch.hslu.newcmpproject.entity.UpdateUsernameRequest
import ch.hslu.newcmpproject.entity.UserSimple
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

    fun generateToken(userId: Long, username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("userName", username)
            .withClaim("role", role)      // ⚡ hier hinzufügen
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
    val thisId = existingUser?.id ?: database.insertUser(
        username = "admin", password = "123",
        role = "ADMIN"
    )

    // Verify that it exists
    val adminUser = database.getUserByUsername("admin")
    if (adminUser != null) {
        println("Admin user exists: id=${adminUser.id}, username=${adminUser.username}, password=${adminUser.passwordHash}, salt=${adminUser.salt} role=${adminUser.role}")
    } else {
        println("Failed to create admin user!")
    }


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

    fun JWTPrincipal.isAdmin(): Boolean =
        payload.getClaim("role").asString() == "ADMIN"


    routing {
        post("/login") {
            val loginRequest = call.receive<LoginRequest>() // z.B. username + password
            val user = database.getUserByUsername(loginRequest.username)

            if (user == null || !verifyPassword(loginRequest.password, user)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token = JwtConfig.generateToken(user.id, user.username, user.role)

            call.respond(mapOf("token" to token))
        }

        // HEALTH
        get("/health") {
            call.respondText("OK")
        }

        authenticate("auth-jwt") {
            // Admin

            // READ ALL USERS
            get("/users") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Admin only")
                }

                val users = database.getAllUsers()  // Eine Funktion, die alle User zurückgibt
                call.respond(users.map { user ->
                    // Nur die notwendigen Daten zurückgeben, z.B. ID + Username + Role
                    UserSimple(
                        userId = user.id,
                        userName = user.username,
                        role = user.role
                    )
                })
            }

            get("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val requesterId = principal.payload.getClaim("userId").asLong()
                val requesterRole = principal.payload.getClaim("role").asString()

                // ID aus der URL lesen
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user id")

                // Zugriff prüfen: entweder Admin oder eigener User
                if (requesterRole != "ADMIN" && requesterId != userId) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Access denied")
                }

                // User aus DB holen
                val user = database.getUserById(userId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

                // Nur sichere Daten zurückgeben
                call.respond(
                    UserSimple(
                        userId = user.id,
                        userName = user.username,
                        role = user.role
                    )
                )
            }


            // Create
            post("/users") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Admin only")
                }

                val userrequest = call.receive<CreateUserRequest>() // username + password

                val existing = database.getUserByUsername(userrequest.username)
                if (existing != null) {
                    return@post call.respond(HttpStatusCode.Conflict, "User already exists")
                }

                val userId = database.insertUser(
                    userrequest.username,
                    userrequest.password,
                    userrequest.role
                )

                val user = database.getUserById(userId)!!

                val userResponse = UserSimple(
                    userId = user.id,
                    userName = user.username,
                    role = user.role
                )

                call.respond(userResponse)
            }

            // User
            // UPDATE USERNAME
            put("/user/username") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val adminRole = principal.payload.getClaim("role").asString()
                val requesterId = principal.payload.getClaim("userId").asLong()

                val request = call.receive<UpdateUsernameRequest>()
                val targetUserId = request.userId ?: requesterId  // eigener User, falls keine ID angegeben

                // Nur Admin darf andere User ändern
                if (targetUserId != requesterId && adminRole != "ADMIN") {
                    return@put call.respond(HttpStatusCode.Forbidden, "Only admins can update other users")
                }

                // Prüfen, ob Username bereits existiert
                val existingUser = database.getUserByUsername(request.username)
                if (existingUser != null && existingUser.id != targetUserId) {
                    return@put call.respond(HttpStatusCode.Conflict, "Username already exists")
                }

                // Update durchführen
                database.updateUsername(targetUserId, request.username)

                val targetUser = database.getUserById(targetUserId)!!

                // Neues JWT nur für eigenen User
                if (targetUserId == requesterId) {
                    val newToken = JwtConfig.generateToken(targetUser.id, targetUser.username, targetUser.role)
                    call.respond(HttpStatusCode.OK, mapOf("token" to newToken))
                } else {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User updated"))
                }
            }


            // Update Password
            put("/user/password") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val adminRole = principal.payload.getClaim("role").asString()
                val requesterId = principal.payload.getClaim("userId").asLong()

                val request = call.receive<UpdatePasswordRequest>()
                val targetUserId = request.userId ?: requesterId

                if (targetUserId != requesterId && adminRole != "ADMIN") {
                    return@put call.respond(HttpStatusCode.Forbidden, "Only admins can change other users' passwords")
                }

                val user = database.getUserById(targetUserId)
                    ?: return@put call.respond(HttpStatusCode.NotFound, "User not found")

                if (targetUserId == requesterId) {
                    val oldPassword = request.oldPassword
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            "Old password required"
                        )

                    if (!verifyPassword(oldPassword, user)) {
                        return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            "Wrong password"
                        )
                    }
                }

                val newSalt = generateSalt()
                val newPasswordHash = hashPasswordWithSalt(request.newPassword, newSalt)
                database.updatePassword(targetUserId, newPasswordHash, newSalt.joinToString("") { "%02x".format(it) })

                call.respond(HttpStatusCode.OK, mapOf("message" to "Password updated"))
            }

            // Delete
            delete("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@delete call.respond(HttpStatusCode.Forbidden, "Admin only")
                }

                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                database.deleteUser(userId)

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

