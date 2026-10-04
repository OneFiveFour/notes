package net.onefivefour.echolist.feature.note.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.core.designsystem.ui.theme.EchoListTheme

@Composable
internal fun EditNoteError(errorMessage: String) {
    Spacer(modifier = Modifier.height(EchoListTheme.dimensions.s))
    Text(
        text = errorMessage,
        style = EchoListTheme.typography.bodySmall,
        color = EchoListTheme.materialColors.error,
        modifier = Modifier.fillMaxWidth()
    )
}