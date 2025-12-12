package ch.hslu.newcmpproject.view.userScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun SyncSection (taskViewModel: TaskViewModel, paddingValues: PaddingValues){
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "Synchronisation",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {

            // Pull Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Button(onClick = { taskViewModel.pullTasks() }) {
                    Text("Pull Tasks")
                }
                Text(
                    text = "(overwrites local)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Push Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Button(onClick = { taskViewModel.postTasks() }) {
                    Text("Push Tasks")
                }
                Text(
                    text = "(overwrites server)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Merge Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Button(onClick = { taskViewModel.mergeTasks() }) {
                    Text("Merge Tasks")
                }
                Text(
                    text = "(adds tasks from both sides)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

        }
    }
}