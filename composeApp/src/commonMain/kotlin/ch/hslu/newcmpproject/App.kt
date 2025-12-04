package ch.hslu.newcmpproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import ch.hslu.newcmpproject.view.Navigation
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val taskViewModel = TaskViewModel()
    MaterialTheme {
        Navigation(taskViewModel)
    }
}


