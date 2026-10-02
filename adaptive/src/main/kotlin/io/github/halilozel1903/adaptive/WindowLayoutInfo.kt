package io.github.halilozel1903.adaptive

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds
import io.github.halilozel1903.adaptive.core.FoldInfo
import io.github.halilozel1903.adaptive.core.FoldOcclusion
import io.github.halilozel1903.adaptive.core.FoldOrientation
import io.github.halilozel1903.adaptive.core.FoldState
import kotlin.math.roundToInt

/**
 * The current window's size class, density and fold, updated on resizes, rotation, multi-window and fold changes.
 *
 * The size comes from Jetpack WindowManager's `WindowMetricsCalculator` (the real window, not the whole screen),
 * the fold from `WindowInfoTracker`. Outside an Activity (previews) it falls back to the configuration's screen
 * size and reports no fold.
 *
 * ```
 * val info = rememberWindowLayoutInfo()
 * if (info.sizeClass.isWidthAtLeast(WindowWidthClass.Expanded)) TwoColumns() else OneColumn()
 * if (info.posture == Posture.Tabletop) VideoOnTopControlsBelow()
 * ```
 */
@Composable
public fun rememberWindowLayoutInfo(): AdaptiveWindowInfo {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val inspection = LocalInspectionMode.current
    val activity = remember(context, inspection) { if (inspection) null else context.findActivity() }
    val windowSize = remember(
        activity,
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        configuration.orientation,
        density,
    ) {
        if (activity != null) {
            val bounds = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity).bounds
            IntSize(bounds.width(), bounds.height())
        } else {
            IntSize((configuration.screenWidthDp * density).roundToInt(), (configuration.screenHeightDp * density).roundToInt())
        }
    }
    val fold by produceState<FoldInfo?>(initialValue = null, activity) {
        val host = activity ?: return@produceState
        WindowInfoTracker.getOrCreate(host).windowLayoutInfo(host).collect { layoutInfo ->
            value = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()?.toFoldInfo()
        }
    }
    return remember(windowSize, density, fold) {
        AdaptiveWindowInfo.fromPixels(windowSize.width, windowSize.height, density, fold)
    }
}

internal fun FoldingFeature.toFoldInfo(): FoldInfo {
    val rect = bounds
    return FoldInfo(
        bounds = Bounds(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat()),
        orientation = if (orientation == FoldingFeature.Orientation.VERTICAL) FoldOrientation.Vertical else FoldOrientation.Horizontal,
        state = if (state == FoldingFeature.State.HALF_OPENED) FoldState.HalfOpened else FoldState.Flat,
        occlusion = if (occlusionType == FoldingFeature.OcclusionType.FULL) FoldOcclusion.Full else FoldOcclusion.None,
        isSeparating = isSeparating,
    )
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
