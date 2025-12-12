package ch.hslu.newcmpproject.view.userScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

@Composable
fun UserScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues
) {
    SyncSection(taskViewModel,paddingValues)
}

