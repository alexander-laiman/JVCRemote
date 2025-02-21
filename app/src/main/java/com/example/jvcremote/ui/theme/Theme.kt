package com.example.jvcremote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.jvcremote.RemoteControlScreen

// Define your colors
val BlackBackground = Color(0xFF000000) // Pure Black
val SoftYellow = Color(0xFFFFE082)      // Soft Yellow
val BlackText = Color(0xFF000000)       // Pure Black Text

// Custom button colors
@Composable
fun CustomButtonColors() = ButtonDefaults.buttonColors(
    containerColor = SoftYellow,
    contentColor = BlackText
)
// Theme setup
@Composable
fun JVCRemoteTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )

}

// Define dark and light color schemes (optional, for future customization)
// Dark color scheme
private fun darkColorScheme(): ColorScheme = darkColorScheme(
    background = BlackBackground,
    primary = SoftYellow,
    onPrimary = BlackText,
    surface = BlackBackground,
    onSurface = SoftYellow,
    onBackground = SoftYellow,
    onPrimaryContainer = BlackText
)

// Dark color scheme
private fun lightColorScheme(): ColorScheme = darkColorScheme(
    background = BlackBackground,
    primary = SoftYellow,
    onPrimary = BlackText,
    surface = BlackBackground,
    onSurface = SoftYellow
)
