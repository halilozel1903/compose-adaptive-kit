package io.github.halilozel1903.adaptive.core

/** The navigation component that fits a window. */
public enum class NavigationType {
    /** A bottom navigation bar, for compact widths (phones in portrait). */
    BottomBar,

    /** A navigation rail at the start, for medium widths and short windows (phones in landscape). */
    Rail,

    /** A permanent navigation drawer, for expanded widths and larger (tablets in landscape, desktops). */
    Drawer,
    ;

    public companion object {
        /** The navigation type Material 3 recommends for [sizeClass]. */
        public fun forSizeClass(sizeClass: WindowSizeClass): NavigationType = when {
            sizeClass.widthClass == WindowWidthClass.Compact -> BottomBar
            sizeClass.heightClass == WindowHeightClass.Compact -> Rail
            sizeClass.widthClass == WindowWidthClass.Medium -> Rail
            else -> Drawer
        }
    }
}
