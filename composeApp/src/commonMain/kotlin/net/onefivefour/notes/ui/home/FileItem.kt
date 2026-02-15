package net.onefivefour.notes.ui.home

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.notes.ui.theme.LocalBeepMeDimensions

@Composable
fun FileItem(
    file: FileUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalBeepMeDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val mediumShape = MaterialTheme.shapes.medium
    val smallShape = MaterialTheme.shapes.small

    Row(
        modifier = modifier
            .clip(mediumShape)
            .background(MaterialTheme.colorScheme.surface, mediumShape)
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
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(dimensions.xs))
            Text(
                text = file.preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = file.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
