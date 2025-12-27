package ch.hslu.newcmpproject.cache

import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.network.auth.AuthService
import ch.hslu.newcmpproject.network.SyncService
import ch.hslu.newcmpproject.network.TaskApi

class TaskRepository(
    private val database: Database,
    private val taskApi: TaskApi,
    private val authService: AuthService,
    private val syncService: SyncService
) {

    private val token: Token?
        get() = authService.token

    val isServerOnline: Boolean
        get() = syncService.isServerOnline.value

    private val userId get() = authService.currentUser?.userId

    suspend fun getLocalTasks(): List<Task> =
        userId?.let { database.getTasks(it) } ?: emptyList()

    suspend fun addTask(task: Task): Boolean {
        val id = userId ?: return false
        val newTask = database.insertTask(task.copy(userId = id))

        return isServerOnline && taskApi.addTask(token!!, newTask)
    }

    suspend fun updateTask(task: Task): Boolean {
        val id = userId ?: return false
        database.updateTask(task.copy(userId = id))

        return isServerOnline && taskApi.updateTask(token!!, task)
    }

    suspend fun deleteTask(task: Task): Boolean {
        database.deleteTask(task)
        return isServerOnline && taskApi.deleteTask(token!!, task.id)
    }
}
