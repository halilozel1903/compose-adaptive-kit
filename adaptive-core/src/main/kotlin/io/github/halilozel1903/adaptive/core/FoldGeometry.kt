package io.github.halilozel1903.adaptive.core

/** A rectangle in any unit (window pixels for folds, dp for layouts). */
public data class Bounds(
    public val left: Float,
    public val top: Float,
    public val right: Float,
    public val bottom: Float,
) {
    init {
        require(right >= left && bottom >= top) { "Bounds must not be inverted: $this" }
    }

    public val width: Float get() = right - left
    public val height: Float get() = bottom - top

    /** These bounds moved by [dx] and [dy]. */
    public fun offset(dx: Float, dy: Float): Bounds = Bounds(left + dx, top + dy, right + dx, bottom + dy)

    /** These bounds with every edge divided by [divisor], for example window pixels to dp. */
    public fun scaledDown(divisor: Float): Bounds {
        require(divisor > 0f) { "divisor must be > 0, was $divisor" }
        return Bounds(left / divisor, top / divisor, right / divisor, bottom / divisor)
    }
}

/** The direction of the fold line. A [Vertical] fold splits the window into a left and a right side. */
public enum class FoldOrientation { Vertical, Horizontal }

/** How far a foldable is open. */
public enum class FoldState { Flat, HalfOpened }

/** Whether the fold hides content, like the physical hinge between two screens. */
public enum class FoldOcclusion { None, Full }

/**
 * How the device is held.
 *
 * - [Flat]: no fold, or a fold that is fully open.
 * - [Tabletop]: half opened with a horizontal fold, like a laptop. Content goes on top, controls below.
 * - [Book]: half opened with a vertical fold, like a book. Two pages side by side.
 */
public enum class Posture { Flat, Tabletop, Book }

/**
 * A fold or hinge, as reported by Jetpack WindowManager's `FoldingFeature`.
 *
 * @property bounds the fold in window pixels. Folds without a physical hinge have zero width or height.
 * @property isSeparating true when the fold splits the window into two areas that should not share content.
 */
public data class FoldInfo(
    public val bounds: Bounds,
    public val orientation: FoldOrientation,
    public val state: FoldState,
    public val occlusion: FoldOcclusion = FoldOcclusion.None,
    public val isSeparating: Boolean = FoldGeometry.isSeparating(state, occlusion),
)

/**
 * A separating hinge inside a container, in the container's own coordinates and unit.
 * For a [FoldOrientation.Vertical] hinge [start] and [end] are x positions, for a horizontal one y positions.
 */
public data class HingeSpan(
    public val orientation: FoldOrientation,
    public val start: Float,
    public val end: Float,
) {
    init {
        require(end >= start) { "end must be >= start: $this" }
    }

    /** The hinge's thickness; zero for a fold without a physical hinge. */
    public val size: Float get() = end - start

    /** The same hinge seen from the other side of a container [containerSize] long, for right-to-left layouts. */
    public fun mirrored(containerSize: Float): HingeSpan = HingeSpan(orientation, containerSize - end, containerSize - start)
}

/** Fold and hinge math: postures, where a hinge falls inside a layout and how to keep content off it. */
public object FoldGeometry {

    /** A fold separates the window when it is half opened or physically hides content (a hinge). */
    public fun isSeparating(state: FoldState, occlusion: FoldOcclusion): Boolean =
        state == FoldState.HalfOpened || occlusion == FoldOcclusion.Full

    /** The posture for [fold]; [Posture.Flat] without a fold or when it is fully open. */
    public fun posture(fold: FoldInfo?): Posture = when {
        fold == null || fold.state != FoldState.HalfOpened -> Posture.Flat
        fold.orientation == FoldOrientation.Horizontal -> Posture.Tabletop
        else -> Posture.Book
    }

    /**
     * Where [fold] crosses [container], in the container's coordinates divided by [scale]
     * (pass the screen density to turn window pixels into dp).
     *
     * Returns null when there is no fold, when [separatingOnly] and the fold does not separate, or when the
     * fold does not cut the container into two non-empty parts.
     */
    public fun hingeIn(
        fold: FoldInfo?,
        container: Bounds,
        scale: Float = 1f,
        separatingOnly: Boolean = true,
    ): HingeSpan? {
        if (fold == null || (separatingOnly && !fold.isSeparating)) return null
        require(scale > 0f) { "scale must be > 0, was $scale" }
        val f = fold.bounds
        return when (fold.orientation) {
            FoldOrientation.Vertical -> {
                // The fold must run through the container vertically and lie strictly inside it horizontally.
                if (f.bottom <= container.top || f.top >= container.bottom) return null
                if (f.left <= container.left || f.right >= container.right) return null
                HingeSpan(FoldOrientation.Vertical, (f.left - container.left) / scale, (f.right - container.left) / scale)
            }
            FoldOrientation.Horizontal -> {
                if (f.right <= container.left || f.left >= container.right) return null
                if (f.top <= container.top || f.bottom >= container.bottom) return null
                HingeSpan(FoldOrientation.Horizontal, (f.top - container.top) / scale, (f.bottom - container.top) / scale)
            }
        }
    }

    /**
     * The parts of a container [containerSize] long (along the hinge's axis) that are not covered by [hinge].
     * Without a hinge, the whole container.
     */
    public fun safeRegions(containerSize: Float, hinge: HingeSpan?): List<ClosedFloatingPointRange<Float>> {
        requireSize(containerSize, "containerSize")
        if (hinge == null) return listOf(0f..containerSize)
        val result = mutableListOf<ClosedFloatingPointRange<Float>>()
        val before = hinge.start.coerceIn(0f, containerSize)
        val after = hinge.end.coerceIn(0f, containerSize)
        if (before > 0f) result += 0f..before
        if (after < containerSize) result += after..containerSize
        return result
    }

    /** True when content from [start] spanning [size] would be cut by [hinge]. */
    public fun overlapsHinge(start: Float, size: Float, hinge: HingeSpan?): Boolean {
        if (hinge == null) return false
        return start < hinge.end && start + size > hinge.start
    }

    /**
     * Moves content from [start] spanning [size] off [hinge], to the nearest side where it fits inside a
     * container [containerSize] long. Returns the new start (unchanged when nothing overlaps). When the content
     * fits on neither side, it goes to the start of the larger side.
     */
    public fun avoidHinge(start: Float, size: Float, containerSize: Float, hinge: HingeSpan?): Float {
        requireSize(size, "size")
        requireSize(containerSize, "containerSize")
        if (!overlapsHinge(start, size, hinge)) return start
        hinge!!
        val fitsBefore = hinge.start >= size
        val fitsAfter = containerSize - hinge.end >= size
        val before = (hinge.start - size).coerceAtLeast(0f)
        val after = hinge.end
        return when {
            fitsBefore && fitsAfter -> if (start - before <= after - start) before else after
            fitsBefore -> before
            fitsAfter -> after
            hinge.start >= containerSize - hinge.end -> 0f
            else -> after
        }
    }
}
