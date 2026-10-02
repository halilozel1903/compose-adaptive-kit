package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SupportingPaneTest {

    @Test
    fun wideContainerIsSideBySide() {
        val placement = SupportingPaneCalculator.compute(1040f, 760f, SupportingPaneConfig(spacing = 12f))
        assertEquals(SupportingArrangement.SideBySide, placement.arrangement)
        assertEquals(343.2f, placement.supportingSize, 0.01f)
        assertEquals(1040f, placement.mainSize + placement.spacing + placement.supportingSize, 0.01f)
    }

    @Test
    fun supportingWidthIsClampedAndMainKeepsItsMinimum() {
        assertEquals(420f, SupportingPaneCalculator.compute(2000f, 800f).supportingSize)
        val tight = SupportingPaneCalculator.compute(660f, 800f, SupportingPaneConfig(supportingRatio = 0.6f))
        assertEquals(360f, tight.mainSize, 0.01f)
        assertEquals(300f, tight.supportingSize, 0.01f)
    }

    @Test
    fun phonePortraitStacks() {
        val placement = SupportingPaneCalculator.compute(412f, 800f)
        assertEquals(SupportingArrangement.Stacked, placement.arrangement)
        assertEquals(320f, placement.supportingSize, 0.01f)
        assertEquals(480f, placement.mainSize, 0.01f)
    }

    @Test
    fun stackedMainKeepsItsMinimumHeight() {
        val placement = SupportingPaneCalculator.compute(412f, 420f, SupportingPaneConfig(stackedSupportingRatio = 0.9f))
        assertEquals(240f, placement.mainSize, 0.01f)
        assertEquals(180f, placement.supportingSize, 0.01f)
    }

    @Test
    fun noRoomOrHiddenIsMainOnly() {
        assertEquals(SupportingArrangement.MainOnly, SupportingPaneCalculator.compute(412f, 300f).arrangement)
        val hidden = SupportingPaneCalculator.compute(1200f, 800f, showSupporting = false)
        assertEquals(SupportingArrangement.MainOnly, hidden.arrangement)
        assertEquals(1200f, hidden.mainSize)
    }

    @Test
    fun hingesSplitExactly() {
        val book = SupportingPaneCalculator.compute(860f, 700f, hinge = HingeSpan(FoldOrientation.Vertical, 420f, 440f))
        assertEquals(SupportingArrangement.SideBySide, book.arrangement)
        assertTrue(book.splitAtHinge)
        assertEquals(420f, book.mainSize)
        assertEquals(20f, book.spacing)

        val tabletop = SupportingPaneCalculator.compute(860f, 700f, hinge = HingeSpan(FoldOrientation.Horizontal, 340f, 340f))
        assertEquals(SupportingArrangement.Stacked, tabletop.arrangement)
        assertEquals(340f, tabletop.mainSize)
        assertEquals(360f, tabletop.supportingSize)
    }
}
