package ch.hslu.newcmpproject

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.hslu.newcmpproject.view.KanBanScreen.COLUMN_WIDTH_DP

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NewCMPProject",
        state = rememberWindowState(width = 3*COLUMN_WIDTH_DP+4*20.dp)
        ) {
        App()
    }
}