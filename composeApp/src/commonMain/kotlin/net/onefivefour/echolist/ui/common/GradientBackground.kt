package net.onefivefour.echolist.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.onefivefour.echolist.ui.theme.EchoListTheme

/**
 * A full-screen background with three radial gradient orbs
 * matching the Figma "Background" frame (node 4077:2).
 *
 * Uses [EchoListTheme.echoListColorScheme] colors:
 * - background for the base fill
 * - backgroundGradient1 for the top-left orb
 * - backgroundGradient2 for the bottom-right orb
 * - backgroundGradient3 for the center orb
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = EchoListTheme.echoListColorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        GradientOrb(
            color = colors.backgroundGradient1,
            alignment = Alignment.TopStart,
            diameter = 500.dp,
            blurRadius = 200.dp
        )

        GradientOrb(
            color = colors.backgroundGradient3,
            alignment = Alignment.Center,
            diameter = 750.dp,
            blurRadius = 150.dp
        )

        GradientOrb(
            color = colors.backgroundGradient2,
            alignment = Alignment.BottomEnd,
            diameter = 600.dp,
            blurRadius = 150.dp
        )

        content()
    }
}

@Composable
private fun BoxScope.GradientOrb(
    color: Color,
    alignment: Alignment,
    diameter: Dp,
    blurRadius: Dp
) {
    val halfDiameter = diameter / 2
    val offsetX = when (alignment) {
        Alignment.TopStart -> -halfDiameter
        Alignment.BottomEnd -> halfDiameter
        else -> 0.dp
    }
    val offsetY = when (alignment) {
        Alignment.TopStart -> -halfDiameter
        Alignment.BottomEnd -> halfDiameter
        else -> 0.dp
    }

    Box(
        modifier = Modifier
            .size(diameter)
            .align(alignment)
            .offset(x = offsetX, y = offsetY)
            .blur(blurRadius)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent)
                ),
                shape = CircleShape
            )
    )
}

@Preview
@Composable
private fun GradientBackgroundPreview() {
    EchoListTheme {
        GradientBackground()
    }
}
