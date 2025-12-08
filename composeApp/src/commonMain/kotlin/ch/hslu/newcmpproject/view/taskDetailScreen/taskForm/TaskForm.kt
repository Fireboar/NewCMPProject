package ch.hslu.newcmpproject.view.taskDetailScreen.taskForm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.model.Task
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    paddingValues: PaddingValues,
    taskId: Int? = null,
    taskViewModel: TaskViewModel,
    buttonText: String,
    onSubmit: (Task) -> Unit,
    onNavigateBack: () -> Unit = {}
) {

    val tasks by taskViewModel.tasks.collectAsState()
    val existingTask = tasks.find { it.id == taskId }

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var dueDate by remember { mutableStateOf(existingTask?.dueDate ?: "") }
    var dueTime by remember { mutableStateOf(existingTask?.dueTime ?: "") }
    var status by remember { mutableStateOf(existingTask?.status ?: "To Do") }

    var error by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues).padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschreibung") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Fälligkeitsdatum") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        OutlinedTextField(
            value = dueTime,
            onValueChange = { dueTime = it },
            label = { Text("Fälligkeitszeit") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        // ---- Status Dropdown ----
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = status,
                onValueChange = {},
                label = { Text("Status") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("To Do", "In Progress", "Done").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            status = option
                            expanded = false
                        }
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    error = "Titel darf nicht leer sein"
                    return@Button
                }

                try {
                    val partsDate = dueDate.split(".").map { it.toInt() }
                    val partsTime = dueTime.split(":").map { it.toInt() }

                    LocalDateTime(
                        year = partsDate[2],
                        month = partsDate[1],
                        day = partsDate[0],
                        hour = partsTime[0],
                        minute = partsTime[1]
                    )

                    val task = Task(
                        id = existingTask?.id ?: 0,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        status = status
                    )

                    onSubmit(task)   // Hier wird der Task nach oben weitergegeben
                    onNavigateBack() // Bei update kann hier zurück zum KanBan gesprungen werden

                    title = ""
                    description = ""
                    dueDate = ""
                    dueTime = ""
                    error = ""

                } catch (e: Exception) {
                    error = "Datum oder Uhrzeit ungültig"
                }

            },
            modifier = Modifier.align(Alignment.End).padding(16.dp)
        ) {
            Text(buttonText)
        }


    }
}

