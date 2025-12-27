package ch.hslu.newcmpproject.view.task.kanBanScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.toLocalDateTimeOrNull
import ch.hslu.newcmpproject.viewmodel.TaskViewModel


val statuses = listOf("To Do", "In Progress", "Done")

@Composable
fun KanbanScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues,
    onTaskClick: (Task) -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth >= 900.dp   // 👈 Web/Desktop
        val columnWidth = if (isWideScreen) 0.dp else 300.dp

        val rowModifier = if (isWideScreen) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .horizontalScroll(rememberScrollState())
        }

        Row(
            modifier = rowModifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            statuses.forEach { status ->
                val columnBackgroundColor = when (status) {
                    "To Do" -> Color(0xFF90CAF9).copy(alpha = 0.3f)
                    "In Progress" -> Color(0xFFFFF9C4).copy(alpha = 0.3f)
                    "Done" -> Color(0xFFC8E6C9).copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val verticalScroll = rememberScrollState()

                val columnModifier = if (isWideScreen) {
                    Modifier
                        .weight(1f)
                        .fillMaxHeight() // 👈 Column nimmt volle Höhe ein
                } else {
                    Modifier
                        .width(300.dp)
                        .fillMaxHeight() // 👈 auch auf Mobile volle Höhe
                }

                Column(
                    modifier = columnModifier
                        .background(columnBackgroundColor)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val headerColor = when (status) {
                        "To Do" -> Color(0xFF90CAF9)
                        "In Progress" -> Color(0xFFFFF9C4)
                        "Done" -> Color(0xFFC8E6C9)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            .padding(4.dp)
                    )

                    // Scrollbarer Container für die Tasks
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // nimmt restliche Höhe der Column ein
                            .verticalScroll(verticalScroll),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tasks.filter { it.status == status }
                            .sortedBy { it.toLocalDateTimeOrNull() }
                            .forEach { task ->
                                DraggableTaskItem(
                                    task = task,
                                    columnWidthDp = columnWidth.takeIf { !isWideScreen } ?: 300.dp,
                                    onDelete = { taskViewModel.deleteTask(task) },
                                    onMove = { targetStatus -> taskViewModel.moveTask(task, targetStatus) },
                                    onClick = { onTaskClick(task) }
                                )
                            }
                    }
                }
            }

        }
    }
}


