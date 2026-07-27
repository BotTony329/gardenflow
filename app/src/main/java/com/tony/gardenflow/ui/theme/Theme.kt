package com.tony.gardenflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GardenGreen = Color(0xFF5E8A5F)
val GardenGreenDark = Color(0xFF2D2A26)
val GardenCream = Color(0xFFFAF6F0)
val GardenSurface = Color(0xFFFFFFFF)
val GardenMist = Color(0xFFE8F0E6)
val GardenInk = Color(0xFF2D2A26)
val GardenMuted = Color(0xFF6E6862)
val GardenSoil = Color(0xFFA07856)
val GardenWarmCard = Color(0xFFF2EBDC)
val GardenBorder = Color(0xFFECE6D8)

private val Light = lightColorScheme(
    primary = GardenGreen,
    onPrimary = Color.White,
    secondary = GardenMuted,
    tertiary = GardenSoil,
    background = GardenCream,
    onBackground = GardenInk,
    surface = GardenSurface,
    onSurface = GardenInk,
    surfaceContainer = GardenWarmCard,
    surfaceContainerHigh = GardenMist,
    outline = GardenBorder,
    error = Color(0xFFD08068)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF84C894),
    onPrimary = Color(0xFF0E2E24),
    secondary = Color(0xFFB9C9B2),
    tertiary = Color(0xFFA4D0CB),
    background = Color(0xFF061E19),
    onBackground = Color(0xFFEAF2E6),
    surface = Color(0xFF0B2A23),
    onSurface = Color(0xFFEAF2E6),
    surfaceContainer = Color(0xFF12382F),
    surfaceContainerHigh = Color(0xFF19463B),
    outline = Color(0xFF31594E)
)

private val GardenTypography = Typography(
    displayLarge = TextStyle(fontSize = 52.sp, lineHeight = 58.sp, fontWeight = FontWeight.Normal),
    displayMedium = TextStyle(fontSize = 38.sp, lineHeight = 44.sp, fontWeight = FontWeight.Normal),
    displaySmall = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Normal),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 18.sp, lineHeight = 25.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal)
)

@Composable
fun GardenFlowTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) Dark else Light
    MaterialTheme(colorScheme = colors, typography = GardenTypography, content = content)
}
