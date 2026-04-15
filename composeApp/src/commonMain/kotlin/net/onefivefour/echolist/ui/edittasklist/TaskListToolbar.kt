package net.onefivefour.echolist.ui.edittasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.ic_delete
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TaskListToolbar(
    isEnabled: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_delete),
            contentDescription = "Delete task list",
            modifier = Modifier.Companion
                .clip(RoundedCornerShape(50))
                .clickable(enabled = isEnabled) { onDeleteClick() }
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                )
        )
    }
}
