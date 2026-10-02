package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HingeSplitTest {

    @Test
    fun splitsExactlyAtAVerticalHinge() {
        val split = HingeSplit.compute(1000f, 800f, HingeSpan(FoldOrientation.Vertical, 600f, 640f))
        assertEquals(SplitDirection.SideBySide, split.direction)
        assertEquals(600f, split.firstSize)
        assertEquals(40f, split.gap)
        assertEquals(360f, split.secondSize)
        assertTrue(split.alignedToHinge)
    }

    @Test
    fun tabletopHingeStacks() {
        val split = HingeSplit.compute(1000f, 800f, HingeSpan(FoldOrientation.Horizontal, 400f, 400f))
        assertEquals(SplitDirection.Stacked, split.direction)
        assertEquals(400f, split.firstSize)
        assertEquals(0f, split.gap)
        assertEquals(400f, split.secondSize)
    }

    @Test
    fun fallsBackToRatioAndSpacing() {
        val split = HingeSplit.compute(1000f, 800f, null, ratio = 0.4f, spacing = 20f)
        assertEquals(SplitDirection.SideBySide, split.direction)
        assertEquals(392f, split.firstSize, 0.001f)
        assertEquals(20f, split.gap)
        assertEquals(588f, split.secondSize, 0.001f)
        assertFalse(split.alignedToHinge)

        val stacked = HingeSplit.compute(1000f, 800f, null, fallbackDirection = SplitDirection.Stacked)
        assertEquals(400f, stacked.firstSize)
        assertEquals(400f, stacked.secondSize)
    }

    @Test
    fun sizesAlwaysAddUp() {
        for (ratio in listOf(0f, 0.25f, 0.5f, 1f)) {
            val split = HingeSplit.compute(731f, 300f, null, ratio = ratio, spacing = 13f)
            assertEquals(731f, split.firstSize + split.gap + split.secondSize, 0.001f)
        }
    }

    @Test
    fun rejectsBadRatio() {
        assertFailsWith<IllegalArgumentException> { HingeSplit.compute(100f, 100f, null, ratio = 1.5f) }
    }
}
