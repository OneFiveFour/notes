package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
fun BreadcrumbNav(
    breadcrumbs: List<BreadcrumbItem>,
    onBreadcrumbClick: (path: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = EchoListTheme.dimensions
    val primaryColor = EchoListTheme.materialColors.primary
    val onPrimaryColor = EchoListTheme.materialColors.onPrimary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, item ->
            val isLast = index == breadcrumbs.lastIndex

            if (isLast) {
                // Current segment: pill-shaped chip
                val pillShape = RoundedCornerShape(50)
                Text(
                    text = item.label,
                    style = EchoListTheme.typography.labelMedium,
                    color = onPrimaryColor,
                    modifier = Modifier
                        .clip(pillShape)
                        .background(primaryColor, pillShape)
                        .padding(
                            horizontal = dimensions.m,
                            vertical = dimensions.xs
                        )
                )
            } else {
                // Non-current segment: clickable plain text
                Text(
                    text = item.label,
                    style = EchoListTheme.typography.labelMedium,
                    color = primaryColor,
                    modifier = Modifier.clickable { onBreadcrumbClick(item.path) }
                )
                Text(
                    text = " / ",
                    style = EchoListTheme.typography.labelMedium,
                    color = primaryColor
                )
            }
        }
    }
}
