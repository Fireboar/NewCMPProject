package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.hslu.newcmpproject.viewmodel.TaskViewModel


@Composable
fun UserScreen(
    taskViewModel: TaskViewModel,
    paddingValues: PaddingValues,
    onUserClick: (userId: Long) -> Unit
    /*isAdmin: Boolean // Flag, ob aktueller Nutzer Admin ist*/
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(scrollState)
    ) {
        // Bestehende Sections
        SyncSection(taskViewModel)
        UserSection(
            taskViewModel,
            onUserClick
        )

        // Admin-Section nur sichtbar für Admins
        /*if (isAdmin) {
            UserAdminSection(
                taskViewModel = taskViewModel,
                onUserClick = onUserClick
            )
        }*/
    }
}




