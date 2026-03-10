package net.onefivefour.echolist.ui.common

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LookaheadScope

enum class ExpandableBoxState {
    COLLAPSED,
    EXPANDED
}

private val ExpandableBoxBoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = Rect.VisibilityThreshold
    )
}

/**
 * A composable that animates its bounds within a [LookaheadScope] when its size changes.
 * Sizing (e.g. weight, fillMaxWidth) should be controlled by the caller via [modifier].
 *
 * @param state Whether this box is currently [ExpandableBoxState.EXPANDED] or [ExpandableBoxState.COLLAPSED].
 * @param onClick Called when the box is clicked (typically to toggle state).
 * @param lookaheadScope The enclosing [LookaheadScope] used to drive bounds animations.
 * @param modifier Modifier applied to the outer Box — use this to control sizing (weight, fillMaxWidth, etc.).
 * @param content The content to display inside the box.
 */
@Composable
fun ExpandableBox(
    state: ExpandableBoxState,
    onClick: () -> Unit,
    lookaheadScope: LookaheadScope,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .animateBounds(
                lookaheadScope = lookaheadScope,
                boundsTransform = ExpandableBoxBoundsTransform
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
