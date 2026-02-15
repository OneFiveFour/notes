package net.onefivefour.notes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf

internal fun material3Shapes() = Shapes(
    small = RoundedCornerShape(Dimensions().s),
    medium = RoundedCornerShape(Dimensions().m)
)

internal val LocalShapes = staticCompositionLocalOf { material3Shapes() }
