package ch.hslu.newcmpproject

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NewCMPProject",
        state = rememberWindowState(width = 1600.dp, height = 800.dp)
        ) {
        App()
    }
}