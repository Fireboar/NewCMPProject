package ch.hslu.newcmpproject

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NewCMPProject",
    ) {
        App()
    }
}