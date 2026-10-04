package net.onefivefour.echolist.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun ElButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = EchoListTheme.materialColors.primary,
            contentColor = EchoListTheme.materialColors.onPrimary
        )
    ) {
        content()
    }
}

@Preview
@Composable
private fun ElButtonPreview() {
    EchoListTheme {
        ElButton(onClick = { }) {
            Text("Button")
        }
    }
}