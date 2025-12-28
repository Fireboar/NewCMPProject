package ch.hslu.newcmpproject.view.user.addUser

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel
import ch.hslu.newcmpproject.model.UserRole


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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.TopCenter // 👈 mittig horizontal
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp) // maximale Breite
                .padding(16.dp),
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
                        .menuAnchor( ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    true)
                )

                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    UserRole.entries.forEach { role ->
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
                        password.length < 8 -> error = "Passwort zu kurz (mind. 8 Zeichen)"
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // grüner Hintergrund
                    contentColor = Color.White           // weiße Schrift
                )
            ) {
                Text("Save")
            }


        }
    }
}
