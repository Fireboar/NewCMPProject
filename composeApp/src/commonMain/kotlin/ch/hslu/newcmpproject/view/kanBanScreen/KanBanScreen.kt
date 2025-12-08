package ch.hslu.newcmpproject.view.kanBanScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.model.Task
import ch.hslu.newcmpproject.model.toLocalDateTimeOrNull
import ch.hslu.newcmpproject.viewmodel.TaskViewModel


val statuses = listOf("To Do", "In Progress", "Done")
val COLUMN_WIDTH_DP = 300.dp

@Composable
fun KanbanScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues,
    onTaskClick: (Task) -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize().padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScroll)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            statuses.forEach { status ->
                val verticalScroll = rememberScrollState()

                val columnColor = when (status) {
                    "To Do" -> Color(0xFF90CAF9)
                    "In Progress" -> Color(0xFFFFF9C4)
                    "Done" -> Color(0xFFC8E6C9)
                    else -> MaterialTheme.colorScheme.surface
                }

                Column(
                    modifier = Modifier
                        .width(COLUMN_WIDTH_DP)
                        .verticalScroll(verticalScroll)
                        .background(columnColor)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = status, style = MaterialTheme.typography.titleMedium)
                    tasks.filter { it.status == status }
                        .sortedBy { it.toLocalDateTimeOrNull() }
                        .forEach { task ->
                            DraggableTaskItem(
                                task = task,
                                columnWidthDp = COLUMN_WIDTH_DP,
                                onDelete = { taskViewModel.deleteTask(task) },
                                onMove = {
                                        targetStatus ->
                                    taskViewModel.moveTask(task, targetStatus)
                                },
                                onClick = { onTaskClick(task) }
                            )
                        }

                }
            }
        }
    }
}

