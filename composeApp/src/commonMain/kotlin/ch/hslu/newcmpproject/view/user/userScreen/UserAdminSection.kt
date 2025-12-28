package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel

@Composable
fun UserAdminSection(
    userViewModel: UserViewModel,
    onUserClick: (userId: Long) -> Unit,
    onAddUserClick: () -> Unit,
) {
    val users by userViewModel.allUsers.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadAllUsers()
    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Users (Admin)",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add User Button
            Button(
                onClick = onAddUserClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Grün
                    contentColor = Color.White
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add User"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("User Liste:", style = MaterialTheme.typography.titleMedium)
        Column {
            users.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onUserClick(user.userId) }, // Klick auf die ganze Card
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5) // leichter Hintergrund
                    ),
                    elevation = cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Name: ${user.userName}")
                            Text(
                                "Role: ${user.role.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        DeleteUserButton(onDelete = { userViewModel.deleteUser(user.userId) })

                    }
                }
            }
        }


    }
}
