package ch.hslu.newcmpproject.view.user.editUserScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun UserDetailScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues,
    currentUserId: Long?
) {
    val isLoggedIn = taskViewModel.isLoggedIn.value
    val initialUsername = taskViewModel.currentUserName.value ?: ""

    // 🔹 Lokaler UI-State
    var username by remember { mutableStateOf(initialUsername) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") } // Feedback

    val scrollState = rememberScrollState() // Scroll-Zustand

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        if (isLoggedIn && currentUserId != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 400.dp) // mittlere Spalte
                    .verticalScroll(scrollState) // Scroll aktivieren
                    .padding(16.dp)
            ) {
                // --- Username Section ---
                Text("Username", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { taskViewModel.updateUsername(username) }) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Password Section ---
                Text("Password", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (oldPassword.isNotBlank() && newPassword.isNotBlank()) {
                                taskViewModel.updatePassword(oldPassword, newPassword)
                                oldPassword = ""
                                newPassword = ""
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Text
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            Text("You are not logged in.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
