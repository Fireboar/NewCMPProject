package ch.hslu.newcmpproject.network

import ch.hslu.newcmpproject.data.local.database.TaskDao
import ch.hslu.newcmpproject.data.remote.api.ApiClient
import ch.hslu.newcmpproject.data.remote.api.TaskApi
import ch.hslu.newcmpproject.network.AuthService
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SyncService(
    private val taskApi: TaskApi,
    private val taskDao: TaskDao,
    private val authService: AuthService
) {

    private val _isServerOnline = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline

    private val _isInSync = MutableStateFlow(true)
    val isInSync: StateFlow<Boolean> = _isInSync

    suspend fun checkServerStatus() {
        val online = isServerOnline()
        _isServerOnline.value = online

        if (online) updateSyncState()
    }

    suspend fun isServerOnline(): Boolean {
        return try {
            val response = ApiClient.client.get("$SERVER_IP/health")
            return response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun updateSyncState() {
        val token = authService.token ?: return
        val userId = authService.currentUser?.userId ?: return
        val serverTasks = taskApi.getTasks(token)
        val localTasks = taskDao.getAll(userId)
        _isInSync.value = localTasks == serverTasks
    }

    // Folgender Code

    suspend fun pull(): Boolean {
        val token = authService.token ?: return false
        val userId = authService.currentUser?.userId ?: return false

        if (!isServerOnline.value) return false

        val serverTasks = taskApi.getTasks(token)
        if (serverTasks.isEmpty()) return false

        taskDao.replaceAll(userId, serverTasks)
        return true
    }

    suspend fun push(): Boolean {
        val token = authService.token ?: return false
        val userId = authService.currentUser?.userId ?: return false

        if (!isServerOnline.value) return false

        return taskApi.replaceTasks(token, taskDao.getAll(userId))
    }

    // Folgender Code

    suspend fun merge(): Boolean {
        val token = authService.token ?: return false
        val userId = authService.currentUser?.userId ?: return false

        if (!isServerOnline.value) return false

        val serverTasks = taskApi.getTasks(token)
        val localTasks = taskDao.getAll(userId)

        if (serverTasks.isEmpty() && localTasks.isEmpty()) return true

        val mergedTasks = (localTasks + serverTasks)
            .distinctBy { it.id }

        taskDao.replaceAll(userId, mergedTasks)

        return taskApi.replaceTasks(token, mergedTasks)
    }



}
