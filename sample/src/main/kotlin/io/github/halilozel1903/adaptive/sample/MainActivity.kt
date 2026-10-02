package io.github.halilozel1903.adaptive.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Harbor, a fictional mail and notes app built with compose-adaptive-kit. `scripts/screenshots.sh` starts it with
 * `--es scene <scene>` to open a fixed screen for README screenshots:
 *
 * - `listdetail`: the inbox with a message open (list, detail and thread details on tablets)
 * - `supporting`: a note with related notes in the supporting pane
 * - `compact`: the inbox list with the bottom bar (phones)
 *
 * Scenes turn on the debug overlay (size class and posture in the corner); `--ez debug true` turns it on without
 * a scene, and the info button in the list header toggles it.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        val debug = intent.getBooleanExtra(EXTRA_DEBUG, false) || scene != null
        setContent {
            SampleTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    SampleApp(scene = scene, initialDebug = debug)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
        const val EXTRA_DEBUG = "debug"
    }
}
