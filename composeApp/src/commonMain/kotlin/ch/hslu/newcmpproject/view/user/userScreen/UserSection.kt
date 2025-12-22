package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel
@Composable
fun UserSection(
    userViewModel: UserViewModel,
    onUserClick: (userId: Long) -> Unit
) {
    val userName = userViewModel.currentUser.value?.userName
    val isLoggedIn = userViewModel.isLoggedIn.value

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (isLoggedIn) {
            // Card als editierbarer Bereich
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(userViewModel.currentUser.value!!.userId) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // z.B. leichtes Gelb
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Name: $userName", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Role: ${userViewModel.currentUser.value?.role?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Optional Logout-Button rechts
                    Button(
                        onClick = { userViewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            }

        } else {
            Text(
                "Not logged in",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}