package ch.hslu.newcmpproject.view.user.userScreen

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
fun SyncSection (taskViewModel: TaskViewModel){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Synchronisation",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Pull Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { taskViewModel.pullTasks() }) {
                    Text("Pull Tasks")
                }
            }

            // Push Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { taskViewModel.postTasks() }) {
                    Text("Push Tasks")
                }
            }

            // Merge Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { taskViewModel.mergeTasks() }) {
                    Text("Merge Tasks")
                }
            }

        }
    }
}