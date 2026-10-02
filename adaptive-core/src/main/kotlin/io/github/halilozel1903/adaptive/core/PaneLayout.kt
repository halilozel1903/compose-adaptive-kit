package io.github.halilozel1903.adaptive.core

/** How many panes a list-detail layout shows. */
public enum class PaneMode {
    /** One pane at a time: the list, or the detail when something is selected. */
    Single,

    /** The list and the detail side by side. */
    ListDetail,

    /** The list, the detail and an extra (supporting) pane for the selected item. */
    ThreePane,
}

/**
 * Rules for [PaneLayoutCalculator]. All sizes are in dp.
 *
 * @property listRatio the list's share of the width, kept between [minListWidth] and [maxListWidth].
 * @property extraRatio the extra pane's share of the width in three pane mode.
 * @property minDetailWidth the narrowest detail pane worth showing next to the list.
 * @property spacing the gap between panes.
 * @property singlePaneBelowWidth containers narrower than this always show one pane (Material 3 compact).
 * @property allowThreePane set to false to never show the extra pane next to the other two.
 * @property minHingePaneWidth a separating vertical hinge splits the panes only when both sides are at least this wide.
 */
public data class PaneConfig(
    public val listRatio: Float = 0.32f,
    public val extraRatio: Float = 0.26f,
    public val minListWidth: Float = 280f,
    public val maxListWidth: Float = 420f,
    public val minDetailWidth: Float = 360f,
    public val minExtraWidth: Float = 260f,
    public val maxExtraWidth: Float = 400f,
    public val spacing: Float = 0f,
    public val singlePaneBelowWidth: Float = WindowWidthClass.Medium.minWidthDp,
    public val allowThreePane: Boolean = true,
    public val minHingePaneWidth: Float = 200f,
) {
    init {
        require(listRatio in 0f..1f) { "listRatio must be in 0..1, was $listRatio" }
        require(extraRatio in 0f..1f) { "extraRatio must be in 0..1, was $extraRatio" }
        requireSize(minListWidth, "minListWidth")
        require(maxListWidth >= minListWidth) { "maxListWidth must be >= minListWidth" }
        requireSize(minDetailWidth, "minDetailWidth")
        requireSize(minExtraWidth, "minExtraWidth")
        require(maxExtraWidth >= minExtraWidth) { "maxExtraWidth must be >= minExtraWidth" }
        requireSize(spacing, "spacing")
        requireSize(singlePaneBelowWidth, "singlePaneBelowWidth")
        requireSize(minHingePaneWidth, "minHingePaneWidth")
    }
}

/**
 * The widths of the panes of a list-detail layout, in dp. In [PaneMode.Single] the visible pane takes the whole
 * width; panes that are not shown are 0 wide.
 *
 * @property splitAtHinge true when the list ends at a separating hinge and the detail starts after it.
 */
public data class PaneLayout(
    public val mode: PaneMode,
    public val totalWidth: Float,
    public val listWidth: Float,
    public val detailWidth: Float,
    public val extraWidth: Float,
    public val spacing: Float,
    public val splitAtHinge: Boolean = false,
) {
    /** The number of panes shown at once. */
    public val paneCount: Int
        get() = when (mode) {
            PaneMode.Single -> 1
            PaneMode.ListDetail -> 2
            PaneMode.ThreePane -> 3
        }

    public companion object {
        /** A single pane layout [width] wide. */
        public fun single(width: Float): PaneLayout = PaneLayout(PaneMode.Single, width, width, width, 0f, 0f)
    }
}

/** Decides between one, two and three panes and sizes them. */
public object PaneLayoutCalculator {

    /**
     * Lays out a list-detail container [width] dp wide.
     *
     * - A separating vertical [hinge] with room on both sides splits list and detail exactly at the hinge.
     * - Below [PaneConfig.singlePaneBelowWidth], or without room for both minimum widths, one pane.
     * - With [hasExtra] and room for three minimum widths, three panes.
     * - Otherwise list and detail.
     */
    public fun compute(
        width: Float,
        config: PaneConfig = PaneConfig(),
        hasExtra: Boolean = false,
        hinge: HingeSpan? = null,
    ): PaneLayout {
        requireSize(width, "width")
        if (hinge != null && hinge.orientation == FoldOrientation.Vertical) {
            val list = hinge.start
            val detail = width - hinge.end
            if (list >= config.minHingePaneWidth && detail >= config.minHingePaneWidth) {
                return PaneLayout(PaneMode.ListDetail, width, list, detail, 0f, hinge.size, splitAtHinge = true)
            }
        }
        val s = config.spacing
        if (width < config.singlePaneBelowWidth || width < config.minListWidth + s + config.minDetailWidth) {
            return PaneLayout.single(width)
        }
        val threeFits = width >= config.minListWidth + config.minDetailWidth + config.minExtraWidth + 2 * s
        if (hasExtra && config.allowThreePane && threeFits) {
            var extra = (width * config.extraRatio).coerceIn(config.minExtraWidth, config.maxExtraWidth)
            var list = (width * config.listRatio).coerceIn(config.minListWidth, config.maxListWidth)
            var detail = width - list - extra - 2 * s
            if (detail < config.minDetailWidth) {
                // Give the detail its minimum: take from the extra pane first, then from the list.
                var deficit = config.minDetailWidth - detail
                val fromExtra = minOf(deficit, extra - config.minExtraWidth)
                extra -= fromExtra
                deficit -= fromExtra
                list -= minOf(deficit, list - config.minListWidth)
                detail = width - list - extra - 2 * s
            }
            return PaneLayout(PaneMode.ThreePane, width, list, detail, extra, s)
        }
        var list = (width * config.listRatio).coerceIn(config.minListWidth, config.maxListWidth)
        if (width - list - s < config.minDetailWidth) {
            list = maxOf(config.minListWidth, width - s - config.minDetailWidth)
        }
        return PaneLayout(PaneMode.ListDetail, width, list, width - list - s, 0f, s)
    }
}
