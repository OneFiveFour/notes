package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteLoading() {
    Box(
        modifier = Modifier.Companion.fillMaxSize(),
        contentAlignment = Alignment.Companion.Center
    ) {
        Text(
            text = "Loading note...",
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.onSurface
        )
    }
}