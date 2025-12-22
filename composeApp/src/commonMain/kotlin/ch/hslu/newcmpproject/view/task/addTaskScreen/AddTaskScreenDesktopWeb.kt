package ch.hslu.newcmpproject.view.task.addTaskScreen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.view.task.taskDetailScreen.taskForm.TaskFormDesktopWeb
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun AddTaskScreenDesktopWeb(taskViewModel: TaskViewModel, paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp) // 👈 mittlere Spalte
        ) {
            TaskFormDesktopWeb(
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
                paddingValues = paddingValues
            )
        }
    }
}


