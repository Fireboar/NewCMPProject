package ch.hslu.newcmpproject.view.user.addUser

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel

enum class UserRole(val displayName: String) {
    USER("User"),
    ADMIN("Admin")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    userViewModel: UserViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(paddingValues)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = passwordRepeat,
            onValueChange = { passwordRepeat = it },
            label = { Text("Repeat Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role Dropdown
        ExposedDropdownMenuBox(
            expanded = roleDropdownExpanded,
            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedRole.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // wichtig für das Menü
            )

            ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false }
            ) {
                UserRole.values().forEach { role -> // <-- hier muss `values()` sein, nicht entries
                    DropdownMenuItem(
                        text = { Text(role.displayName) },
                        onClick = {
                            selectedRole = role
                            roleDropdownExpanded = false
                        }
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    username.isBlank() -> error = "Username darf nicht leer sein"
                    password.length < 6 -> error = "Passwort zu kurz (mind. 6 Zeichen)"
                    password != passwordRepeat -> error = "Passwörter stimmen nicht überein"
                    else -> {
                        error = null
                        userViewModel.addUser(
                            username = username,
                            password = password,
                            role = selectedRole.name
                        )
                        username = ""
                        password = ""
                        passwordRepeat = ""
                        selectedRole = UserRole.USER
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
