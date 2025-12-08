package ch.hslu.newcmpproject.view

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ch.hslu.newcmpproject.view.addTaskScreen.AddTaskScreen
import ch.hslu.newcmpproject.view.addTaskScreen.AddTaskScreenDesktopWeb
import ch.hslu.newcmpproject.view.kanBanScreen.KanbanScreen
import ch.hslu.newcmpproject.view.bars.BottomNavigationBar
import ch.hslu.newcmpproject.view.bars.TopBar
import ch.hslu.newcmpproject.view.taskDetailScreen.TaskDetailScreen
import ch.hslu.newcmpproject.view.taskDetailScreen.TaskDetailScreenDesktopWeb
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

enum class PlatformType { ANDROID, IOS, DESKTOP, WEB }

expect fun getPlatform(): PlatformType

enum class ScreenType { KANBAN, ADDTASK,  TASKDETAIL}

@Composable
fun Navigation(taskViewModel: TaskViewModel) {

    var currentScreen by rememberSaveable {
        mutableStateOf(ScreenType.KANBAN)
    }

    var currentTaskId by rememberSaveable { mutableStateOf<Int?>(null) }

    fun navigateTo(screen: ScreenType, taskId: Int? = null) {
        currentScreen = screen
        currentTaskId = taskId
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
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    navigateTo(screen)
                }
            )
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





        }

    }
}
