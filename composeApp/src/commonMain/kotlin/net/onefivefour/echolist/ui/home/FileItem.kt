package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun FileItem(
    file: FileUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
    val primaryColor = EchoListTheme.materialColors.primary
    val secondaryColor = EchoListTheme.materialColors.secondary
    val mediumShape = EchoListTheme.shapes.medium
    val smallShape = EchoListTheme.shapes.small

    Row(
        modifier = modifier
            .clip(mediumShape)
            .background(EchoListTheme.materialColors.surface, mediumShape)
            .border(dimensions.borderWidth, secondaryColor, mediumShape)
            .clickable(onClick = onClick)
            .padding(dimensions.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.iconSmall)
                .background(primaryColor.copy(alpha = 0.05f), smallShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = primaryColor
            )
        }
        Spacer(modifier = Modifier.width(dimensions.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.title,
                style = EchoListTheme.typography.titleSmall,
                color = EchoListTheme.materialColors.onSurface
            )
            Spacer(modifier = Modifier.height(dimensions.xs))
            Text(
                text = file.preview,
                style = EchoListTheme.typography.bodySmall,
                color = EchoListTheme.materialColors.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = file.timestamp,
            style = EchoListTheme.typography.labelSmall,
            color = EchoListTheme.materialColors.onSurface
        )
    }
}
