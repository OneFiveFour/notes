package net.onefivefour.echolist.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun ElTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    style: TextStyle = EchoListTheme.typography.bodyMedium,
    color: Color = EchoListTheme.materialColors.onSurface
) {
    BasicTextField(
        state = state,
        modifier = modifier,
        textStyle = style.copy(color = color),
        cursorBrush = SolidColor(EchoListTheme.materialColors.primary)
    )
}

@Preview
@Composable
private fun ElTextFieldPreview() {
    val state = remember { TextFieldState(initialText = "Sample text") }
    EchoListTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ElTextField(state = state)
        }
    }
}
