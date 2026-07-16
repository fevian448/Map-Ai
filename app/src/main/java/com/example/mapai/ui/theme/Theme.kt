package com.example.mapai.ui.theme

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
import com.example.mapai.data.AppSettings
import com.example.mapai.data.SettingsStore

@Composable
fun MapAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val settings = SettingsStore.get()
    val primary = AppSettings.themeColor(settings.themeColor)

    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = primary,
            secondary = AccentTeal,
            tertiary = primary,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSurface = Color(0xFFE0E0E0)
        )
        else -> lightColorScheme(
            primary = primary,
            secondary = AccentTeal,
            tertiary = primary,
            background = SurfaceLight,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = OnSurfaceLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
