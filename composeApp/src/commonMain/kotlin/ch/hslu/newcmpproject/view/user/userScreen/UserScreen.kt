package ch.hslu.newcmpproject.view.user.userScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.hslu.newcmpproject.viewmodel.SyncViewModel
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel


@Composable
fun UserScreen(
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel,
    syncViewModel: SyncViewModel,
    paddingValues: PaddingValues,
    onUserClick: (userId: Long) -> Unit,
    onAddUserClick: () -> Unit,
    isAdmin: Boolean
) {
    val scrollState = rememberScrollState()
    val isServerOnline = syncViewModel.isServerOnline.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(scrollState)
        ) {
            UserSection(userViewModel, onUserClick)

            if (isServerOnline) {
                SyncSection(taskViewModel)
            }

            if (isAdmin && isServerOnline) {
                UserAdminSection(
                    userViewModel = userViewModel,
                    onUserClick = onUserClick,
                    onAddUserClick = onAddUserClick
                )
            }
        }
    }
}

