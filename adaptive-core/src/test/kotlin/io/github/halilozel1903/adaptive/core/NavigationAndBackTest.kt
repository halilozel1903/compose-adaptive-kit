package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationAndBackTest {

    @Test
    fun singlePaneShowsListOrDetail() {
        assertEquals(listOf(PaneRole.List), ListDetailNavigation.visiblePanes(PaneMode.Single, hasSelection = false))
        assertEquals(listOf(PaneRole.Detail), ListDetailNavigation.visiblePanes(PaneMode.Single, hasSelection = true, hasExtra = true))
    }

    @Test
    fun multiPaneAlwaysShowsTheList() {
        assertEquals(listOf(PaneRole.List, PaneRole.Detail), ListDetailNavigation.visiblePanes(PaneMode.ListDetail, hasSelection = false))
        assertEquals(
            listOf(PaneRole.List, PaneRole.Detail, PaneRole.Extra),
            ListDetailNavigation.visiblePanes(PaneMode.ThreePane, hasSelection = true, hasExtra = true),
        )
        assertEquals(listOf(PaneRole.List, PaneRole.Detail), ListDetailNavigation.visiblePanes(PaneMode.ThreePane, hasSelection = false, hasExtra = true))
    }

    @Test
    fun backIsHandledOnlyOnASinglePaneDetail() {
        assertTrue(ListDetailNavigation.handlesBack(PaneMode.Single, hasSelection = true))
        assertEquals(PaneRole.List, ListDetailNavigation.paneAfterBack(PaneMode.Single, hasSelection = true))
        assertFalse(ListDetailNavigation.handlesBack(PaneMode.Single, hasSelection = false))
        assertFalse(ListDetailNavigation.handlesBack(PaneMode.ListDetail, hasSelection = true))
        assertNull(ListDetailNavigation.paneAfterBack(PaneMode.ThreePane, hasSelection = true))
    }

    @Test
    fun predictiveBackTransform() {
        val start = PredictiveBackMath.transform(0f, SwipeEdge.Left, 400f)
        assertEquals(BackTransform(0f, 1f, 0f, 0f), start)

        val end = PredictiveBackMath.transform(1f, SwipeEdge.Left, 400f)
        assertEquals(32f, end.translationX, 0.001f)
        assertEquals(PredictiveBackMath.MIN_SCALE, end.scale, 0.001f)
        assertEquals(0f, end.scrimAlpha, 0.001f)
        assertEquals(1f, end.cornerFraction, 0.001f)

        val fromRight = PredictiveBackMath.transform(0.5f, SwipeEdge.Right, 400f)
        assertTrue(fromRight.translationX < 0f)
        assertTrue(fromRight.scale in PredictiveBackMath.MIN_SCALE..1f)
        assertTrue(fromRight.scrimAlpha > 0f)

        // Out of range progress is clamped.
        assertEquals(end, PredictiveBackMath.transform(3f, SwipeEdge.Left, 400f))
        assertEquals(start, PredictiveBackMath.transform(Float.NaN, SwipeEdge.Left, 400f))
    }

    @Test
    fun windowInfoLabel() {
        val tablet = AdaptiveWindowInfo.fromPixels(2560, 1600, density = 2f)
        assertEquals("Large x Medium · 1280x800 dp · Flat", tablet.label())
        assertEquals(NavigationType.Drawer, tablet.navigationType)
        assertFalse(tablet.hasSeparatingFold)

        val fold = FoldInfo(Bounds(1103f, 0f, 1103f, 1840f), FoldOrientation.Vertical, FoldState.HalfOpened)
        val foldable = AdaptiveWindowInfo.fromPixels(2208, 1840, density = 2.625f, fold = fold)
        assertEquals("Expanded x Medium · 841x701 dp · Book · vertical fold, separating", foldable.label())
        assertTrue(foldable.hasSeparatingFold)
        assertEquals(Posture.Book, foldable.posture)
        assertEquals(1103f / 2.625f, foldable.foldBoundsDp!!.left, 0.001f)
    }
}
