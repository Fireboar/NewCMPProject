package ch.hslu.newcmpproject.view.task.taskDetailScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import ch.hslu.newcmpproject.view.task.taskDetailScreen.taskForm.TaskFormDesktopWeb
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun TaskDetailScreenDesktopWeb(
    taskId: Long,
    taskViewModel: TaskViewModel,
    outerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    TaskFormDesktopWeb(
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
