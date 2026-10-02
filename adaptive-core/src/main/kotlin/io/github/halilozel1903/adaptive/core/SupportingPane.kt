package io.github.halilozel1903.adaptive.core

/** Where the supporting pane goes relative to the main pane. */
public enum class SupportingArrangement {
    /** Only the main pane (no room, or the supporting pane is hidden). */
    MainOnly,

    /** The supporting pane at the end of the main pane. */
    SideBySide,

    /** The supporting pane below the main pane. */
    Stacked,
}

/**
 * Rules for [SupportingPaneCalculator]. All sizes are in dp.
 *
 * @property supportingRatio the supporting pane's share of the width when side by side.
 * @property stackedSupportingRatio the supporting pane's share of the height when stacked.
 * @property minHingePaneSize a separating hinge splits the panes only when both sides are at least this large.
 */
public data class SupportingPaneConfig(
    public val supportingRatio: Float = 0.33f,
    public val minMainWidth: Float = 360f,
    public val minSupportingWidth: Float = 280f,
    public val maxSupportingWidth: Float = 420f,
    public val stackedSupportingRatio: Float = 0.4f,
    public val minStackedMainHeight: Float = 240f,
    public val minStackedSupportingHeight: Float = 160f,
    public val spacing: Float = 0f,
    public val minHingePaneSize: Float = 200f,
) {
    init {
        require(supportingRatio in 0f..1f) { "supportingRatio must be in 0..1, was $supportingRatio" }
        require(stackedSupportingRatio in 0f..1f) { "stackedSupportingRatio must be in 0..1, was $stackedSupportingRatio" }
        requireSize(minMainWidth, "minMainWidth")
        requireSize(minSupportingWidth, "minSupportingWidth")
        require(maxSupportingWidth >= minSupportingWidth) { "maxSupportingWidth must be >= minSupportingWidth" }
        requireSize(minStackedMainHeight, "minStackedMainHeight")
        requireSize(minStackedSupportingHeight, "minStackedSupportingHeight")
        requireSize(spacing, "spacing")
        requireSize(minHingePaneSize, "minHingePaneSize")
    }
}

/**
 * The result of [SupportingPaneCalculator.compute]. [mainSize] and [supportingSize] are widths when side by
 * side and heights when stacked. With [SupportingArrangement.MainOnly] the main pane fills the container.
 */
public data class SupportingPanePlacement(
    public val arrangement: SupportingArrangement,
    public val mainSize: Float,
    public val supportingSize: Float,
    public val spacing: Float,
    public val splitAtHinge: Boolean = false,
)

/** Places a main pane and a supporting pane (Material 3 supporting pane layout). */
public object SupportingPaneCalculator {

    /**
     * Places the panes in a [width] by [height] container.
     *
     * - A separating [hinge] with room on both sides splits exactly at it: side by side for a vertical hinge
     *   (book), stacked for a horizontal one (tabletop: main on top, supporting below).
     * - Side by side when the width fits both minimum widths.
     * - Stacked when the height fits both minimum heights.
     * - Otherwise main only.
     */
    public fun compute(
        width: Float,
        height: Float,
        config: SupportingPaneConfig = SupportingPaneConfig(),
        hinge: HingeSpan? = null,
        showSupporting: Boolean = true,
    ): SupportingPanePlacement {
        requireSize(width, "width")
        requireSize(height, "height")
        if (!showSupporting) return mainOnly(width)
        if (hinge != null) {
            val total = if (hinge.orientation == FoldOrientation.Vertical) width else height
            val main = hinge.start
            val supporting = total - hinge.end
            if (main >= config.minHingePaneSize && supporting >= config.minHingePaneSize) {
                val arrangement = if (hinge.orientation == FoldOrientation.Vertical) {
                    SupportingArrangement.SideBySide
                } else {
                    SupportingArrangement.Stacked
                }
                return SupportingPanePlacement(arrangement, main, supporting, hinge.size, splitAtHinge = true)
            }
        }
        val s = config.spacing
        if (width >= config.minMainWidth + config.minSupportingWidth + s) {
            var supporting = (width * config.supportingRatio).coerceIn(config.minSupportingWidth, config.maxSupportingWidth)
            if (width - supporting - s < config.minMainWidth) {
                supporting = maxOf(config.minSupportingWidth, width - s - config.minMainWidth)
            }
            return SupportingPanePlacement(SupportingArrangement.SideBySide, width - supporting - s, supporting, s)
        }
        if (height >= config.minStackedMainHeight + config.minStackedSupportingHeight + s) {
            var supporting = maxOf(config.minStackedSupportingHeight, (height - s) * config.stackedSupportingRatio)
            if (height - s - supporting < config.minStackedMainHeight) {
                supporting = height - s - config.minStackedMainHeight
            }
            return SupportingPanePlacement(SupportingArrangement.Stacked, height - s - supporting, supporting, s)
        }
        return mainOnly(width)
    }

    private fun mainOnly(width: Float) = SupportingPanePlacement(SupportingArrangement.MainOnly, width, 0f, 0f)
}
