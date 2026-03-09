package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import net.onefivefour.echolist.data.models.FileMetadata
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun NoteItem(
    title: String,
    metadata: FileMetadata.Note?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.secondary,
                shape = EchoListTheme.shapes.small
            )
            .clickable(onClick = onClick),
        shape = EchoListTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(EchoListTheme.dimensions.m)
        ) {
            Text(
                text = title,
                style = EchoListTheme.typography.titleSmall,
                color = EchoListTheme.materialColors.onSurface
            )

            if (metadata != null && metadata.preview.isNotEmpty()) {
                Text(
                    text = metadata.preview,
                    style = EchoListTheme.typography.bodySmall,
                    color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = EchoListTheme.dimensions.xs)
                )
            }
        }
    }
}
