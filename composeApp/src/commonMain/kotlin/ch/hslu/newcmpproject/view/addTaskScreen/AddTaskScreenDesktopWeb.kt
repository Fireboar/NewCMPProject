package ch.hslu.newcmpproject.view.addTaskScreen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import ch.hslu.newcmpproject.view.taskDetailScreen.taskForm.TaskFormDesktopWeb
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

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


