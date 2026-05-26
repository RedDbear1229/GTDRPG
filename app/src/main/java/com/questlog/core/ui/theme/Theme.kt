package com.questlog.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// QuestLog는 다크 테마 고정 — 시스템 설정 무시 (CLAUDE.md)
private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = BackgroundDark,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnSurfacePrimary,

    secondary = TealStone,
    onSecondary = BackgroundDark,
    secondaryContainer = TealStoneContainer,
    onSecondaryContainer = OnSurfacePrimary,

    tertiary = EmeraldXP,
    onTertiary = BackgroundDark,

    error = CrimsonHP,
    onError = BackgroundDark,
    errorContainer = CrimsonHPContainer,
    onErrorContainer = OnSurfacePrimary,

    background = BackgroundDark,
    onBackground = OnSurfacePrimary,

    surface = SurfaceDark,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceSecondary,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,

    outline = OutlineGold,
    outlineVariant = OutlineVariant
)

@Composable
fun QuestLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = QuestLogTypography,
        shapes = QuestLogShapes,
        content = content
    )
}
