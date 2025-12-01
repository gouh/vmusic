package mx.valdora.vmusic.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VMusicTheme(
    accentColor: Color = AccentPink,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = accentColor,
        secondary = AccentTeal,
        background = Black,
        surface = Black,
        surfaceVariant = MediumGray,
        onPrimary = White,
        onSecondary = Black,
        onBackground = White,
        onSurface = White,
        onSurfaceVariant = White
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
