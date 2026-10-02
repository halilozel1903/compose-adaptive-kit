package io.github.halilozel1903.adaptive.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** A calm teal theme for Harbor. */
@Composable
fun SampleTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFF7DD3C7),
            onPrimary = Color(0xFF00382F),
            primaryContainer = Color(0xFF0F5146),
            onPrimaryContainer = Color(0xFFB4F1E6),
            secondary = Color(0xFFB2CCC6),
            secondaryContainer = Color(0xFF334B46),
            onSecondaryContainer = Color(0xFFCDE8E1),
            tertiary = Color(0xFFF4B88A),
            tertiaryContainer = Color(0xFF5E3A1C),
            onTertiaryContainer = Color(0xFFFFDCC4),
            background = Color(0xFF0E1513),
            onBackground = Color(0xFFDDE4E1),
            surface = Color(0xFF0E1513),
            onSurface = Color(0xFFDDE4E1),
            surfaceVariant = Color(0xFF3F4946),
            onSurfaceVariant = Color(0xFFBEC9C5),
            surfaceContainerLowest = Color(0xFF09100E),
            surfaceContainerLow = Color(0xFF161D1B),
            surfaceContainer = Color(0xFF1A2120),
            surfaceContainerHigh = Color(0xFF242B2A),
            surfaceContainerHighest = Color(0xFF2F3634),
            outline = Color(0xFF899390),
            outlineVariant = Color(0xFF3F4946),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF006B5E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFA6F2E3),
            onPrimaryContainer = Color(0xFF00201B),
            secondary = Color(0xFF4A635E),
            secondaryContainer = Color(0xFFCDE8E1),
            onSecondaryContainer = Color(0xFF06201B),
            tertiary = Color(0xFF8A5222),
            tertiaryContainer = Color(0xFFFFDCC4),
            onTertiaryContainer = Color(0xFF2F1400),
            background = Color(0xFFEFF4F2),
            onBackground = Color(0xFF171D1B),
            surface = Color(0xFFEFF4F2),
            onSurface = Color(0xFF171D1B),
            surfaceVariant = Color(0xFFDAE5E1),
            onSurfaceVariant = Color(0xFF3F4946),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFE6EEEB),
            surfaceContainerHigh = Color(0xFFE0E9E6),
            surfaceContainerHighest = Color(0xFFDAE3E0),
            outline = Color(0xFF6F7976),
            outlineVariant = Color(0xFFC3CDC9),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
