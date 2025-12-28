package ch.hslu.newcmpproject

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.data.database.DatabaseProvider
import ch.hslu.newcmpproject.data.database.TaskDao
import ch.hslu.newcmpproject.cache.database.UserDao
import ch.hslu.newcmpproject.entity.CreateUserRequest
import ch.hslu.newcmpproject.entity.LoginRequest
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.UpdatePasswordRequest
import ch.hslu.newcmpproject.entity.UpdateUsernameRequest
import ch.hslu.newcmpproject.entity.UserSimple
import ch.hslu.newcmpproject.security.PasswordService
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
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module)
        .start(wait = true)
}

// Folgender Code

object JwtConfig {
    private const val secret = "super-secret-key" // sicherer Schlüssel
    private const val issuer = "ch.hslu.newcmpproject"
    private const val audience = "ch.hslu.newcmpproject.audience"
    const val realm = "Access to tasks"

    fun generateToken(userId: Long, username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("userName", username)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(secret))
    }

    fun verifier() = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
}

// Folgender Code


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

    // DB
    val databaseProvider = DatabaseProvider(driver)

    // DAOs
    val userDao = UserDao(databaseProvider.queries)
    val taskDao = TaskDao(databaseProvider.queries)

    // Services
    val passwordService = PasswordService()


    val existingUser = userDao.getByUsername("admin")
    val adminId = existingUser?.id ?: userDao.insert(
        username = "admin",
        password = "123",
        role = "ADMIN"
    )

    // Folgender Code

    fun JWTPrincipal.isAdmin(): Boolean =
        payload.getClaim("role").asString() == "ADMIN"

    // Folgender Code

    routing {
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val user = userDao.getByUsername(loginRequest.username)

            if (user == null || !passwordService.verifyPassword(loginRequest.password, user)) {
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

        // Folgender Code

        authenticate("auth-jwt") {

            /* ADMIN-Section */
            // READ ALL USERS
            get("/users") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Admin only")
                }

                val users = userDao.getAll()  // Eine Funktion, die alle User zurückgibt
                call.respond(users.map { user ->
                    // Nur die notwendigen Daten zurückgeben, z.B. ID + Username + Role
                    UserSimple(
                        userId = user.id,
                        userName = user.username,
                        role = user.role
                    )
                })
            }

            // Folgender Code

            // READ SINGLE
            get("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val requesterId = principal.payload.getClaim("userId").asLong()
                val requesterRole = principal.payload.getClaim("role").asString()

                // ID aus der URL lesen
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid user id")

                // Zugriff prüfen: entweder Admin oder eigener User
                if (requesterRole != "ADMIN" && requesterId != userId) {
                    return@get call.respond(
                        HttpStatusCode.Forbidden,
                        "Access denied")
                }

                // User aus DB holen
                val user = userDao.getById(userId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        "User not found")

                // Nur sichere Daten zurückgeben
                call.respond(
                    UserSimple(
                        userId = user.id,
                        userName = user.username,
                        role = user.role
                    )
                )
            }

            // Folgender Code


            // Create
            post("/users") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        "Admin only")
                }

                val userrequest = call.receive<CreateUserRequest>()

                val existing = userDao.getByUsername(userrequest.username)
                if (existing != null) {
                    return@post call.respond(
                        HttpStatusCode.Conflict,
                        "User already exists")
                }

                val userId = userDao.insert(
                    userrequest.username,
                    userrequest.password,
                    userrequest.role
                )

                val user = userDao.getById(userId)!!

                val userResponse = UserSimple(
                    userId = user.id,
                    userName = user.username,
                    role = user.role
                )

                call.respond(userResponse)
            }

            // Folgender Code

            /* USER-Section */
            // UPDATE USERNAME
            put("/user/username") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val adminRole = principal.payload.getClaim("role").asString()
                val requesterId = principal.payload.getClaim("userId").asLong()

                val request = call.receive<UpdateUsernameRequest>()
                val targetUserId = request.userId ?: requesterId

                // Nur Admin darf andere User ändern
                if (targetUserId != requesterId && adminRole != "ADMIN") {
                    return@put call.respond(
                        HttpStatusCode.Forbidden,
                        "Only admins can update other users")
                }

                // Prüfen, ob Username bereits existiert
                val existingUser = userDao.getByUsername(request.username)
                if (existingUser != null && existingUser.id != targetUserId) {
                    return@put call.respond(
                        HttpStatusCode.Conflict,
                        "Username already exists")
                }

                // Update durchführen
                userDao.updateUsername(targetUserId, request.username)

                val targetUser = userDao.getById(targetUserId)!!

                // Neues JWT nur für eigenen User
                if (targetUserId == requesterId) {
                    val newToken = JwtConfig.generateToken(
                        targetUser.id,
                        targetUser.username,
                        targetUser.role)
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("token" to newToken))
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "User updated"))
                }
            }

            // Folgender Code


            // UPDATE PASSWORD
            put("/user/password") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val adminRole = principal.payload.getClaim("role").asString()
                val requesterId = principal.payload.getClaim("userId").asLong()

                val request = call.receive<UpdatePasswordRequest>()
                val targetUserId = request.userId ?: requesterId

                if (targetUserId != requesterId && adminRole != "ADMIN") {
                    return@put call.respond(
                        HttpStatusCode.Forbidden,
                        "Only admins can change other users' passwords")
                }

                val user = userDao.getById(targetUserId)
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        "User not found")

                if (targetUserId == requesterId) {
                    val oldPassword = request.oldPassword
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            "Old password required"
                        )

                    if (!passwordService.verifyPassword(oldPassword, user)) {
                        return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            "Wrong password"
                        )
                    }
                }

                userDao.updatePassword(
                    id = targetUserId,
                    newPassword = request.newPassword
                )

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Password updated"))
            }

            // Folgender Code

            // DELETE USER
            delete("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                if (!principal.isAdmin()) {
                    return@delete call.respond(
                        HttpStatusCode.Forbidden,
                        "Admin only")
                }

                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                userDao.delete(userId)

                call.respond(HttpStatusCode.OK)
            }

            // Folgender Code


            /* TASKS */
            // CREATE
            post("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.payload.getClaim("userId").asLong()

                val taskFromClient = call.receive<Task>()

                // 🔐 userId erzwingen
                val task = taskFromClient.copy(userId = userId)

                val insertedTask = taskDao.upsert(task)
                call.respond(HttpStatusCode.OK, insertedTask)
            }

            // Folgender Code

            // READ ALL
            get("/tasks") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asLong()

                val tasks = taskDao.getAll(userId = userId)
                call.respond(tasks)
            }

            // Folgender Code


            // UPDATE
            put("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid task ID")

                // Task aus Request Body
                val updatedTaskData = call.receive<Task>()

                // Existenz prüfen
                val existingTask = taskDao.getById(userId, taskId)
                if (existingTask == null) {
                    return@put call.respond(
                        HttpStatusCode.NotFound,
                        "Task with id=$taskId not found")
                }

                // userId aus JWT erzwingen & ID aus URL setzen
                val taskToUpdate = updatedTaskData.copy(
                    id = taskId,
                    userId = userId
                )

                // Update in DB
                taskDao.update(taskToUpdate)

                call.respond(HttpStatusCode.OK, taskToUpdate)
            }

            // Folgender Code

            // DELETE
            delete("/tasks/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // ID aus URL prüfen
                val taskId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid task ID")

                // Existenz prüfen
                val existingTask = taskDao.getById(userId, taskId)
                if (existingTask == null) {
                    return@delete call.respond(
                        HttpStatusCode.NotFound,
                        "Task not found")
                }

                // Task löschen
                taskDao.delete(
                    taskId = existingTask.id,
                    userId = userId
                )

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Deleted task $taskId"))
            }

            // Folgender Code


            // REPLACE
            post("/tasks/replace") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        "Not authenticated")

                val userId = principal.payload.getClaim("userId").asLong()

                // Tasks aus Request Body
                val tasksFromClient = call.receive<List<Task>>()

                // userId erzwingen
                val safeTasks = tasksFromClient.map { it.copy(userId = userId) }

                // Alle Tasks für diesen User ersetzen
                taskDao.replaceAll(userId, safeTasks)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Tasks replaced successfully"))
            }
        }



    }


}

