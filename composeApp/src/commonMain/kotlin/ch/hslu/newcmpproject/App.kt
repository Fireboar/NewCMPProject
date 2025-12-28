package ch.hslu.newcmpproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.domain.repository.TaskRepository
import ch.hslu.newcmpproject.domain.repository.UserRepository
import ch.hslu.newcmpproject.cache.provideDbDriver
import ch.hslu.newcmpproject.data.local.database.TaskDao
import ch.hslu.newcmpproject.cache.TokenStorage
import ch.hslu.newcmpproject.cache.UserStorage
import ch.hslu.newcmpproject.network.SyncService
import ch.hslu.newcmpproject.data.remote.api.TaskApi
import ch.hslu.newcmpproject.data.remote.api.UserApi
import ch.hslu.newcmpproject.data.remote.api.AuthApi
import ch.hslu.newcmpproject.network.AuthService
import ch.hslu.newcmpproject.view.Navigation
import ch.hslu.newcmpproject.viewmodel.SyncViewModel
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel

@Composable
fun App() {
    var taskViewModel by remember { mutableStateOf<TaskViewModel?>(null) }
    var userViewModel by remember { mutableStateOf<UserViewModel?>(null) }
    var syncViewModel by remember { mutableStateOf<SyncViewModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val driver = provideDbDriver(AppDatabase.Schema)
        val database = AppDatabase(driver)

        val taskDao = TaskDao(
            queries = database.appDatabaseQueries
        )

        val taskApi = TaskApi()
        val userApi = UserApi()

        // Storage-Instanzen erstellen
        val tokenStorage = TokenStorage()
        val userStorage = UserStorage()
        val authApi = AuthApi()

        // AuthService
        val authService = AuthService(
            tokenStorage = tokenStorage,
            userStorage = userStorage,
            authApi = authApi
        )

        // SyncService
        val syncService = SyncService(taskApi, taskDao, authService)

        // TaskRepository
        val taskRepository = TaskRepository(
            taskDao, taskApi, authService,
            syncService = syncService
        )
        val userRepository = UserRepository(
            userApi = userApi,
            authService = authService
        )

        // SyncViewModel
        syncViewModel = SyncViewModel(syncService)

        // UserViewModel
        userViewModel = UserViewModel(
            authService, userRepository, syncViewModel!!,
            userStorage = userStorage,
            tokenStorage = tokenStorage
        )

        // TaskViewModel
        taskViewModel = TaskViewModel(
            taskRepository, syncService,
            syncViewModel = syncViewModel!!,
            userViewModel = userViewModel!!
        )

        // Laden beendet
        isLoading = false
    }




    if (!isLoading) {
        MaterialTheme {
            Navigation(
                taskViewModel = taskViewModel!!,
                userViewModel = userViewModel!!,
                syncViewModel = syncViewModel!!
            )
        }
    }

}



