package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0A2654),
    primaryContainer = PharmaBlueDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = PharmaBlue,
    onPrimary = Color.White,
    primaryContainer = PharmaBlueLight,
    onPrimaryContainer = PharmaBlueDark,
    secondary = PharmaCyan,
    onSecondary = Color.White,
    secondaryContainer = PharmaCyanLight,
    onSecondaryContainer = PharmaCyanDark,
    tertiary = PharmaGreen,
    onTertiary = Color.White,
    tertiaryContainer = PharmaGreenLight,
    background = PharmaBg,
    surface = PharmaSurface,
    onBackground = PharmaTextPrimary,
    onSurface = PharmaTextPrimary,
    outline = PharmaBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep clean tailored pharma branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
