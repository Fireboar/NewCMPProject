package ch.hslu.newcmpproject.view.task.taskDetailScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.view.task.taskDetailScreen.taskForm.TaskForm
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun TaskDetailScreen(
    taskId: Long,
    taskViewModel: TaskViewModel,
    outerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp) // 👈 mittlere Spalte
        ) {
            TaskForm(
                paddingValues = PaddingValues(0.dp), // 👈 Padding jetzt hier
                taskId = taskId,
                taskViewModel = taskViewModel,
                buttonText = "Speichern",
                onSubmit = { task ->
                    taskViewModel.updateTask(task)
                },
                onNavigateBack = onNavigateBack
            )
        }
    }
}


