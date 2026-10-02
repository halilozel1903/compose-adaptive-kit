package io.github.halilozel1903.adaptive

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.LayoutDirection
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds
import io.github.halilozel1903.adaptive.core.FoldGeometry
import io.github.halilozel1903.adaptive.core.FoldOrientation
import io.github.halilozel1903.adaptive.core.HingeSpan

/** Reports this layout's bounds in window pixels, the space folds are reported in. */
internal fun Modifier.onWindowBounds(onBounds: (Bounds) -> Unit): Modifier = onGloballyPositioned { coordinates ->
    val position = coordinates.positionInWindow()
    onBounds(
        Bounds(
            left = position.x,
            top = position.y,
            right = position.x + coordinates.size.width,
            bottom = position.y + coordinates.size.height,
        ),
    )
}

/**
 * The separating hinge inside [container] (window pixels), divided by [scale], mirrored for right-to-left layouts
 * so that "start" matches where Row and Column place the first child.
 */
internal fun AdaptiveWindowInfo.hingeIn(container: Bounds?, scale: Float, layoutDirection: LayoutDirection): HingeSpan? {
    if (container == null) return null
    val span = FoldGeometry.hingeIn(fold, container, scale) ?: return null
    return if (span.orientation == FoldOrientation.Vertical && layoutDirection == LayoutDirection.Rtl) {
        span.mirrored(container.width / scale)
    } else {
        span
    }
}
