package ch.hslu.newcmpproject.view.task.kanBanScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun DeleteTaskButton(onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    // Button
    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
    ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
    }

    // Bestätigungsdialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Löschen bestätigen") },
            text = { Text("Bist du sicher, dass du diesen Eintrag löschen möchtest?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()       // löschen ausführen
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White)
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White)
                ) {
                    Text("Nein")
                }
            }
        )
    }
}
