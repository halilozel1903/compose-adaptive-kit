package io.github.halilozel1903.adaptive.core

/** How two panes share a container: [SideBySide] (columns) or [Stacked] (rows). */
public enum class SplitDirection { SideBySide, Stacked }

/**
 * Sizes of a two pane split along [direction]: the first pane, the gap (the hinge or the spacing) and the second
 * pane. [alignedToHinge] is true when the split follows a separating hinge.
 */
public data class SplitResult(
    public val direction: SplitDirection,
    public val firstSize: Float,
    public val gap: Float,
    public val secondSize: Float,
    public val alignedToHinge: Boolean,
)

/** Splits a container in two, exactly at a separating hinge when there is one. */
public object HingeSplit {

    /**
     * Splits a [width] by [height] container.
     *
     * With a [hinge] the panes end exactly at its edges: a vertical hinge gives two panes side by side, a
     * horizontal hinge (tabletop) two stacked panes, and the gap is the hinge itself. Without one,
     * [fallbackDirection] and [ratio] (the first pane's share of the space left after [spacing]) decide.
     */
    public fun compute(
        width: Float,
        height: Float,
        hinge: HingeSpan?,
        fallbackDirection: SplitDirection = SplitDirection.SideBySide,
        ratio: Float = 0.5f,
        spacing: Float = 0f,
    ): SplitResult {
        requireSize(width, "width")
        requireSize(height, "height")
        require(ratio in 0f..1f) { "ratio must be in 0..1, was $ratio" }
        requireSize(spacing, "spacing")
        if (hinge != null) {
            val direction = if (hinge.orientation == FoldOrientation.Vertical) SplitDirection.SideBySide else SplitDirection.Stacked
            val total = if (direction == SplitDirection.SideBySide) width else height
            val first = hinge.start.coerceIn(0f, total)
            val end = hinge.end.coerceIn(first, total)
            return SplitResult(direction, first, end - first, total - end, alignedToHinge = true)
        }
        val total = if (fallbackDirection == SplitDirection.SideBySide) width else height
        val gap = spacing.coerceAtMost(total)
        val available = total - gap
        val first = available * ratio
        return SplitResult(fallbackDirection, first, gap, available - first, alignedToHinge = false)
    }
}
