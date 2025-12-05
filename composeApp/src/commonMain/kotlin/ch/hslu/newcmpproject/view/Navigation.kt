package ch.hslu.newcmpproject.view

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ch.hslu.newcmpproject.view.AddTaskScreen.AddTaskScreen
import ch.hslu.newcmpproject.view.AddTaskScreen.AddTaskScreenDesktopWeb
import ch.hslu.newcmpproject.view.KanBanScreen.KanbanScreen
import ch.hslu.newcmpproject.view.bars.BottomNavigationBar
import ch.hslu.newcmpproject.view.bars.TopBar
import ch.hslu.newcmpproject.viewmodel.TaskViewModel

enum class PlatformType { ANDROID, IOS, DESKTOP, WEB }

expect fun getPlatform(): PlatformType

enum class ScreenType { KANBAN, ADDTASK}

@Composable
fun Navigation(taskViewModel: TaskViewModel) {
    var currentScreen by rememberSaveable {
        mutableStateOf(ScreenType.KANBAN)
    }

    fun navigateTo(screen: ScreenType) {
        currentScreen = screen
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
                paddingValues
            )

            ScreenType.ADDTASK -> {
                when (getPlatform()) {
                    PlatformType.DESKTOP, PlatformType.WEB -> AddTaskScreenDesktopWeb(taskViewModel, paddingValues)
                    else -> AddTaskScreen(taskViewModel, paddingValues)
                }
            }

        }

    }
}
