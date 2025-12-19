package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel

@Composable
fun UserAdminSection(
    userViewModel: UserViewModel,
    onUserClick: (userId: Long) -> Unit,
    onAddUserClick: () -> Unit,
) {
    val currentUser = userViewModel.currentUser.value
    val isAdmin = currentUser?.role == "ADMIN"

    if (!isAdmin) return // Nicht-Admin: nichts anzeigen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.Center // Zentriert die Column
    ) {
        Column(
            modifier = Modifier
                .width(400.dp) // Feste Breite für mittige Darstellung
        ) {
            Text(
                "Admin Section",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAddUserClick() },
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Add User")
                }

                Button(
                    onClick = { userViewModel.loadAllUsers() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load Users")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val users by userViewModel.allUsers.collectAsState()
            Column {
                users.forEach { user ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(user.userName)
                        Row {
                            Button(onClick = { onUserClick(user.userId) }) { Text("Edit") }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(onClick = { userViewModel.deleteUser(user.userId) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
