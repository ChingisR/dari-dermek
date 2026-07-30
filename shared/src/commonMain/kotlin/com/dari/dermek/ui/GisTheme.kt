package com.dari.dermek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * GIS Дари-дермек Design System
 * Premium government-grade color palette inspired by Kazakh national colors.
 */
object GisTheme {
    // ─── Primary Palette (Kazakh Gold + Teal) ───
    val Primary = Color(0xFF0D6E6E)         // Deep teal — trust, government
    val PrimaryLight = Color(0xFF1A9A9A)    // Lighter teal for hover
    val PrimaryDark = Color(0xFF084D4D)     // Dark teal for headers
    val Accent = Color(0xFFD4A843)          // Kazakh gold — heritage
    val AccentLight = Color(0xFFF0D078)     // Light gold for highlights

    // ─── Background ───
    val Background = Color(0xFF0F1923)       // Deep navy-black
    val Surface = Color(0xFF1A2634)          // Card surface
    val SurfaceElevated = Color(0xFF243342)  // Elevated surface
    val SurfaceBorder = Color(0xFF2E3F50)    // Border lines

    // ─── Text ───
    val TextPrimary = Color(0xFFE8ECF1)      // Near-white
    val TextSecondary = Color(0xFF8899AA)    // Muted grey-blue
    val TextMuted = Color(0xFF5A6B7D)        // Very muted

    // ─── Status Colors ───
    val StatusActive = Color(0xFF22C55E)     // Green — approved/active
    val StatusPending = Color(0xFFF59E0B)    // Amber — pending/review
    val StatusWarning = Color(0xFFF97316)    // Orange — warning
    val StatusDanger = Color(0xFFEF4444)     // Red — rejected/counterfeit
    val StatusInfo = Color(0xFF3B82F6)       // Blue — information
    val StatusPaused = Color(0xFF8B5CF6)     // Purple — clock paused

    // ─── Pathway Colors ───
    val PathCompliance = Color(0xFF06B6D4)   // Cyan
    val PathStandard = Color(0xFF3B82F6)     // Blue
    val PathSimplified = Color(0xFF22C55E)   // Green
    val PathConfirmation = Color(0xFF8B5CF6) // Purple
    val PathAmendment = Color(0xFFF59E0B)    // Amber
    val PathRecognition = Color(0xFFEC4899)  // Pink

    // ─── Role Colors ───
    val RoleApplicant = Color(0xFF3B82F6)
    val RoleCommittee = Color(0xFFD4A843)
    val RoleExpert = Color(0xFF06B6D4)
    val RoleLab = Color(0xFF8B5CF6)
    val RoleBorder = Color(0xFFF97316)
    val RoleWarehouse = Color(0xFF22C55E)
    val RoleFarmer = Color(0xFF84CC16)

    // ─── Utility ───
    val Overlay = Color(0x80000000)           // 50% black overlay
    val Divider = Color(0xFF2E3F50)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val AppColorScheme = darkColorScheme(
        primary = Primary,
        onPrimary = White,
        secondary = Accent,
        onSecondary = Black,
        tertiary = StatusInfo,
        background = Background,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceElevated,
        onSurfaceVariant = TextSecondary,
        error = StatusDanger,
        onError = White
    )

    val AppTypography = Typography(
        headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
        headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
        titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
        titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
        bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, color = TextPrimary),
        bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = TextSecondary),
        labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
        labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
    )
}

@Composable
fun DariDermekTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GisTheme.AppColorScheme,
        typography = GisTheme.AppTypography,
        content = content
    )
}

@Composable
fun AppGradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0A1017),
                        Color(0xFF101C2B),
                        Color(0xFF0B1F1F)
                    )
                )
            )
    ) {
        content()
    }
}
