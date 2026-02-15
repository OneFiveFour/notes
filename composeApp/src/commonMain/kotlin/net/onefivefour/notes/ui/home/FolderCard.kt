package net.onefivefour.notes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.notes.ui.theme.LocalBeepMeDimensions

@Composable
fun FolderCard(
    folder: FolderUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalBeepMeDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val mediumShape = MaterialTheme.shapes.medium
    val smallShape = MaterialTheme.shapes.small

    Column(
        modifier = modifier
            .clip(mediumShape)
            .background(MaterialTheme.colorScheme.surface, mediumShape)
            .border(dimensions.borderWidth, primaryColor, mediumShape)
            .clickable(onClick = onClick)
            .padding(dimensions.m)
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.iconMedium)
                .background(primaryColor, smallShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = onPrimaryColor
            )
        }
        Spacer(modifier = Modifier.height(dimensions.s))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(dimensions.xs))
        Text(
            text = "${folder.itemCount} items",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
