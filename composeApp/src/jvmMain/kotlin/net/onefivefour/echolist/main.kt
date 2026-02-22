package net.onefivefour.echolist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.onefivefour.echolist.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "EchoList",
        ) {
            App()
        }
    }
}