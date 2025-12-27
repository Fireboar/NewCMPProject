package ch.hslu.newcmpproject.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ch.hslu.newcmpproject.view.task.addTaskScreen.AddTaskScreen
import ch.hslu.newcmpproject.view.task.addTaskScreen.AddTaskScreenDesktopWeb
import ch.hslu.newcmpproject.view.task.kanBanScreen.KanbanScreen
import ch.hslu.newcmpproject.view.bars.BottomNavigationBar
import ch.hslu.newcmpproject.view.bars.SuccessMessage
import ch.hslu.newcmpproject.view.bars.TopBar
import ch.hslu.newcmpproject.view.user.loginScreen.LoginScreen
import ch.hslu.newcmpproject.view.task.taskDetailScreen.TaskDetailScreen
import ch.hslu.newcmpproject.view.task.taskDetailScreen.TaskDetailScreenDesktopWeb
import ch.hslu.newcmpproject.view.user.addUser.AddUserScreen
import ch.hslu.newcmpproject.view.user.editUserScreen.UserDetailScreen
import ch.hslu.newcmpproject.view.user.userScreen.UserScreen
import ch.hslu.newcmpproject.viewmodel.SyncViewModel
import ch.hslu.newcmpproject.viewmodel.TaskViewModel
import ch.hslu.newcmpproject.viewmodel.UserViewModel

enum class PlatformType { ANDROID, IOS, DESKTOP, WEB }

expect fun getPlatform(): PlatformType

enum class ScreenType { KANBAN, ADDTASK,  TASKDETAIL, USER, ADDUSER, USERDETAIL, LOGIN}

@Composable
fun Navigation(taskViewModel: TaskViewModel, userViewModel: UserViewModel, syncViewModel: SyncViewModel) {

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()

    var wasLoggedIn by remember { mutableStateOf(isLoggedIn) }

    var currentScreen by rememberSaveable {
        mutableStateOf(if (isLoggedIn) ScreenType.KANBAN else ScreenType.LOGIN)
    }

    var currentTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    var currentUserId by rememberSaveable {mutableStateOf<Long?>(null)}

    val currentUser = userViewModel.currentUser.value
    val isAdmin = currentUser?.role == "ADMIN"


    fun navigateTo(screen: ScreenType, taskId: Long? = null, userId: Long? = null) {
        currentScreen = screen
        currentTaskId = taskId
        currentUserId = userId
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !wasLoggedIn) {
            currentScreen = ScreenType.KANBAN
        } else if (!isLoggedIn && wasLoggedIn) {
            currentScreen = ScreenType.LOGIN
            currentTaskId = null
            currentUserId = null
        }
        wasLoggedIn = isLoggedIn
    }



    Scaffold(
        topBar = {
            val screenTitle = currentScreen.toString()
                .lowercase().replaceFirstChar { it.uppercaseChar() }
            if(currentScreen != ScreenType.KANBAN){
                TopBar(screenTitle)
            }
        },
        bottomBar = {
            if(isLoggedIn){
                Column (Modifier.fillMaxWidth()) {

                    SuccessMessage(syncViewModel)

                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            navigateTo(screen)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentScreen) {

            ScreenType.KANBAN -> KanbanScreen(
                taskViewModel = taskViewModel,
                paddingValues= paddingValues,
                onTaskClick = { task -> navigateTo(ScreenType.TASKDETAIL, task.id) }
            )

            ScreenType.ADDTASK -> {
                when (getPlatform()) {
                    PlatformType.DESKTOP, PlatformType.WEB -> AddTaskScreenDesktopWeb(taskViewModel, paddingValues)
                    else -> AddTaskScreen(taskViewModel, paddingValues)
                }
            }

            ScreenType.TASKDETAIL -> currentTaskId?.let { taskId ->
                when (getPlatform()) {
                    PlatformType.DESKTOP, PlatformType.WEB -> TaskDetailScreenDesktopWeb(
                        taskId = taskId,
                        taskViewModel = taskViewModel,
                        outerPadding = paddingValues,
                        onNavigateBack = { navigateTo(ScreenType.KANBAN) })
                    else -> TaskDetailScreen(
                        taskId = taskId,
                        taskViewModel = taskViewModel,
                        outerPadding = paddingValues,
                        onNavigateBack = { navigateTo(ScreenType.KANBAN) })
                }
            }

            ScreenType.USER ->
                UserScreen(
                    taskViewModel,
                    userViewModel,
                    syncViewModel,
                    paddingValues,
                    onUserClick = { userId -> navigateTo(ScreenType.USERDETAIL, userId = userId) },
                    isAdmin = isAdmin,
                    onAddUserClick = { navigateTo(ScreenType.ADDUSER) }
                )

            ScreenType.ADDUSER ->
                AddUserScreen(
                    userViewModel,
                    paddingValues
                )

            ScreenType.USERDETAIL -> currentUserId?.let{ userId ->
                UserDetailScreen(
                    userViewModel,
                    paddingValues,
                    userId
                )
            }

            ScreenType.LOGIN -> LoginScreen(userViewModel, taskViewModel, syncViewModel, paddingValues)

        }
    }
}
