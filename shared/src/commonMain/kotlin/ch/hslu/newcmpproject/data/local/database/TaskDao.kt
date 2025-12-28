package ch.hslu.newcmpproject.data.local.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import ch.hslu.cmpproject.cache.AppDatabaseQueries
import ch.hslu.newcmpproject.data.local.database.mapper.TaskMapper
import ch.hslu.newcmpproject.domain.entity.Task

class TaskDao(
    private val queries: AppDatabaseQueries
) {

    suspend fun getAll(userId: Long): List<Task> =
        queries.selectAllTasks(userId, TaskMapper::map)
            .awaitAsList()

    suspend fun getById(userId: Long, taskId: Long): Task? =
        queries.selectTaskById(taskId, userId, TaskMapper::map)
            .awaitAsOneOrNull()

    suspend fun insert(task: Task): Task =
        queries.transactionWithResult {
            queries.insertTask(
                userId = task.userId,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )

            val newId = queries.lastInsertRowId().awaitAsOne()

            queries.selectTaskById(
                newId,
                task.userId,
                TaskMapper::map)
                .awaitAsOne()
        }

    // Folgender Code

    suspend fun update(task: Task) =
        queries.updateTask(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    suspend fun upsert(task: Task) =
        queries.insertOrReplaceTask(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            status = task.status
        )

    // Folgender Code

    suspend fun delete(taskId: Long, userId: Long) =
        queries.deleteTaskById(taskId, userId)

    suspend fun replaceAll(userId: Long, tasks: List<Task>) =
        queries.transaction {
            queries.deleteAllTasks(userId)
            tasks.forEach { task ->
                queries.insertOrReplaceTask(
                    id = task.id,
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