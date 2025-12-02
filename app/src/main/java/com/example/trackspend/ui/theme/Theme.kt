package com.example.trackspend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


/**
 * Custom dark color scheme used throughout the TrackSpend app.
 *
 * <p>Defines high-contrast purples for primary/secondary colors and a deep black
 * background to create a consistent dark UI experience.</p>
 *
 * <p>This palette overrides Material3 defaults to match the brand's glow-based,
 * purple aesthetic.</p>
 */
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9B6DFF),
    secondary = Color(0xFF7A4FFF),
    tertiary = Color(0xFFE0B3FF),

    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF1A1A1A),
    onBackground = Color.White,
    onSurface = Color.White
)


/**
 * Custom light color scheme for TrackSpend.
 *
 * <p>Uses branded purple tones for primary elements while retaining Material3
 * defaults for supporting surfaces. Designed to match the dark theme styling
 * but with brighter backgrounds.</p>
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


/**
 * Root theme composable for the TrackSpend app.
 *
 * <p>Applies the app's custom light or dark color palette and typography to
 * all nested composables. This ensures consistent visuals across screens,
 * charts, cards, and UI components.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>darkTheme</b> – Whether the system is currently in dark mode.</li>
 *   <li><b>dynamicColor</b> – Dynamic colors (Material You). Disabled by design.</li>
 *   <li><b>content</b> – The composable content to apply the theme to.</li>
 * </ul>
 *
 * @see DarkColorScheme
 * @see LightColorScheme
 */
@Composable
fun TrackSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // fully disabled
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}