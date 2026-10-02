package io.github.halilozel1903.adaptive.core

/** The screen edge a back gesture started from. */
public enum class SwipeEdge { Left, Right }

/**
 * How the detail pane looks at some point of a predictive back gesture.
 *
 * @property translationX horizontal shift, away from the edge the gesture started from.
 * @property scale scale of the detail pane, from 1 down to [PredictiveBackMath.MIN_SCALE].
 * @property scrimAlpha alpha of the scrim over the list revealed behind it.
 * @property cornerFraction 0..1, how rounded the detail pane's corners are.
 */
public data class BackTransform(
    public val translationX: Float,
    public val scale: Float,
    public val scrimAlpha: Float,
    public val cornerFraction: Float,
)

/** The Material 3 style shrink and shift of a pane while a predictive back gesture is in progress. */
public object PredictiveBackMath {
    /** The smallest scale, reached at the end of the gesture. */
    public const val MIN_SCALE: Float = 0.9f

    /** The scrim alpha over the list when the gesture starts. */
    public const val MAX_SCRIM_ALPHA: Float = 0.32f

    /**
     * The transform at [progress] (0..1, clamped) of a gesture from [edge] over a pane [width] wide.
     * [maxShiftFraction] is the largest shift as a share of the width.
     */
    public fun transform(
        progress: Float,
        edge: SwipeEdge,
        width: Float,
        maxShiftFraction: Float = 0.08f,
    ): BackTransform {
        val p = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
        // Decelerate: most of the movement happens early in the gesture, like the system animation.
        val eased = 1f - (1f - p) * (1f - p)
        val direction = if (edge == SwipeEdge.Left) 1f else -1f
        return BackTransform(
            translationX = direction * width * maxShiftFraction * eased,
            scale = 1f - (1f - MIN_SCALE) * eased,
            scrimAlpha = if (p == 0f) 0f else MAX_SCRIM_ALPHA * (1f - eased),
            cornerFraction = eased,
        )
    }
}
