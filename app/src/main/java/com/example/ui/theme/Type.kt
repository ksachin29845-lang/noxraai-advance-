package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NunitoFontFamily = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito, FontWeight.SemiBold),
    Font(R.font.nunito, FontWeight.Bold),
    Font(R.font.nunito, FontWeight.ExtraBold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

fun buildSachinChatTypography(scale: Float = 1.0f): Typography {
    val clamped = scale.coerceIn(0.85f, 1.3f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (40 * clamped).sp,
            lineHeight = (48 * clamped).sp
        ),
        displayMedium = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (32 * clamped).sp,
            lineHeight = (40 * clamped).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * clamped).sp,
            lineHeight = (36 * clamped).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * clamped).sp,
            lineHeight = (32 * clamped).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * clamped).sp,
            lineHeight = (28 * clamped).sp
        ),
        titleLarge = TextStyle(
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (19 * clamped).sp,
            lineHeight = (26 * clamped).sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * clamped).sp,
            lineHeight = (22 * clamped).sp
        ),
        titleSmall = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * clamped).sp,
            lineHeight = (20 * clamped).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * clamped).sp,
            lineHeight = (24 * clamped).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * clamped).sp,
            lineHeight = (20 * clamped).sp
        ),
        bodySmall = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * clamped).sp,
            lineHeight = (17 * clamped).sp
        ),
        labelLarge = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * clamped).sp,
            lineHeight = (20 * clamped).sp
        ),
        labelMedium = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (12 * clamped).sp,
            lineHeight = (16 * clamped).sp
        ),
        labelSmall = TextStyle(
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * clamped).sp,
            lineHeight = (15 * clamped).sp
        )
    )
}

val Typography = buildSachinChatTypography(1.0f)
