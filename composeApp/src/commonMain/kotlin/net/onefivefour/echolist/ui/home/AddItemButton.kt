package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import net.onefivefour.echolist.ui.theme.EchoListTheme
import net.onefivefour.echolist.ui.theme.LocalDimensions

@Composable
fun AddItemButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val mediumShape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .clip(mediumShape)
            .background(MaterialTheme.colorScheme.surface, mediumShape)
            .border(dimensions.borderWidth, primaryColor, mediumShape)
            .clickable(onClick = onClick)
            .padding(dimensions.m),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add item",
            tint = primaryColor
        )
    }
}

@Preview
@Composable
fun AddItemButtonPreview() {
    EchoListTheme {
        AddItemButton(
            onClick = { }
        )
    }
}
