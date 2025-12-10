package ch.hslu.newcmpproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import ch.hslu.cmpproject.cache.AppDatabase
import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.cache.provideDbDriver
import ch.hslu.newcmpproject.network.TaskApi
import ch.hslu.newcmpproject.view.Navigation
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    var taskViewModel by remember { mutableStateOf<TaskViewModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val driver = provideDbDriver(AppDatabase.Schema)
        val database = Database(driver)
        val api = TaskApi()
        val sdk = TaskSDK(database, api)
        taskViewModel = TaskViewModel(sdk)
        isLoading = false
    }

    if (isLoading) {
        Text("Loading…")
    } else {
        taskViewModel?.let {
            MaterialTheme {
                Navigation(taskViewModel = it)
            }
        }
    }
}



