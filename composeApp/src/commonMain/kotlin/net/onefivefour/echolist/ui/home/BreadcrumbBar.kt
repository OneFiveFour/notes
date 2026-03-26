package net.onefivefour.echolist.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.home_title
import echolist.composeapp.generated.resources.ic_arrow_right
import echolist.composeapp.generated.resources.ic_home
import net.onefivefour.echolist.ui.theme.EchoListTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.first

@Composable
internal fun BreadcrumbBar(
    breadcrumbs: List<BreadcrumbItem>,
    onBreadcrumbClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(breadcrumbs) {
        if (breadcrumbs.isNotEmpty()) {
            snapshotFlow { scrollState.maxValue }
                .first { it > 0 }

            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = EchoListTheme.materialColors.surfaceVariant,
                shape = RoundedCornerShape(50)
            ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = EchoListTheme.dimensions.m,
                    vertical = EchoListTheme.dimensions.m
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
        ) {
            Icon(
                modifier = Modifier
                    .offset(x = EchoListTheme.dimensions.xs)
                    .clickable { onBreadcrumbClick("") }
                ,
                painter = painterResource(Res.drawable.ic_home),
                contentDescription = stringResource(Res.string.home_title),
                tint = when (breadcrumbs.size) {
                    1 -> EchoListTheme.materialColors.primary
                    else -> EchoListTheme.materialColors.onSurface
                }
            )

            if (breadcrumbs.isEmpty()) {
                return@Row
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
            ) {
                breadcrumbs
                    .dropLast(1)
                    .forEach { bc ->
                        Text(
                            modifier = Modifier.clickable { onBreadcrumbClick(bc.path) },
                            text = bc.label,
                            style = EchoListTheme.typography.labelMedium
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_right),
                            contentDescription = null
                        )
                    }

                Text(
                    text = breadcrumbs.last().label,
                    style = EchoListTheme.typography.labelMedium.copy(
                        color = EchoListTheme.materialColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
@Preview
private fun BreadcrumbBarPreview() {
    EchoListTheme {
        BreadcrumbBar(
            breadcrumbs = listOf(
                BreadcrumbItem(
                    label = "Home",
                    path = ""
                ),
                BreadcrumbItem(
                    label = "Folder 1",
                    path = "folder1"
                ),
                BreadcrumbItem(
                    label = "Folder 2",
                    path = "folder1/folder2"
                )
            ),
            onBreadcrumbClick = { }
        )
    }
}
