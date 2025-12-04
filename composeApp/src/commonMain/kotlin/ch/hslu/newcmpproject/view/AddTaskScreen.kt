package ch.hslu.newcmpproject.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime

@Composable
fun AddTaskScreen(taskViewModel: TaskViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschreibung") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fälligkeitsdatum (dd.mm.yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Uhrzeit (hh:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    error = "Titel darf nicht leer sein"
                    return@Button
                }

                try {
                    val partsDate = date.split(".").map { it.toInt() }
                    val partsTime = time.split(":").map { it.toInt() }

                    // Validierung
                    LocalDateTime(
                        year = partsDate[2],
                        month = partsDate[1],
                        day = partsDate[0],
                        hour = partsTime[0],
                        minute = partsTime[1]
                    )

                    taskViewModel.addTask(
                        title = title,
                        description = description,
                        dueDate = date,
                        dueTime = time,
                        status = "To Do"
                    )

                    title = ""
                    description = ""
                    date = ""
                    time = ""
                    error = ""

                } catch (e: Exception) {
                    error = "Datum oder Uhrzeit ungültig"
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Aufgabe hinzufügen")
        }

        val tasks by taskViewModel.tasks.collectAsState()

        Column(modifier = Modifier.padding(top = 16.dp)) {
            tasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = task.description)
                        Text(text = task.dueDate)
                        Text(text = task.dueTime)
                    }
                }
            }
        }
    }
}