package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FoldGeometryTest {

    private val window = Bounds(0f, 0f, 2208f, 1840f)
    private val verticalFold = FoldInfo(Bounds(1104f, 0f, 1104f, 1840f), FoldOrientation.Vertical, FoldState.HalfOpened)
    private val hinge = FoldInfo(Bounds(1300f, 0f, 1384f, 1800f), FoldOrientation.Vertical, FoldState.Flat, FoldOcclusion.Full)

    @Test
    fun separatingFollowsJetpackRules() {
        assertTrue(FoldGeometry.isSeparating(FoldState.HalfOpened, FoldOcclusion.None))
        assertTrue(FoldGeometry.isSeparating(FoldState.Flat, FoldOcclusion.Full))
        assertFalse(FoldGeometry.isSeparating(FoldState.Flat, FoldOcclusion.None))
        assertFalse(FoldInfo(Bounds(0f, 900f, 2208f, 900f), FoldOrientation.Horizontal, FoldState.Flat).isSeparating)
    }

    @Test
    fun postures() {
        assertEquals(Posture.Flat, FoldGeometry.posture(null))
        assertEquals(Posture.Book, FoldGeometry.posture(verticalFold))
        assertEquals(Posture.Flat, FoldGeometry.posture(verticalFold.copy(state = FoldState.Flat)))
        val tabletop = FoldInfo(Bounds(0f, 920f, 2208f, 920f), FoldOrientation.Horizontal, FoldState.HalfOpened)
        assertEquals(Posture.Tabletop, FoldGeometry.posture(tabletop))
        // A flat device with a physical hinge is still held flat.
        assertEquals(Posture.Flat, FoldGeometry.posture(hinge))
    }

    @Test
    fun hingeInContainerIsLocalAndScaled() {
        val span = FoldGeometry.hingeIn(hinge, Bounds(100f, 50f, 2000f, 1800f), scale = 2f)!!
        assertEquals(FoldOrientation.Vertical, span.orientation)
        assertEquals(600f, span.start)
        assertEquals(642f, span.end)
        assertEquals(42f, span.size)
    }

    @Test
    fun zeroWidthFoldIsFound() {
        val span = FoldGeometry.hingeIn(verticalFold, window)!!
        assertEquals(1104f, span.start)
        assertEquals(0f, span.size)
    }

    @Test
    fun hingeOutsideContainerOrNotSeparatingIsIgnored() {
        assertNull(FoldGeometry.hingeIn(hinge, Bounds(0f, 0f, 1200f, 1800f)))
        assertNull(FoldGeometry.hingeIn(hinge, Bounds(1384f, 0f, 2208f, 1800f)))
        val flatFold = FoldInfo(verticalFold.bounds, FoldOrientation.Vertical, FoldState.Flat)
        assertNull(FoldGeometry.hingeIn(flatFold, window))
        assertEquals(1104f, FoldGeometry.hingeIn(flatFold, window, separatingOnly = false)!!.start)
        assertNull(FoldGeometry.hingeIn(null, window))
    }

    @Test
    fun horizontalHinge() {
        val tabletop = FoldInfo(Bounds(0f, 920f, 2208f, 940f), FoldOrientation.Horizontal, FoldState.HalfOpened)
        val span = FoldGeometry.hingeIn(tabletop, Bounds(0f, 120f, 2208f, 1840f))!!
        assertEquals(FoldOrientation.Horizontal, span.orientation)
        assertEquals(800f, span.start)
        assertEquals(820f, span.end)
    }

    @Test
    fun safeRegionsSkipTheHinge() {
        assertEquals(listOf(0f..1000f), FoldGeometry.safeRegions(1000f, null))
        val span = HingeSpan(FoldOrientation.Vertical, 480f, 520f)
        assertEquals(listOf(0f..480f, 520f..1000f), FoldGeometry.safeRegions(1000f, span))
    }

    @Test
    fun avoidHingeMovesContentToTheNearestSide() {
        val span = HingeSpan(FoldOrientation.Vertical, 480f, 520f)
        // Not overlapping: unchanged.
        assertEquals(100f, FoldGeometry.avoidHinge(100f, 200f, 1000f, span))
        assertEquals(520f, FoldGeometry.avoidHinge(520f, 200f, 1000f, span))
        // Mostly left of the hinge: snaps to end at the hinge.
        assertEquals(280f, FoldGeometry.avoidHinge(400f, 200f, 1000f, span))
        // Mostly right: starts after the hinge.
        assertEquals(520f, FoldGeometry.avoidHinge(470f, 200f, 1000f, span))
        // Only fits after the hinge.
        assertEquals(520f, FoldGeometry.avoidHinge(300f, 450f, 1000f, HingeSpan(FoldOrientation.Vertical, 400f, 520f)))
        // Fits nowhere: start of the larger side.
        assertEquals(0f, FoldGeometry.avoidHinge(300f, 700f, 1000f, span))
        // A zero width fold still counts.
        assertEquals(300f, FoldGeometry.avoidHinge(400f, 200f, 1000f, HingeSpan(FoldOrientation.Vertical, 500f, 500f)))
    }

    @Test
    fun mirroredHinge() {
        assertEquals(HingeSpan(FoldOrientation.Vertical, 400f, 450f), HingeSpan(FoldOrientation.Vertical, 550f, 600f).mirrored(1000f))
    }
}
