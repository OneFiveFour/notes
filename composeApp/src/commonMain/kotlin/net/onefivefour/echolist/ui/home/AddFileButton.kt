package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme
import net.onefivefour.echolist.ui.theme.LocalDimensions

@Composable
fun AddFileButton(
    onAddNoteClick: () -> Unit,
    onAddTasklistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val mediumShape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(mediumShape)
            .background(MaterialTheme.colorScheme.surface, mediumShape)
            .border(dimensions.borderWidth, secondaryColor, mediumShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubButton(
            icon = Icons.Default.Description,
            label = "Note",
            onClick = onAddNoteClick,
            modifier = Modifier.weight(1f)
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = dimensions.borderWidth,
            color = secondaryColor
        )

        SubButton(
            icon = Icons.Default.Checklist,
            label = "Tasklist",
            onClick = onAddTasklistClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(dimensions.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun AddFileButtonPreview() {
    EchoListTheme {
        AddFileButton(
            onAddNoteClick = { },
            onAddTasklistClick = { }
        )
    }
}
