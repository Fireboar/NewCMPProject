package ch.hslu.newcmpproject.view.taskDetailScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import ch.hslu.newcmpproject.view.taskDetailScreen.taskForm.TaskForm
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun TaskDetailScreen(
    taskId: Int,
    taskViewModel: TaskViewModel,
    outerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    TaskForm(
        paddingValues = outerPadding,
        taskId = taskId,
        taskViewModel = taskViewModel,
        buttonText = "Speichern",
        onSubmit = { task ->
            taskViewModel.updateTask(task)
        },
        onNavigateBack = { onNavigateBack() }
    )
}


