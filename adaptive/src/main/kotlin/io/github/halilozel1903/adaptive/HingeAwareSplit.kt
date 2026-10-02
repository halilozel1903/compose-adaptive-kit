package io.github.halilozel1903.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds
import io.github.halilozel1903.adaptive.core.FoldGeometry
import io.github.halilozel1903.adaptive.core.HingeSplit
import io.github.halilozel1903.adaptive.core.SplitDirection
import kotlin.math.roundToInt

/**
 * Two panes that split exactly at a separating hinge or fold: [first] ends where the hinge starts and [second]
 * starts where it ends, so no content is drawn under a physical hinge or across a half opened fold.
 *
 * A vertical hinge (book posture, dual screens) puts the panes side by side, a horizontal one (tabletop) stacks
 * them. Without a separating hinge, [fallbackDirection], [fallbackRatio] and [spacing] decide.
 *
 * ```
 * HingeAwareSplit(
 *     first = { VideoPlayer() },
 *     second = { PlayerControls() },
 *     fallbackDirection = SplitDirection.Stacked,
 *     fallbackRatio = 0.6f,
 * )
 * ```
 *
 * The hinge is placed in physical (left to right) coordinates, so with a hinge [first] is always on the left (or
 * top); without one, the fallback split follows the layout direction.
 */
@Composable
public fun HingeAwareSplit(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fallbackDirection: SplitDirection = SplitDirection.SideBySide,
    fallbackRatio: Float = 0.5f,
    spacing: Dp = 0.dp,
    windowInfo: AdaptiveWindowInfo = rememberWindowLayoutInfo(),
) {
    require(fallbackRatio in 0f..1f) { "fallbackRatio must be in 0..1, was $fallbackRatio" }
    var container by remember { mutableStateOf<Bounds?>(null) }
    Layout(
        content = {
            Box(propagateMinConstraints = true) { first() }
            Box(propagateMinConstraints = true) { second() }
        },
        modifier = modifier.onWindowBounds { container = it },
    ) { measurables, constraints ->
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else constraints.minHeight
        // Read in the measure block: when the position in the window changes, only the layout reruns.
        val bounds = container
        val hinge = if (bounds == null) null else FoldGeometry.hingeIn(windowInfo.fold, bounds)
        val split = HingeSplit.compute(
            width = width.toFloat(),
            height = height.toFloat(),
            hinge = hinge,
            fallbackDirection = fallbackDirection,
            ratio = fallbackRatio,
            spacing = spacing.toPx(),
        )
        val total = if (split.direction == SplitDirection.SideBySide) width else height
        val firstSize = split.firstSize.roundToInt().coerceIn(0, total)
        val gap = split.gap.roundToInt().coerceIn(0, total - firstSize)
        val secondSize = (total - firstSize - gap).coerceAtLeast(0)
        val sideBySide = split.direction == SplitDirection.SideBySide
        val firstPlaceable = measurables[0].measure(
            if (sideBySide) Constraints.fixed(firstSize, height) else Constraints.fixed(width, firstSize),
        )
        val secondPlaceable = measurables[1].measure(
            if (sideBySide) Constraints.fixed(secondSize, height) else Constraints.fixed(width, secondSize),
        )
        layout(width, height) {
            val offset = firstSize + gap
            if (sideBySide) {
                if (split.alignedToHinge) {
                    // Physical coordinates: the hinge does not move in right-to-left layouts.
                    firstPlaceable.place(0, 0)
                    secondPlaceable.place(offset, 0)
                } else {
                    firstPlaceable.placeRelative(0, 0)
                    secondPlaceable.placeRelative(offset, 0)
                }
            } else {
                firstPlaceable.place(0, 0)
                secondPlaceable.place(0, offset)
            }
        }
    }
}
