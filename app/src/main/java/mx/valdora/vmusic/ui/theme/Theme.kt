package mx.valdora.vmusic.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPink,
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

@Composable
fun VMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
