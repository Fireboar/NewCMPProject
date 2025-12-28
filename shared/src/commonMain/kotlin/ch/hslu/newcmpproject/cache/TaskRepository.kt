package ch.hslu.newcmpproject.cache

import ch.hslu.newcmpproject.data.database.TaskDao
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.Token
import ch.hslu.newcmpproject.network.auth.AuthService
import ch.hslu.newcmpproject.network.SyncService
import ch.hslu.newcmpproject.network.api.TaskApi

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val authService: AuthService,
    private val syncService: SyncService
) {
    private val token: Token?
        get() = authService.token

    val isServerOnline: Boolean
        get() = syncService.isServerOnline.value

    private val userId get() = authService.currentUser?.userId

    // Folgender Code

    suspend fun addTask(task: Task): Boolean {
        val id = userId ?: return false
        val newTask = taskDao.insert(task.copy(userId = id))

        return isServerOnline && taskApi.addTask(token!!, newTask)
    }

    suspend fun getLocalTasks(): List<Task> =
        userId?.let { taskDao.getAll(it) } ?: emptyList()

    suspend fun updateTask(task: Task): Boolean {
        val id = userId ?: return false
        taskDao.update(task.copy(userId = id))

        return isServerOnline && taskApi.updateTask(token!!, task)
    }

    suspend fun deleteTask(task: Task): Boolean {
        taskDao.delete(
            taskId = task.id,
            userId = userId!!
        )
        return isServerOnline && taskApi.deleteTask(token!!, task.id)
    }
}
