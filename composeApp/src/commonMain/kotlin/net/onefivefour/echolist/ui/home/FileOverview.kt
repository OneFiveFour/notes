package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun FileOverview(
    title: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            color = EchoListTheme.materialColors.primary,
            style = EchoListTheme.typography.titleLarge
        )

        Surface(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = EchoListTheme.materialColors.surfaceVariant,
                    shape = RoundedCornerShape(50.dp)
                )
                .fillMaxSize()

        ) {


        }
    }
}