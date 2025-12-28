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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
    val isOwnUser = user?.userId == userViewModel.currentUser.collectAsState().value?.userId

    var username by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }


    LaunchedEffect(selectedUserId) {
        userViewModel.loadUser(selectedUserId)
    }

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
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Speichern")
                }

                Spacer(Modifier.height(24.dp))

                // --- Old Password  ---
                if (isOwnUser) {
                    TextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Old Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                // --- New Password  ---
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Button ---
                Spacer(Modifier.height(8.dp))

                Button(
                    enabled = newPassword.length >= 8 && (isOwnUser.not() || oldPassword.isNotBlank()),
                    onClick = {
                        userViewModel.updatePassword(
                            userId = user!!.userId,
                            oldPassword = if (isOwnUser) oldPassword else null,
                            newPassword = newPassword
                        )
                        oldPassword = ""
                        newPassword = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Speichern")
                }

                // Folgender Code

            }
        } else {
            Text("Sie sind nicht eingeloggt.")
            println("isLoggedin $isLoggedIn, User: ${user?.userName}")
        }
    }
}
