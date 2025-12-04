package ch.hslu.newcmpproject.view.AddTaskScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime

@Composable
fun AddTaskScreenDesktopWeb(taskViewModel: TaskViewModel, paddingValues: PaddingValues) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // FocusRequester für jedes Feld
    val titleFocus = remember { FocusRequester() }
    val descriptionFocus = remember { FocusRequester() }
    val dateFocus = remember { FocusRequester() }
    val timeFocus = remember { FocusRequester() }

    fun submitTask() {
        if (title.isBlank()) {
            error = "Titel darf nicht leer sein"
            return
        }

        try {
            val partsDate = date.split(".").map { it.toInt() }
            val partsTime = time.split(":").map { it.toInt() }

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

            // Felder zurücksetzen
            title = ""
            description = ""
            date = ""
            time = ""
            error = ""

            titleFocus.requestFocus()

        } catch (e: Exception) {
            error = "Datum oder Uhrzeit ungültig"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(16.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                    submitTask()
                    true
                } else false
            },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocus)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
                        descriptionFocus.requestFocus()
                        true
                    } else false
                }
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschreibung") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(descriptionFocus)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
                        dateFocus.requestFocus()
                        true
                    } else false
                }
        )

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fälligkeitsdatum (dd.mm.yyyy)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(dateFocus)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
                        timeFocus.requestFocus()
                        true
                    } else false
                }
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Uhrzeit (hh:mm)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(timeFocus)
        )

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { submitTask() },
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
