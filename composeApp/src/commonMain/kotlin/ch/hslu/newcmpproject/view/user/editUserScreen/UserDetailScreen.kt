package ch.hslu.newcmpproject.view.user.editUserScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.UserViewModel

@Composable
fun UserDetailScreen(
    userViewModel: UserViewModel,
    paddingValues: PaddingValues,
    selectedUserId: Long
) {
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.selectedUser.collectAsState()

    var username by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // 🔑 User laden (einmal pro ID)
    LaunchedEffect(selectedUserId) {
        userViewModel.loadUser(selectedUserId)
    }

    // 🔑 UI-State synchronisieren
    LaunchedEffect(user?.userId) {
        username = user?.userName ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        if (isLoggedIn && user != null) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Username ---
                Text("Username", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = username.isNotBlank() && username != user!!.userName,
                    onClick = { userViewModel.updateUsername(
                        user!!.userId,
                        newUsername = username
                    )
                    }
                ) {
                    Text("Save")
                }

                Spacer(Modifier.height(24.dp))

                // --- Password ---
                Text("Password", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = oldPassword.isNotBlank() && newPassword.length >= 8,
                    onClick = {
                        userViewModel.updatePassword(
                            userId = user!!.userId,
                            oldPassword = oldPassword,
                            newPassword = newPassword
                        )
                        oldPassword = ""
                        newPassword = ""
                    }
                ) {
                    Text("Save")
                }
            }
        } else {
            Text("You are not logged in.")
            println("isLoggedin $isLoggedIn, User: ${user?.userName}")
        }
    }
}
