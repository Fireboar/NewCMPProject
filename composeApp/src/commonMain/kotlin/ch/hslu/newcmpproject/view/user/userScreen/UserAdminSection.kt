package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun UserAdminSection(
    taskViewModel: TaskViewModel,
    onUserClick: (userId: Long) -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            "Admin Section",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { /* Logik zum neuen User hinzufügen */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Beispiel: Liste existierender User (nur ID und Name)
        /*taskViewModel.allUsers.forEach { user ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${user.userName} (${user.userId})")

                Row {
                    Button(onClick = { onUserClick(user.userId) }) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { taskViewModel.deleteUser(user.userId) }) {
                        Text("Delete")
                    }
                }
            }
        }*/
    }
}