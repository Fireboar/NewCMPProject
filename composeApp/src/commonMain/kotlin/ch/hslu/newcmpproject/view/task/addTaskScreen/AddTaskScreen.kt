package ch.hslu.newcmpproject.view.task.addTaskScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import ch.hslu.newcmpproject.view.task.taskDetailScreen.taskForm.TaskForm
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

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