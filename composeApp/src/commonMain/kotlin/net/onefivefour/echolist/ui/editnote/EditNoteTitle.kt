package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun EditNoteTitle(
    textFieldState: TextFieldState
) {
    Surface(
        shape = EchoListTheme.shapes.medium,
        color = EchoListTheme.materialColors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = EchoListTheme.shapes.medium
            )
    ) {
        BasicTextField(
            modifier = Modifier.padding(EchoListTheme.dimensions.m),
            state = textFieldState,
            textStyle = EchoListTheme.typography.titleLarge.copy(
                color = EchoListTheme.materialColors.onSurface
            )
        )
    }
}