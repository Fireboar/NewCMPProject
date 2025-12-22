package ch.hslu.newcmpproject.view.task.kanBanScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.entity.Task
import kotlin.math.roundToInt

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun DraggableTaskItem(
    task: Task,
    columnWidthDp: Dp,
    onDelete: () -> Unit,
    onMove: (targetStatus: String) -> Unit,
    onClick: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = null
            )
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(task.id) {
                detectDragGestures(
                    onDrag = { _, dragAmount -> offset += dragAmount },
                    onDragEnd = {
                        val startIndex = statuses.indexOf(task.status)
                        val delta = (offset.x / columnWidthDp.value).toInt()
                        val columnIndex = (startIndex + delta)
                            .coerceIn(0, statuses.lastIndex)
                        val targetStatus = statuses[columnIndex]
                        if (targetStatus != task.status) onMove(targetStatus)
                        offset = Offset.Zero
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = when (task.status) {
            "To Do" -> Color(0xFF90CAF9)
            "In Progress" -> Color(0xFFFFF9C4)
            "Done" -> Color(0xFFC8E6C9)
            else -> MaterialTheme.colorScheme.surface
        }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, maxLines = 1)
                Text("${task.dueDate} ${task.dueTime}", style = MaterialTheme.typography.bodySmall)
            }
            DeleteTaskButton(onDelete = { onDelete() })
        }
    }
}
