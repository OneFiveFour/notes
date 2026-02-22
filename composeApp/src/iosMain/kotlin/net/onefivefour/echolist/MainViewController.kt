package net.onefivefour.echolist

import androidx.compose.ui.window.ComposeUIViewController
import net.onefivefour.echolist.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
