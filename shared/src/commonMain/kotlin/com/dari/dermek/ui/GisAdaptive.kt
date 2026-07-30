package com.dari.dermek.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dari.dermek.api.UserDto
import com.dari.dermek.api.UserRole

/**
 * Adaptive layout system for Дәрі-Дәрмек.
 *
 * Compact  (< 720dp)  — phones: bottom navigation.
 * Medium   (< 1100dp) — tablets / small windows: compact navigation rail.
 * Expanded (>= 1100dp)— desktop & web: navigation rail with labels.
 */

enum class GisWindowSize { COMPACT, MEDIUM, EXPANDED }

object GisSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object GisLayout {
    val contentMaxWidth = 1180.dp
    val formMaxWidth = 720.dp
    val railWidth = 240.dp
    val railWidthCompact = 88.dp
}

data class GisDestination(
    val screen: GisScreen,
    val label: String,
    val icon: ImageVector
)

fun gisDestinationsFor(role: UserRole): List<GisDestination> {
    val base = mutableListOf(
        GisDestination(GisScreen.DASHBOARD, "Обзор", GisIcons.Home),
        GisDestination(GisScreen.APPLICATIONS, "Заявки", GisIcons.Assignment),
        GisDestination(GisScreen.DRUG_REGISTRY, "Реестр", GisIcons.Medication),
        GisDestination(GisScreen.QR_SCANNER, "Сканер", GisIcons.QrCodeScanner),
        GisDestination(GisScreen.SAFETY_REPORTS, "Надзор", GisIcons.Pharmacovigilance)
    )
    if (role == UserRole.APPLICANT || role == UserRole.WAREHOUSE_CLERK ||
        role == UserRole.FARMER_VET || role == UserRole.ADMIN
    ) {
        base.add(GisDestination(GisScreen.DISPOSAL_LOG, "Утилизация", GisIcons.Recycle))
    }
    return base
}

/** Maps a detail screen back to the primary destination that owns it. */
fun GisScreen.primaryDestination(): GisScreen = when (this) {
    GisScreen.DRUG_DETAIL -> GisScreen.DRUG_REGISTRY
    GisScreen.APPLICATION_DETAIL, GisScreen.NEW_APPLICATION -> GisScreen.APPLICATIONS
    GisScreen.BATCH_LOOKUP -> GisScreen.QR_SCANNER
    GisScreen.NEW_SAFETY_REPORT -> GisScreen.SAFETY_REPORTS
    GisScreen.NEW_DISPOSAL_ACT -> GisScreen.DISPOSAL_LOG
    else -> this
}

/** Gentle fade/rise applied whenever the active screen changes. */
@Composable
private fun GisScreenTransition(
    key: GisScreen,
    content: @Composable () -> Unit
) {
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { progress.animateTo(1f, animationSpec = tween(220)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 0.55f + 0.45f * progress.value
                translationY = (1f - progress.value) * 14f
            }
    ) {
        content()
    }
}

/**
 * Chooses between the plain full-bleed layout (login / no session) and the
 * adaptive navigation shell used for every authenticated screen.
 */
@Composable
fun GisShellHost(
    user: UserDto?,
    currentScreen: GisScreen,
    onNavigate: (GisScreen) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    if (user == null || currentScreen == GisScreen.LOGIN) {
        content()
    } else {
        GisAppShell(
            user = user,
            currentScreen = currentScreen,
            onNavigate = onNavigate,
            onLogout = onLogout,
            content = content
        )
    }
}

/**
 * Application shell: renders adaptive navigation around screen content.
 */
@Composable
fun GisAppShell(
    user: UserDto,
    currentScreen: GisScreen,
    onNavigate: (GisScreen) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val destinations = gisDestinationsFor(user.role)
    val selected = currentScreen.primaryDestination()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowSize = when {
            maxWidth < 720.dp -> GisWindowSize.COMPACT
            maxWidth < 1100.dp -> GisWindowSize.MEDIUM
            else -> GisWindowSize.EXPANDED
        }

        when (windowSize) {
            GisWindowSize.COMPACT -> Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    GisScreenTransition(currentScreen, content)
                }
                GisBottomNav(
                    destinations = destinations,
                    selected = selected,
                    onNavigate = onNavigate
                )
            }

            else -> Row(modifier = Modifier.fillMaxSize()) {
                GisNavigationRail(
                    user = user,
                    destinations = destinations,
                    selected = selected,
                    expanded = windowSize == GisWindowSize.EXPANDED,
                    onNavigate = onNavigate,
                    onLogout = onLogout
                )
                Box(modifier = Modifier.weight(1f)) {
                    GisScreenTransition(currentScreen, content)
                }
            }
        }
    }
}

