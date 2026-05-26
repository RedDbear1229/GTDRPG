package com.questlog.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: replace FontFamily.Serif with MedievalSharp (res/font/medievalsharp_regular.ttf)
//       replace FontFamily.SansSerif with Noto Sans KR (res/font/notosanskr_*.ttf)
//       Both must be added before any UI screenshot tests are recorded (Paparazzi).
private val MedievalSharpFamily = FontFamily.Serif
private val NotoSansKrFamily = FontFamily.SansSerif

val QuestLogTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MedievalSharpFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        color = OnSurfacePrimary
    ),
    displayMedium = TextStyle(
        fontFamily = MedievalSharpFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        color = OnSurfacePrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        color = OnSurfacePrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = OnSurfacePrimary
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = OnSurfacePrimary
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = OnSurfacePrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = OnSurfaceSecondary
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = OnSurfaceSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = OnSurfaceDisabled
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = OnSurfacePrimary
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = OnSurfaceSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansKrFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = OnSurfaceDisabled
    )
)
