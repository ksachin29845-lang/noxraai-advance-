package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val SachinChatShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SachinChatTheme(
    themeMode: String = "System", // "System", "Light", "Dark"
    accentPreset: String = "Emerald", // "Emerald", "Ocean", "Royal", "Sunset"
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val primaryLight = when (accentPreset) {
        "Ocean" -> OceanPrimaryLight
        "Royal" -> RoyalPrimaryLight
        "Sunset" -> SunsetPrimaryLight
        else -> EmeraldPrimaryLight
    }

    val primaryDark = when (accentPreset) {
        "Ocean" -> OceanPrimaryDark
        "Royal" -> RoyalPrimaryDark
        "Sunset" -> SunsetPrimaryDark
        else -> EmeraldPrimaryDark
    }

    val containerLight = when (accentPreset) {
        "Ocean" -> Color(0xFFE0F2FE)
        "Royal" -> Color(0xFFEDE9FE)
        "Sunset" -> Color(0xFFFFE4E6)
        else -> EmeraldContainerLight
    }

    val containerDark = when (accentPreset) {
        "Ocean" -> Color(0xFF0C4A6E)
        "Royal" -> Color(0xFF4C1D95)
        "Sunset" -> Color(0xFF881337)
        else -> EmeraldContainerDark
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = primaryDark,
            onPrimary = EmeraldOnPrimaryDark,
            primaryContainer = containerDark,
            onPrimaryContainer = Color(0xFFF8FAFC),
            secondary = SaffronSecondaryDark,
            onSecondary = SaffronOnSecondaryDark,
            secondaryContainer = SaffronContainerDark,
            onSecondaryContainer = SaffronOnContainerDark,
            tertiary = SkyTertiaryDark,
            onTertiary = SkyOnTertiaryDark,
            tertiaryContainer = SkyContainerDark,
            onTertiaryContainer = SkyOnTertiaryContainerDark,
            background = BackgroundDark,
            onBackground = OnSurfaceDark,
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = OnSurfaceVariantDark
        )
    } else {
        lightColorScheme(
            primary = primaryLight,
            onPrimary = EmeraldOnPrimaryLight,
            primaryContainer = containerLight,
            onPrimaryContainer = OnSurfaceLight,
            secondary = SaffronSecondaryLight,
            onSecondary = SaffronOnSecondaryLight,
            secondaryContainer = SaffronContainerLight,
            onSecondaryContainer = SaffronOnContainerLight,
            tertiary = SkyTertiaryLight,
            onTertiary = SkyOnTertiaryLight,
            tertiaryContainer = SkyContainerLight,
            onTertiaryContainer = SkyOnTertiaryContainerLight,
            background = BackgroundLight,
            onBackground = OnSurfaceLight,
            surface = SurfaceLight,
            onSurface = OnSurfaceLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = OnSurfaceVariantLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = buildSachinChatTypography(textScale),
        shapes = SachinChatShapes,
        content = content
    )
}
