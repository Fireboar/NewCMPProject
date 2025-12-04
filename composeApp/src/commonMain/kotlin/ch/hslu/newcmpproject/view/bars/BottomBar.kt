package ch.hslu.newcmpproject.view.bars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import ch.hslu.newcmpproject.view.ScreenType

@Composable
fun BottomNavigationBar(
    currentScreen: ScreenType,
    onNavigate: (ScreenType) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1E88E5)
    ) {
        NavigationBarItem(
            selected = currentScreen == ScreenType.KANBAN,
            onClick = { onNavigate(ScreenType.KANBAN) },
            icon = { Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = "Kanban") },
            label = { Text("Kanban", fontSize = 14.sp, color = Color.White) }
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.ADDTASK,
            onClick = { onNavigate(ScreenType.ADDTASK) },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Task") },
            label = { Text("Add Task", fontSize = 14.sp, color = Color.White) }
        )
    }
}
