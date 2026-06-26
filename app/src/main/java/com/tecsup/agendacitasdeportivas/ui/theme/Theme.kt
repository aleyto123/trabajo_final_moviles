package com.tecsup.agendacitasdeportivas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SportsGreenDarkPrimary.copy(alpha = GREEN_INTENSITY),
    secondary = SportsGreenDarkSecondary.copy(alpha = GREEN_INTENSITY),
    tertiary = SportsGreenDarkTertiary.copy(alpha = GREEN_INTENSITY),
    onPrimary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = SportsGreenPrimary.copy(alpha = GREEN_INTENSITY),
    secondary = SportsGreenSecondary.copy(alpha = GREEN_INTENSITY),
    tertiary = SportsGreenTertiary.copy(alpha = GREEN_INTENSITY),
    onPrimary = Color.Black
)

@Composable
fun AgendaCitasDeportivasTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
