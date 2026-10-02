package io.github.halilozel1903.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds
import io.github.halilozel1903.adaptive.core.SupportingArrangement
import io.github.halilozel1903.adaptive.core.SupportingPaneCalculator
import io.github.halilozel1903.adaptive.core.SupportingPaneConfig
import io.github.halilozel1903.adaptive.core.SupportingPanePlacement

/** The placement of the nearest [SupportingPaneLayout], for panes that adapt to it. */
public val LocalSupportingPanePlacement: ProvidableCompositionLocal<SupportingPanePlacement> = staticCompositionLocalOf {
    SupportingPanePlacement(SupportingArrangement.MainOnly, 0f, 0f, 0f)
}

private const val MAIN_KEY = "io.github.halilozel1903.adaptive.main"
private const val SUPPORTING_KEY = "io.github.halilozel1903.adaptive.supporting"

/**
 * A main pane with a supporting pane (Material 3 supporting pane layout), for content with related information:
 * a document and its comments, a video and its chapters, a note and related notes.
 *
 * - **Wide** (tablets, phones in landscape): the supporting pane at the end, side by side.
 * - **Narrow and tall** (phones in portrait): the supporting pane below the main pane.
 * - **No room**, or [showSupporting] false: the main pane only.
 * - **Foldables**: a separating hinge splits the panes exactly at the hinge: side by side in book posture,
 *   main on top and supporting below in tabletop posture.
 *
 * ```
 * SupportingPaneLayout(
 *     main = { NoteEditor(note) },
 *     supporting = { RelatedNotes(note) },
 * )
 * ```
 *
 * Both panes keep their saved state when the arrangement changes.
 */
@Composable
public fun SupportingPaneLayout(
    main: @Composable () -> Unit,
    supporting: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showSupporting: Boolean = true,
    config: SupportingPaneConfig = SupportingPaneConfig(),
    windowInfo: AdaptiveWindowInfo = rememberWindowLayoutInfo(),
) {
    var container by remember { mutableStateOf<Bounds?>(null) }
    val stateHolder = rememberSaveableStateHolder()
    val density = LocalDensity.current.density
    val layoutDirection = LocalLayoutDirection.current
    BoxWithConstraints(modifier.onWindowBounds { container = it }) {
        val width = if (constraints.hasBoundedWidth) maxWidth.value else windowInfo.sizeClass.widthDp
        val height = if (constraints.hasBoundedHeight) maxHeight.value else windowInfo.sizeClass.heightDp
        val placement = SupportingPaneCalculator.compute(
            width = width,
            height = height,
            config = config,
            hinge = windowInfo.hingeIn(container, density, layoutDirection),
            showSupporting = showSupporting,
        )
        val mainPane: @Composable () -> Unit = { stateHolder.SaveableStateProvider(MAIN_KEY) { main() } }
        val supportingPane: @Composable () -> Unit = { stateHolder.SaveableStateProvider(SUPPORTING_KEY) { supporting() } }
        CompositionLocalProvider(LocalSupportingPanePlacement provides placement) {
            when (placement.arrangement) {
                SupportingArrangement.MainOnly -> Box(Modifier.fillMaxSize()) { mainPane() }
                SupportingArrangement.SideBySide -> Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight()) { mainPane() }
                    if (placement.spacing > 0f) Spacer(Modifier.width(placement.spacing.dp))
                    Box(Modifier.width(placement.supportingSize.dp).fillMaxHeight()) { supportingPane() }
                }
                SupportingArrangement.Stacked -> Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth()) { mainPane() }
                    if (placement.spacing > 0f) Spacer(Modifier.height(placement.spacing.dp))
                    Box(Modifier.height(placement.supportingSize.dp).fillMaxWidth()) { supportingPane() }
                }
            }
        }
    }
}
