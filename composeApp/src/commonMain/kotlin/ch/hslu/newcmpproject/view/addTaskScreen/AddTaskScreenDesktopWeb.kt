package ch.hslu.newcmpproject.view.addTaskScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.view.taskDetailScreen.taskForm.TaskFormDesktopWeb
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime

@Composable
fun AddTaskScreenDesktopWeb(taskViewModel: TaskViewModel, paddingValues: PaddingValues) {
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


