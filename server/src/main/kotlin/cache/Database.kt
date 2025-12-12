package cache

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.entity.Task
import kotlin.text.toLong

class Database (val driver: SqlDriver){

    private val database = AppDatabase(driver)
    private val dbQuery get() = database.appDatabaseQueries


    // Mapping
    internal fun mapTaskSelecting(
        id: Long,
        title: String,
        description: String?,
        dueDate: String,
        dueTime: String,
        status: String?
    ): Task {
        return Task(
            id = id.toInt(),
            title = title,
            description = description ?: "",
            dueDate = dueDate,
            dueTime = dueTime,
            status = status ?: "To Do"
        )
    }

    // Read
    internal suspend fun getTasks(): List<Task> {
        return dbQuery.selectAllTasks(::mapTaskSelecting).awaitAsList()
    }

    // Single Read
    internal suspend fun getTaskById(id: Int): Task? {
        return dbQuery
            .selectTaskById(id.toLong(), ::mapTaskSelecting)
            .awaitAsOneOrNull()
    }

    internal suspend fun insertTask(task: Task): Task {
        return dbQuery.transactionWithResult {
            // Insert
            dbQuery.insertTask(
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )

            // ID direkt nach Insert abrufen
            val newId = dbQuery.lastInsertRowId().awaitAsOne()

            // Task zurückgeben
            dbQuery.selectTaskById(newId, ::mapTaskSelecting).awaitAsOne()
        }
    }

    internal suspend fun updateTask(task: Task) {
        dbQuery.updateTask(
            id = task.id.toLong(),
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )
    }

    internal suspend fun deleteTask(task: Task) {
        dbQuery.deleteTaskById(task.id.toLong())
    }

    internal suspend fun replaceTasks(tasks: List<Task>) {
        dbQuery.deleteAllTasks()
        dbQuery.transaction {
            tasks.forEach { task ->
                dbQuery.insertOrReplaceTask(
                    id = task.id.toLong(),
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

