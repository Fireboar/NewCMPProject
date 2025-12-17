package ch.hslu.newcmpproject.cache

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.cmpproject.cache.Users
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.User
import ch.hslu.newcmpproject.security.generateSalt
import ch.hslu.newcmpproject.security.hashPasswordWithSalt

class Database (val driver: SqlDriver){

    private val database = AppDatabase(driver)
    private val dbQuery get() = database.appDatabaseQueries

    // User
    suspend fun getUserByUsername(username: String): User? {
        return dbQuery.selectUserByUsername(username, ::mapUser).awaitAsOneOrNull()
    }

    suspend fun insertUser(username: String, password: String): Long {
        val salt = generateSalt()
        val hash = hashPasswordWithSalt(password, salt)
        val user = insertUserSecure(username,hash, salt)
        return user.id
    }
    suspend fun insertUserSecure(username: String, passwordHash: String, salt: ByteArray): User {
        // Convert salt to hex for storage
        val saltHex = salt.joinToString("") { "%02x".format(it) }

        // Insert User
        dbQuery.insertUser(
            username = username,
            passwordHash = passwordHash,
            salt = saltHex
        )

        // Hole die letzte eingefügte ID
        val newId = dbQuery.lastInsertRowId().awaitAsOne()

        if (newId == 0L) {
            throw IllegalStateException("Insert failed: no ID returned. Check if username is unique and table exists.")
        }

        // Hole den User anhand der ID
        val user = dbQuery.selectUserById(newId, ::mapUser).awaitAsOneOrNull()
            ?: throw IllegalStateException("Insert failed: User not found after insert. Check database schema.")

        return user
    }

    suspend fun updateUsername(userId: Long, username: String) {
        dbQuery.updateUsername(username, userId)
    }

    suspend fun updatePassword(
        userId: Long,
        passwordHash: String,
        salt: String
    ) {
        dbQuery.updatePassword(passwordHash, salt, userId)
    }

    suspend fun getUserById(userId: Long): User? {
        return dbQuery
            .selectUserById(userId)
            .executeAsOneOrNull()
            ?.toUser()
    }

    fun Users.toUser(): User =
        User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            salt = salt
        )


    private fun mapUser(id: Long, username: String, passwordHash: String, salt: String): User {
        return User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            salt = salt
        )
    }

    // Task

    // Mapping
    internal fun mapTask(
        id: Long,
        userId: Long,
        title: String,
        description: String?,
        dueDate: String,
        dueTime: String,
        status: String?
    ): Task {
        return Task(
            id = id,
            userId = userId,
            title = title,
            description = description ?: "",
            dueDate = dueDate,
            dueTime = dueTime,
            status = status ?: "To Do"
        )
    }

    // Read
    internal suspend fun getTasks(userId: Long): List<Task> {
        return dbQuery.selectAllTasks(userId.toLong(), ::mapTask).awaitAsList()
    }

    // Single Read
    internal suspend fun getTaskById(userId: Long, id: Long): Task? {
        return dbQuery
            .selectTaskById(id, userId, ::mapTask)
            .awaitAsOneOrNull()
    }

    internal suspend fun insertTask(task: Task): Task {
        return dbQuery.transactionWithResult {
            dbQuery.insertTask(
                userId = task.userId,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )

            val newId = dbQuery.lastInsertRowId().awaitAsOne()

            dbQuery.selectTaskById(newId, task.userId.toLong(), ::mapTask).awaitAsOne()
        }
    }

    internal suspend fun updateTask(task: Task) {
        dbQuery.updateTask(
            id = task.id.toLong(),
            userId = task.userId.toLong(),
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )
    }

    internal suspend fun upsertTask(task: Task) {
        dbQuery.insertOrReplaceTask(
            id = task.id.toLong(),
            userId = task.userId.toLong(),
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )
    }

    internal suspend fun deleteTask(task: Task) {
        dbQuery.deleteTaskById(task.id.toLong(), task.userId.toLong())
    }

    internal suspend fun replaceTasks(userId: Long, tasks: List<Task>) {
        dbQuery.deleteAllTasks(userId)
        dbQuery.transaction {
            tasks.forEach { task ->
                dbQuery.insertOrReplaceTask(
                    id = task.id.toLong(),
                    userId = userId,
                    title = task.title,
                    description = task.description,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    status = task.status
                )
            }
        }
    }

}

