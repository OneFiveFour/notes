package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.folder
import echolist.composeapp.generated.resources.ic_arrow_right
import net.onefivefour.echolist.data.models.FileMetadata
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FolderItem(
    title: String,
    metadata: FileMetadata.Folder?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = EchoListTheme.dimensions.borderWidth,
                color = EchoListTheme.materialColors.primary,
                shape = EchoListTheme.shapes.small
            )
            .clickable(onClick = onClick),
        shape = EchoListTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(EchoListTheme.dimensions.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = EchoListTheme.typography.titleSmall,
                color = EchoListTheme.materialColors.onSurface
            )

            if (metadata != null) {
                Spacer(modifier = Modifier.width(EchoListTheme.dimensions.s))
                Text(
                    text = "${metadata.childCount}",
                    style = EchoListTheme.typography.labelSmall,
                    color = EchoListTheme.materialColors.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                modifier = Modifier.size(EchoListTheme.dimensions.l),
                painter = painterResource(Res.drawable.ic_arrow_right),
                contentDescription = stringResource(Res.string.folder),
                tint = EchoListTheme.materialColors.primary
            )
        }
    }
}
