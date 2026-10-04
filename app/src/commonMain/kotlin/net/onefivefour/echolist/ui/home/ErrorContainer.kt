package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun ErrorContainer(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(EchoListTheme.dimensions.l),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.secondary
        )
    }
}
