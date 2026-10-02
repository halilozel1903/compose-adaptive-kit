package io.github.halilozel1903.adaptive.core

/**
 * Material 3 window width classes. [minWidthDp] is the inclusive lower bound of each class:
 * Compact below 600 dp, Medium from 600, Expanded from 840, Large from 1200 and Extra-large from 1600.
 */
public enum class WindowWidthClass(public val minWidthDp: Float) {
    Compact(0f),
    Medium(600f),
    Expanded(840f),
    Large(1200f),
    ExtraLarge(1600f),
    ;

    public companion object {
        /** The class of a window [widthDp] wide. */
        public fun fromWidth(widthDp: Float): WindowWidthClass {
            requireSize(widthDp, "widthDp")
            return entries.last { widthDp >= it.minWidthDp }
        }
    }
}

/**
 * Material 3 window height classes. [minHeightDp] is the inclusive lower bound of each class:
 * Compact below 480 dp, Medium from 480 and Expanded from 900.
 */
public enum class WindowHeightClass(public val minHeightDp: Float) {
    Compact(0f),
    Medium(480f),
    Expanded(900f),
    ;

    public companion object {
        /** The class of a window [heightDp] tall. */
        public fun fromHeight(heightDp: Float): WindowHeightClass {
            requireSize(heightDp, "heightDp")
            return entries.last { heightDp >= it.minHeightDp }
        }
    }
}

/**
 * The size of a window in dp with its Material 3 width and height classes.
 *
 * ```
 * val sizeClass = WindowSizeClass(widthDp = 1280f, heightDp = 800f)
 * sizeClass.widthClass                                   // Large
 * sizeClass.isWidthAtLeast(WindowWidthClass.Expanded)    // true
 * ```
 */
public data class WindowSizeClass(
    public val widthDp: Float,
    public val heightDp: Float,
) {
    init {
        requireSize(widthDp, "widthDp")
        requireSize(heightDp, "heightDp")
    }

    /** The width class, from Compact to Extra-large. */
    public val widthClass: WindowWidthClass get() = WindowWidthClass.fromWidth(widthDp)

    /** The height class, from Compact to Expanded. */
    public val heightClass: WindowHeightClass get() = WindowHeightClass.fromHeight(heightDp)

    /** True when the window is wider than it is tall. */
    public val isLandscape: Boolean get() = widthDp > heightDp

    /** True when the width class is [widthClass] or larger. */
    public fun isWidthAtLeast(widthClass: WindowWidthClass): Boolean = this.widthClass >= widthClass

    /** True when the height class is [heightClass] or larger. */
    public fun isHeightAtLeast(heightClass: WindowHeightClass): Boolean = this.heightClass >= heightClass

    override fun toString(): String = "WindowSizeClass($widthClass x $heightClass, ${widthDp}x$heightDp dp)"
}

internal fun requireSize(value: Float, name: String) {
    require(!value.isNaN() && value >= 0f && value != Float.POSITIVE_INFINITY) {
        "$name must be a finite value >= 0, was $value"
    }
}
