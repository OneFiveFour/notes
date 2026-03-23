package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.Dimensions
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteError(
    dimensions: Dimensions,
    errorMessage: String
) {
    Spacer(modifier = Modifier.Companion.height(dimensions.s))
    Text(
        text = errorMessage,
        style = EchoListTheme.typography.bodySmall,
        color = EchoListTheme.materialColors.error,
        modifier = Modifier.Companion.fillMaxWidth()
    )
}