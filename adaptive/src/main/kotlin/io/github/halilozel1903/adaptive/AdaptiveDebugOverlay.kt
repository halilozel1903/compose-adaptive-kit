package io.github.halilozel1903.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.Bounds

/**
 * A debug overlay: a label in a corner with the window's size class, size in dp and posture (for example
 * `Large x Medium · 1280x800 dp · Flat`), and the fold or hinge drawn as a translucent band. It draws on top of
 * the content without taking touches. Put it last in a full screen `Box`.
 *
 * @param alignment the corner of the label.
 * @param extraText an optional second line, for example the current pane mode.
 */
@Composable
public fun AdaptiveDebugOverlay(
    modifier: Modifier = Modifier,
    windowInfo: AdaptiveWindowInfo = rememberWindowLayoutInfo(),
    alignment: Alignment = Alignment.BottomEnd,
    extraText: String? = null,
) {
    var origin by remember { mutableStateOf<Bounds?>(null) }
    val foldColor = Color(0x66FF5252)
    Box(
        modifier
            .fillMaxSize()
            .onWindowBounds { origin = it }
            .drawBehind {
                val fold = windowInfo.fold ?: return@drawBehind
                val at = origin ?: return@drawBehind
                // Folds without a hinge have no width: draw them a few pixels thick so they are visible.
                val minThickness = 4.dp.toPx()
                val left = fold.bounds.left - at.left
                val top = fold.bounds.top - at.top
                val width = maxOf(fold.bounds.width, minThickness)
                val height = maxOf(fold.bounds.height, minThickness)
                drawRect(
                    color = foldColor,
                    topLeft = Offset(left - (width - fold.bounds.width) / 2f, top - (height - fold.bounds.height) / 2f),
                    size = Size(width, height),
                )
            }
            .padding(12.dp),
        contentAlignment = alignment,
    ) {
        val text = if (extraText == null) windowInfo.label() else windowInfo.label() + "\n" + extraText
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .background(Color(0xE61B1D24), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
