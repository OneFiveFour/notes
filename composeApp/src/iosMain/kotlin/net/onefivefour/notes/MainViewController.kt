package net.onefivefour.notes

import androidx.compose.ui.window.ComposeUIViewController
import net.onefivefour.notes.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
