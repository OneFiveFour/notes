package net.onefivefour.notes

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.onefivefour.notes.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Notes",
        ) {
            App()
        }
    }
}