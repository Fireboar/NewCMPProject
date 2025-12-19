package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel


@Composable
fun UserScreen(
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel,
    paddingValues: PaddingValues,
    onUserClick: (userId: Long) -> Unit,
    onAddUserClick: () -> Unit,
    isAdmin: Boolean
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
            userViewModel,
            onUserClick
        )

        // Admin-Section nur sichtbar für Admins
        if (isAdmin) {
            UserAdminSection(
                userViewModel = userViewModel,
                onUserClick = onUserClick,
                onAddUserClick = onAddUserClick
            )
        }
    }
}




