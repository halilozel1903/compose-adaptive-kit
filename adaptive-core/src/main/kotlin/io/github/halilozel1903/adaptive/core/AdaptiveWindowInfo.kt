package io.github.halilozel1903.adaptive.core

import kotlin.math.roundToInt

/**
 * Everything an adaptive layout needs to know about the window: its size class, the screen density and the fold
 * (if any).
 *
 * @property density pixels per dp, used to turn the fold's window pixels into dp.
 * @property fold the first fold of the window in window pixels, or null on devices without one.
 */
public data class AdaptiveWindowInfo(
    public val sizeClass: WindowSizeClass,
    public val density: Float = 1f,
    public val fold: FoldInfo? = null,
) {
    init {
        require(density > 0f) { "density must be > 0, was $density" }
    }

    /** How the device is held. */
    public val posture: Posture get() = FoldGeometry.posture(fold)

    /** True when a fold splits the window into two areas (half opened, or a physical hinge). */
    public val hasSeparatingFold: Boolean get() = fold?.isSeparating == true

    /** The navigation type Material 3 recommends for this window. */
    public val navigationType: NavigationType get() = NavigationType.forSizeClass(sizeClass)

    /** The fold in dp, or null. */
    public val foldBoundsDp: Bounds? get() = fold?.bounds?.scaledDown(density)

    /**
     * A short description for debug overlays and logs, for example
     * `Large x Medium · 1280x800 dp · Flat` or `Expanded x Medium · 841x701 dp · Book · vertical hinge, separating`.
     */
    public fun label(): String = buildString {
        append(sizeClass.widthClass.name).append(" x ").append(sizeClass.heightClass.name)
        append(" · ").append(sizeClass.widthDp.roundToInt()).append('x').append(sizeClass.heightDp.roundToInt()).append(" dp")
        append(" · ").append(posture.name)
        val f = fold
        if (f != null) {
            append(" · ").append(f.orientation.name.lowercase())
            append(if (f.occlusion == FoldOcclusion.Full) " hinge" else " fold")
            append(if (f.isSeparating) ", separating" else ", not separating")
        }
    }

    public companion object {
        /** Window info for a window [widthPx] by [heightPx] pixels at [density] pixels per dp. */
        public fun fromPixels(widthPx: Int, heightPx: Int, density: Float, fold: FoldInfo? = null): AdaptiveWindowInfo {
            require(density > 0f) { "density must be > 0, was $density" }
            return AdaptiveWindowInfo(
                sizeClass = WindowSizeClass(widthPx.coerceAtLeast(0) / density, heightPx.coerceAtLeast(0) / density),
                density = density,
                fold = fold,
            )
        }
    }
}
