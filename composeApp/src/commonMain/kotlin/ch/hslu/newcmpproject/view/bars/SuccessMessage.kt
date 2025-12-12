package ch.hslu.newcmpproject.view.bars

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun SuccessMessage(taskViewModel: TaskViewModel) {
    val syncMessage by taskViewModel.syncMessage.collectAsState()
    val serverOnline by taskViewModel.isServerOnline.collectAsState()

    // State für auf-/zugeklappt
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Sync-Message
        if (syncMessage.text.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (syncMessage.isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    .clickable { expanded = !expanded }  // <--- klickbar zum Aufklappen
                    .padding(8.dp)
                    .animateContentSize(),              // <--- animiert die Größe
                contentAlignment = Alignment.CenterStart
            ) {

                Text(
                    text = syncMessage.text,
                    color = Color.White,
                    softWrap = true,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,  // <--- aufklappen
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Serverstatus
        Box(
            modifier = Modifier
                .background(
                    color = if (serverOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (serverOnline) "Server online" else "Server offline",
                color = Color.White
            )
        }
    }
}
