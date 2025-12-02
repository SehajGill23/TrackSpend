package com.example.trackspend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Defines the text styles used throughout the TrackSpend app.
 *
 * <p>This typography configuration customizes Material3's default text system.
 * Currently, only the `bodyLarge` style is overridden, while other text styles
 * fall back to Material defaults unless explicitly added.</p>
 *
 * <p>You can extend this typography setup later by overriding styles such as
 * `titleLarge`, `headlineMedium`, or `labelSmall` to better match the app's
 * branding and visual identity.</p>
 *
 * @see androidx.compose.material3.Typography
 */
val Typography = Typography(

    /**
     * Base body text style used across most screens.
     *
     * <p>Provides comfortable readability with a 16sp font size, 24sp line height,
     * and slight letter spacing for cleaner text rendering.</p>
     *
     * <p>This style is applied to general content text, labels, and descriptions
     * unless a more specific style is requested.</p>
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)