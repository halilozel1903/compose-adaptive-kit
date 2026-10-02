package io.github.halilozel1903.adaptive.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowSizeClassTest {

    @Test
    fun widthBreakpointsFollowMaterial3() {
        assertEquals(WindowWidthClass.Compact, WindowWidthClass.fromWidth(0f))
        assertEquals(WindowWidthClass.Compact, WindowWidthClass.fromWidth(599.9f))
        assertEquals(WindowWidthClass.Medium, WindowWidthClass.fromWidth(600f))
        assertEquals(WindowWidthClass.Medium, WindowWidthClass.fromWidth(839f))
        assertEquals(WindowWidthClass.Expanded, WindowWidthClass.fromWidth(840f))
        assertEquals(WindowWidthClass.Expanded, WindowWidthClass.fromWidth(1199f))
        assertEquals(WindowWidthClass.Large, WindowWidthClass.fromWidth(1200f))
        assertEquals(WindowWidthClass.Large, WindowWidthClass.fromWidth(1599f))
        assertEquals(WindowWidthClass.ExtraLarge, WindowWidthClass.fromWidth(1600f))
        assertEquals(WindowWidthClass.ExtraLarge, WindowWidthClass.fromWidth(3000f))
    }

    @Test
    fun heightBreakpointsFollowMaterial3() {
        assertEquals(WindowHeightClass.Compact, WindowHeightClass.fromHeight(479f))
        assertEquals(WindowHeightClass.Medium, WindowHeightClass.fromHeight(480f))
        assertEquals(WindowHeightClass.Medium, WindowHeightClass.fromHeight(899f))
        assertEquals(WindowHeightClass.Expanded, WindowHeightClass.fromHeight(900f))
    }

    @Test
    fun typicalDevices() {
        val phone = WindowSizeClass(412f, 915f)
        assertEquals(WindowWidthClass.Compact, phone.widthClass)
        assertEquals(WindowHeightClass.Expanded, phone.heightClass)
        assertFalse(phone.isLandscape)

        val tablet = WindowSizeClass(1280f, 800f)
        assertEquals(WindowWidthClass.Large, tablet.widthClass)
        assertEquals(WindowHeightClass.Medium, tablet.heightClass)
        assertTrue(tablet.isLandscape)
        assertTrue(tablet.isWidthAtLeast(WindowWidthClass.Expanded))
        assertFalse(tablet.isWidthAtLeast(WindowWidthClass.ExtraLarge))
        assertTrue(tablet.isHeightAtLeast(WindowHeightClass.Medium))

        val phoneLandscape = WindowSizeClass(915f, 412f)
        assertEquals(WindowWidthClass.Expanded, phoneLandscape.widthClass)
        assertEquals(WindowHeightClass.Compact, phoneLandscape.heightClass)
    }

    @Test
    fun rejectsInvalidSizes() {
        assertFailsWith<IllegalArgumentException> { WindowSizeClass(-1f, 100f) }
        assertFailsWith<IllegalArgumentException> { WindowSizeClass(100f, Float.NaN) }
        assertFailsWith<IllegalArgumentException> { WindowWidthClass.fromWidth(Float.POSITIVE_INFINITY) }
    }

    @Test
    fun navigationTypeFollowsSizeClass() {
        assertEquals(NavigationType.BottomBar, NavigationType.forSizeClass(WindowSizeClass(412f, 915f)))
        assertEquals(NavigationType.Rail, NavigationType.forSizeClass(WindowSizeClass(700f, 1000f)))
        assertEquals(NavigationType.Rail, NavigationType.forSizeClass(WindowSizeClass(915f, 412f)))
        assertEquals(NavigationType.Drawer, NavigationType.forSizeClass(WindowSizeClass(1280f, 800f)))
        assertEquals(NavigationType.Drawer, NavigationType.forSizeClass(WindowSizeClass(841f, 701f)))
    }
}