@Composable
private fun GisBottomNav(
    destinations: List<GisDestination>,
    selected: GisScreen,
    onNavigate: (GisScreen) -> Unit
) {
    Surface(
        color = GisTheme.Surface.copy(alpha = 0.96f),
        tonalElevation = 3.dp,
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            destinations.take(5).forEach { destination ->
                val isSelected = destination.screen == selected
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination.screen) },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = {
                        Text(
                            destination.label,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GisTheme.White,
                        selectedTextColor = GisTheme.PrimaryLight,
                        indicatorColor = GisTheme.Primary.copy(alpha = 0.55f),
                        unselectedIconColor = GisTheme.TextSecondary,
                        unselectedTextColor = GisTheme.TextMuted
                    )
                )
            }
        }
    }
}

@Composable
private fun GisNavigationRail(
    user: UserDto,
    destinations: List<GisDestination>,
    selected: GisScreen,
    expanded: Boolean,
    onNavigate: (GisScreen) -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(if (expanded) GisLayout.railWidth else GisLayout.railWidthCompact),
        color = GisTheme.Surface.copy(alpha = 0.72f),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = GisSpacing.lg, horizontal = GisSpacing.md),
            horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally
        ) {
            GisBrandMark(expanded = expanded)

            Spacer(Modifier.height(GisSpacing.xl))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(GisSpacing.sm)
            ) {
                destinations.forEach { destination ->
                    GisRailItem(
                        destination = destination,
                        isSelected = destination.screen == selected,
                        expanded = expanded,
                        onClick = { onNavigate(destination.screen) }
                    )
                }
            }

            Spacer(Modifier.height(GisSpacing.md))
            HorizontalDivider(color = GisTheme.Divider.copy(alpha = 0.6f))
            Spacer(Modifier.height(GisSpacing.md))

            GisRailProfile(
                user = user,
                expanded = expanded,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun GisBrandMark(expanded: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GisSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(GisTheme.Primary, GisTheme.PrimaryLight))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                GisIcons.HealthAndSafety,
                contentDescription = null,
                tint = GisTheme.White,
                modifier = Modifier.size(22.dp)
            )
        }
        if (expanded) {
            Column {
                Text(
                    "Дәрі-Дәрмек",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GisTheme.TextPrimary,
                    maxLines = 1
                )
                Text(
                    "ГИС оборота ВЛП",
                    fontSize = 11.sp,
                    color = GisTheme.TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun GisRailItem(
    destination: GisDestination,
    isSelected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) GisTheme.Primary.copy(alpha = 0.22f) else Color.Transparent
    val contentColor = if (isSelected) GisTheme.PrimaryLight else GisTheme.TextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GisSpacing.md, vertical = GisSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                destination.icon,
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            if (expanded) {
                Spacer(Modifier.width(GisSpacing.md))
                Text(
                    destination.label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) GisTheme.TextPrimary else GisTheme.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GisRailProfile(
    user: UserDto,
    expanded: Boolean,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GisSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getRoleColor(user.role).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getRoleIcon(user.role),
                    contentDescription = null,
                    tint = getRoleColor(user.role),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.width(GisSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.fullName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GisTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        getRoleName(user.role),
                        fontSize = 11.sp,
                        color = GisTheme.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (expanded) {
            TextButton(onClick = onLogout) {
                Icon(GisIcons.Logout, null, tint = GisTheme.TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(GisSpacing.sm))
                Text("Выйти", fontSize = 13.sp, color = GisTheme.TextSecondary)
            }
        } else {
            IconButton(onClick = onLogout) {
                Icon(GisIcons.Logout, "Выйти", tint = GisTheme.TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Centers page content and caps line length on large screens. */
@Composable
fun GisContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: androidx.compose.ui.unit.Dp = GisLayout.contentMaxWidth,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth()) {
            content()
        }
    }
}

/** Section title used across screens for consistent rhythm. */
@Composable
fun GisSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GisTheme.TextPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(GisSpacing.xs))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = GisTheme.TextMuted,
                    textAlign = TextAlign.Start
                )
            }
        }
        if (trailing != null) trailing()
    }
}
