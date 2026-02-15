package net.onefivefour.notes.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.onefivefour.notes.ui.theme.LocalBeepMeDimensions

@Composable
fun Header(
    title: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalBeepMeDimensions.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigationClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate back",
                tint = primaryColor
            )
        }
        Spacer(modifier = Modifier.width(dimensions.s))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = primaryColor
        )
    }
}
