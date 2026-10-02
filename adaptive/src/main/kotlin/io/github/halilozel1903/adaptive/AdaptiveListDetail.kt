package io.github.halilozel1903.adaptive

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds
import io.github.halilozel1903.adaptive.core.PaneConfig
import io.github.halilozel1903.adaptive.core.PaneLayout
import io.github.halilozel1903.adaptive.core.PaneLayoutCalculator
import io.github.halilozel1903.adaptive.core.PaneMode
import io.github.halilozel1903.adaptive.core.PredictiveBackMath
import io.github.halilozel1903.adaptive.core.SwipeEdge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * The pane layout of the nearest [AdaptiveListDetail]. Panes read it to adapt, for example to show a back
 * button only in [PaneMode.Single], or to show the extra pane's content inline when it is not visible.
 */
public val LocalPaneLayout: ProvidableCompositionLocal<PaneLayout> = staticCompositionLocalOf { PaneLayout.single(0f) }

private const val LIST_KEY = "io.github.halilozel1903.adaptive.list"

/**
 * A list-detail layout that shows one, two or three panes depending on the space it gets.
 *
 * - **Compact** (single pane): the list, or the detail when [selected] is not null. Back (with the predictive
 *   back animation on Android 14+) calls [onBack]; clear the selection there.
 * - **Two panes**: list and detail side by side, [detailPlaceholder] while nothing is selected.
 * - **Three panes**: list, detail and [extra] for the selected item, when [extra] is given and there is room.
 * - **Foldables**: with a separating vertical hinge (book posture, dual screens) list and detail split exactly at
 *   the hinge, so nothing is drawn under it.
 *
 * The list keeps its saved state (scroll position) across mode changes, and the detail is keyed by the selected
 * item.
 *
 * ```
 * var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
 * AdaptiveListDetail(
 *     list = { MailList(onOpen = { selectedId = it.id }) },
 *     detail = { mail -> MailDetail(mail) },
 *     selected = mails.firstOrNull { it.id == selectedId },
 *     onBack = { selectedId = null },
 * )
 * ```
 *
 * @param list the list pane.
 * @param detail the detail pane for the selected item.
 * @param selected the selected item, or null.
 * @param onBack called when the user goes back from the detail in single pane mode.
 * @param extra an optional third pane for the selected item, shown when there is room.
 * @param detailPlaceholder shown in the detail pane when nothing is selected (two and three panes).
 * @param config pane ratios, minimum widths and spacing, in dp.
 * @param windowInfo the window, for the hinge. Defaults to [rememberWindowLayoutInfo].
 * @param detailContainerColor the detail's background in single pane mode, where it covers the list.
 */
@Composable
public fun <T : Any> AdaptiveListDetail(
    list: @Composable () -> Unit,
    detail: @Composable (T) -> Unit,
    selected: T?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    extra: (@Composable (T) -> Unit)? = null,
    detailPlaceholder: @Composable () -> Unit = { DefaultDetailPlaceholder() },
    config: PaneConfig = PaneConfig(),
    windowInfo: AdaptiveWindowInfo = rememberWindowLayoutInfo(),
    detailContainerColor: Color = MaterialTheme.colorScheme.surface,
) {
    var container by remember { mutableStateOf<Bounds?>(null) }
    val stateHolder = rememberSaveableStateHolder()
    val density = LocalDensity.current.density
    val layoutDirection = LocalLayoutDirection.current
    BoxWithConstraints(modifier.onWindowBounds { container = it }) {
        val width = if (constraints.hasBoundedWidth) maxWidth else windowInfo.sizeClass.widthDp.dp
        val hinge = windowInfo.hingeIn(container, density, layoutDirection)
        val layout = PaneLayoutCalculator.compute(
            width = width.value,
            config = config,
            hasExtra = extra != null && selected != null,
            hinge = hinge,
        )
        CompositionLocalProvider(LocalPaneLayout provides layout) {
            if (layout.mode == PaneMode.Single) {
                SinglePane(selected, list, detail, onBack, stateHolder, detailContainerColor)
            } else {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.width(layout.listWidth.dp).fillMaxHeight()) {
                        stateHolder.SaveableStateProvider(LIST_KEY) { list() }
                    }
                    PaneSpacer(layout.spacing.dp)
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        if (selected != null) key(selected) { detail(selected) } else detailPlaceholder()
                    }
                    if (layout.mode == PaneMode.ThreePane && extra != null && selected != null) {
                        PaneSpacer(layout.spacing.dp)
                        Box(Modifier.width(layout.extraWidth.dp).fillMaxHeight()) {
                            key(selected) { extra(selected) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaneSpacer(width: Dp) {
    if (width > 0.dp) Spacer(Modifier.width(width))
}

@Composable
private fun <T : Any> SinglePane(
    selected: T?,
    list: @Composable () -> Unit,
    detail: @Composable (T) -> Unit,
    onBack: () -> Unit,
    stateHolder: SaveableStateHolder,
    containerColor: Color,
) {
    val scope = rememberCoroutineScope()
    val backProgress = remember { Animatable(0f) }
    val enterProgress = remember { Animatable(1f) }
    var swipeEdge by remember { mutableStateOf(SwipeEdge.Left) }
    val hasSelection = selected != null

    LaunchedEffect(hasSelection) {
        backProgress.snapTo(0f)
        if (hasSelection) {
            enterProgress.snapTo(0f)
            enterProgress.animateTo(1f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
        }
    }

    PredictiveBackHandler(enabled = hasSelection) { events ->
        try {
            events.collect { event ->
                swipeEdge = if (event.swipeEdge == BackEventCompat.EDGE_RIGHT) SwipeEdge.Right else SwipeEdge.Left
                backProgress.snapTo(event.progress)
            }
            onBack()
        } catch (e: CancellationException) {
            // The gesture was cancelled: bring the detail back.
            scope.launch { backProgress.animateTo(0f) }
            throw e
        }
    }

    val revealList by remember { derivedStateOf { backProgress.value > 0f } }

    Box(Modifier.fillMaxSize()) {
        if (!hasSelection || revealList) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (hasSelection) {
                            val transform = PredictiveBackMath.transform(backProgress.value, swipeEdge, size.width)
                            drawRect(Color.Black, alpha = transform.scrimAlpha)
                        }
                    },
            ) {
                stateHolder.SaveableStateProvider(LIST_KEY) { list() }
            }
        }
        if (selected != null) {
            Surface(
                color = containerColor,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val transform = PredictiveBackMath.transform(backProgress.value, swipeEdge, size.width)
                        val enter = enterProgress.value
                        translationX = transform.translationX + (1f - enter) * size.width * 0.12f
                        scaleX = transform.scale
                        scaleY = transform.scale
                        alpha = enter
                        if (transform.cornerFraction > 0f) {
                            shape = RoundedCornerShape(28.dp * transform.cornerFraction)
                            clip = true
                        }
                    },
            ) {
                key(selected) { detail(selected) }
            }
        }
    }
}

@Composable
private fun DefaultDetailPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Select an item",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
