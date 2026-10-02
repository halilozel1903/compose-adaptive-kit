package io.github.halilozel1903.adaptive.core

/** The panes of a list-detail layout. */
public enum class PaneRole { List, Detail, Extra }

/**
 * Which panes a list-detail layout shows, and what back does.
 *
 * On a single pane (compact) layout, selecting an item shows the detail and back returns to the list. With two or
 * three panes the list is always visible, so back is not handled and goes to the system (or the next handler).
 */
public object ListDetailNavigation {

    /** The panes shown for [mode], in start to end order. */
    public fun visiblePanes(mode: PaneMode, hasSelection: Boolean, hasExtra: Boolean = false): List<PaneRole> =
        when (mode) {
            PaneMode.Single -> if (hasSelection) listOf(PaneRole.Detail) else listOf(PaneRole.List)
            PaneMode.ListDetail -> listOf(PaneRole.List, PaneRole.Detail)
            PaneMode.ThreePane ->
                if (hasExtra && hasSelection) {
                    listOf(PaneRole.List, PaneRole.Detail, PaneRole.Extra)
                } else {
                    listOf(PaneRole.List, PaneRole.Detail)
                }
        }

    /** True when back should close the detail and show the list (single pane with a selection). */
    public fun handlesBack(mode: PaneMode, hasSelection: Boolean): Boolean = mode == PaneMode.Single && hasSelection

    /** The pane shown after back, or null when back is not handled by the list-detail layout. */
    public fun paneAfterBack(mode: PaneMode, hasSelection: Boolean): PaneRole? =
        if (handlesBack(mode, hasSelection)) PaneRole.List else null
}
