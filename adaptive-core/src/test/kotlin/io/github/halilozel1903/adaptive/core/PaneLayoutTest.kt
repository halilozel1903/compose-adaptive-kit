package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaneLayoutTest {

    @Test
    fun compactIsSinglePane() {
        val layout = PaneLayoutCalculator.compute(412f, hasExtra = true)
        assertEquals(PaneMode.Single, layout.mode)
        assertEquals(1, layout.paneCount)
        assertEquals(412f, layout.listWidth)
        assertEquals(412f, layout.detailWidth)
    }

    @Test
    fun mediumWithoutRoomForBothMinimumsIsSinglePane() {
        assertEquals(PaneMode.Single, PaneLayoutCalculator.compute(620f).mode)
        assertEquals(PaneMode.ListDetail, PaneLayoutCalculator.compute(640f).mode)
    }

    @Test
    fun listDetailUsesRatioWithinLimits() {
        val layout = PaneLayoutCalculator.compute(1000f, PaneConfig(spacing = 16f))
        assertEquals(PaneMode.ListDetail, layout.mode)
        assertEquals(320f, layout.listWidth, 0.001f)
        assertEquals(664f, layout.detailWidth, 0.001f)
        assertEquals(1000f, layout.listWidth + layout.spacing + layout.detailWidth, 0.001f)

        // The list never grows past maxListWidth.
        assertEquals(420f, PaneLayoutCalculator.compute(2000f).listWidth)
    }

    @Test
    fun detailKeepsItsMinimumWidth() {
        val layout = PaneLayoutCalculator.compute(660f, PaneConfig(listRatio = 0.6f))
        assertEquals(300f, layout.listWidth, 0.001f)
        assertEquals(360f, layout.detailWidth, 0.001f)
    }

    @Test
    fun threePanesNeedAnExtraPaneAndRoom() {
        assertEquals(PaneMode.ListDetail, PaneLayoutCalculator.compute(1280f, hasExtra = false).mode)
        assertEquals(PaneMode.ListDetail, PaneLayoutCalculator.compute(880f, hasExtra = true).mode)
        assertEquals(PaneMode.ListDetail, PaneLayoutCalculator.compute(1280f, PaneConfig(allowThreePane = false), hasExtra = true).mode)

        val layout = PaneLayoutCalculator.compute(1280f, hasExtra = true)
        assertEquals(PaneMode.ThreePane, layout.mode)
        assertEquals(3, layout.paneCount)
        assertEquals(1280f, layout.listWidth + layout.detailWidth + layout.extraWidth, 0.01f)
        assertTrue(layout.detailWidth >= 360f)
    }

    @Test
    fun threePaneShrinksExtraThenListForTheDetail() {
        val config = PaneConfig(spacing = 12f)
        val layout = PaneLayoutCalculator.compute(1016f, config, hasExtra = true)
        assertEquals(PaneMode.ThreePane, layout.mode)
        assertTrue(layout.detailWidth >= 360f)
        assertEquals(1016f, layout.listWidth + layout.detailWidth + layout.extraWidth + 2 * layout.spacing, 0.01f)

        // Exactly the minimums: every pane at its minimum.
        val tight = PaneLayoutCalculator.compute(924f, PaneConfig(spacing = 12f, listRatio = 0.5f, extraRatio = 0.5f), hasExtra = true)
        assertEquals(280f, tight.listWidth, 0.01f)
        assertEquals(360f, tight.detailWidth, 0.01f)
        assertEquals(260f, tight.extraWidth, 0.01f)
    }

    @Test
    fun separatingHingeSplitsListAndDetail() {
        val hinge = HingeSpan(FoldOrientation.Vertical, 420f, 440f)
        val layout = PaneLayoutCalculator.compute(860f, hasExtra = true, hinge = hinge)
        assertEquals(PaneMode.ListDetail, layout.mode)
        assertTrue(layout.splitAtHinge)
        assertEquals(420f, layout.listWidth)
        assertEquals(20f, layout.spacing)
        assertEquals(420f, layout.detailWidth)

        // Even on a narrow window, the hinge decides: no content goes under it.
        val narrow = PaneLayoutCalculator.compute(540f, hinge = HingeSpan(FoldOrientation.Vertical, 270f, 270f))
        assertEquals(PaneMode.ListDetail, narrow.mode)
    }

    @Test
    fun hingeNearTheEdgeOrHorizontalIsIgnored() {
        val nearEdge = PaneLayoutCalculator.compute(1000f, hinge = HingeSpan(FoldOrientation.Vertical, 100f, 120f))
        assertEquals(false, nearEdge.splitAtHinge)
        val tabletop = PaneLayoutCalculator.compute(412f, hinge = HingeSpan(FoldOrientation.Horizontal, 400f, 420f))
        assertEquals(PaneMode.Single, tabletop.mode)
    }

    @Test
    fun rejectsInvalidConfig() {
        assertFailsWith<IllegalArgumentException> { PaneConfig(listRatio = 2f) }
        assertFailsWith<IllegalArgumentException> { PaneConfig(minListWidth = 500f, maxListWidth = 400f) }
        assertFailsWith<IllegalArgumentException> { PaneConfig(spacing = -1f) }
    }
}
