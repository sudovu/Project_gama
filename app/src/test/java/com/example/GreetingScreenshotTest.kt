package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.core.ui.GammaWaveformVisualizer
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gamma_visualizer_screenshot() {
        val sampleBands = floatArrayOf(
            0.2f, 0.4f, 0.7f, 0.9f, 0.6f, 0.8f, 0.5f, 0.3f,
            0.6f, 0.85f, 0.95f, 0.7f, 0.4f, 0.3f, 0.5f, 0.2f
        )
        composeTestRule.setContent {
            GammaTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .size(360.dp, 100.dp)
                        .background(GammaBackground)
                        .padding(16.dp)
                ) {
                    GammaWaveformVisualizer(
                        bands = sampleBands,
                        isPlaying = true,
                        height = 60.dp
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/gamma_visualizer.png")
    }
}
