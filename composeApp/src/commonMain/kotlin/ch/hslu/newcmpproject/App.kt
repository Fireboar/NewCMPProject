package ch.hslu.newcmpproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.cache.provideDbDriver
import ch.hslu.newcmpproject.network.TaskApi
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
        val database = Database(driver)
        val api = TaskApi()
        val sdk = TaskSDK(database, api)

        // Setze die State-Variable
        syncViewModel = SyncViewModel(sdk)

        userViewModel = UserViewModel(
            sdk = sdk,
            syncViewModel = syncViewModel!! // jetzt ist sie gesetzt
        )
        taskViewModel = TaskViewModel(
            sdk,
            syncViewModel!!
        )

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



