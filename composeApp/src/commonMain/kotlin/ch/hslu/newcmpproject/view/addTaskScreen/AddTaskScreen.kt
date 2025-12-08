package ch.hslu.newcmpproject.view.addTaskScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.view.taskDetailScreen.taskForm.TaskForm
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime

@Composable
fun AddTaskScreen(taskViewModel: TaskViewModel, paddingValues: PaddingValues) {
    TaskForm(
        paddingValues,
        taskViewModel = taskViewModel,
        buttonText = "Aufgabe hinzufügen",
        onSubmit = { task ->
            taskViewModel.addTask(
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                status = task.status
            )
        },
        onNavigateBack = { }
    )
}