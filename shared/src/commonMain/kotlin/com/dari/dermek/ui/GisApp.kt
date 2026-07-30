package com.dari.dermek.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dari.dermek.api.*
import kotlinx.coroutines.launch

/**
 * Main GIS Application — Entry point for the platform.
 * Replaces the knowledge-base viewer with the actual GIS system.
 */

// ─── Navigation State ───

enum class GisScreen {
    LOGIN,
    DASHBOARD,
    DRUG_REGISTRY,
    DRUG_DETAIL,
    APPLICATIONS,
    APPLICATION_DETAIL,
    NEW_APPLICATION,
    QR_SCANNER,
    BATCH_LOOKUP,
    SETTINGS,
    SAFETY_REPORTS,
    NEW_SAFETY_REPORT,
    DISPOSAL_LOG,
    NEW_DISPOSAL_ACT
}

@Composable
fun GisApp() {
    var currentScreen by remember { mutableStateOf(GisScreen.LOGIN) }
    var currentUser by remember { mutableStateOf<UserDto?>(null) }
    var selectedDrugId by remember { mutableStateOf<Long?>(null) }
    var selectedAppId by remember { mutableStateOf<Long?>(null) }
    var qrResult by remember { mutableStateOf<QrScanResultDto?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isCheckingSession by remember { mutableStateOf(true) }

    // Auto-login on startup
    LaunchedEffect(Unit) {
        try {
            val cached = GisHttpClient.getCurrentUser()
            if (cached != null) {
                currentUser = cached
                currentScreen = GisScreen.DASHBOARD
            }
        } catch (e: Exception) {
            // Session check failed silently — proceed to login
        }
        isCheckingSession = false
    }

    DariDermekTheme {
        AppGradientBackground {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                if (isCheckingSession) {
                    GisLoadingScreen("Восстановление сессии…")
                } else {
                    GisShellHost(
                        user = currentUser,
                        currentScreen = currentScreen,
                        onNavigate = { screen -> currentScreen = screen },
                        onLogout = {
                            GisHttpClient.logout()
                            currentUser = null
                            currentScreen = GisScreen.LOGIN
                        }
                    ) {
                    when (currentScreen) {
                        GisScreen.LOGIN -> LoginScreen(
                            isLoggingIn = isLoggingIn,
                            loginError = loginError,
                            onLogin = { egovCode ->
                                if (!isLoggingIn) {
                                    isLoggingIn = true
                                    loginError = null
                                    scope.launch {
                                        try {
                                            currentUser = GisHttpClient.loginWithEgov(egovCode)
                                            currentScreen = GisScreen.DASHBOARD
                                        } catch (e: Exception) {
                                            loginError = "Ошибка аутентификации: ${e.message ?: "Проверьте подключение к серверу"}"
                                        } finally {
                                            isLoggingIn = false
                                        }
                                    }
                                }
                            }
                        )

                        GisScreen.DASHBOARD -> DashboardScreen(
                            user = currentUser!!,
                            onNavigate = { screen -> currentScreen = screen },
                            onLogout = {
                                GisHttpClient.logout()
                                currentUser = null
                                currentScreen = GisScreen.LOGIN
                            }
                        )

                        GisScreen.DRUG_REGISTRY -> DrugRegistryScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DASHBOARD },
                            onDrugClick = { id ->
                                selectedDrugId = id
                                currentScreen = GisScreen.DRUG_DETAIL
                            }
                        )

                        GisScreen.DRUG_DETAIL -> DrugDetailScreen(
                            drugId = selectedDrugId ?: 0,
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DRUG_REGISTRY }
                        )

                        GisScreen.APPLICATIONS -> ApplicationsScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DASHBOARD },
                            onAppClick = { id ->
                                selectedAppId = id
                                currentScreen = GisScreen.APPLICATION_DETAIL
                            },
                            onNewApp = { currentScreen = GisScreen.NEW_APPLICATION }
                        )

                        GisScreen.APPLICATION_DETAIL -> ApplicationDetailScreen(
                            appId = selectedAppId ?: 0,
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.APPLICATIONS }
                        )

                        GisScreen.NEW_APPLICATION -> NewApplicationScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.APPLICATIONS },
                            onSubmitted = { currentScreen = GisScreen.APPLICATIONS }
                        )

                        GisScreen.QR_SCANNER -> QrScannerScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DASHBOARD },
                            onResult = { result ->
                                qrResult = result
                                currentScreen = GisScreen.BATCH_LOOKUP
                            }
                        )

                        GisScreen.BATCH_LOOKUP -> BatchLookupScreen(
                            result = qrResult,
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.QR_SCANNER }
                        )

                        GisScreen.SETTINGS -> {
                            currentScreen = GisScreen.DASHBOARD
                        }

                        GisScreen.SAFETY_REPORTS -> SafetyReportsScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DASHBOARD },
                            onNewReport = { currentScreen = GisScreen.NEW_SAFETY_REPORT }
                        )

                        GisScreen.NEW_SAFETY_REPORT -> NewSafetyReportScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.SAFETY_REPORTS },
                            onSubmitted = { currentScreen = GisScreen.SAFETY_REPORTS }
                        )

                        GisScreen.DISPOSAL_LOG -> DisposalLogScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DASHBOARD },
                            onNewAct = { currentScreen = GisScreen.NEW_DISPOSAL_ACT }
                        )

                        GisScreen.NEW_DISPOSAL_ACT -> NewDisposalActScreen(
                            user = currentUser!!,
                            onBack = { currentScreen = GisScreen.DISPOSAL_LOG },
                            onSubmitted = { currentScreen = GisScreen.DISPOSAL_LOG }
                        )
                    }
                    }
                }
            }
        }
    }
}

// ─── LOGIN SCREEN ───

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    isLoggingIn: Boolean = false,
    loginError: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Logo area
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(GisTheme.Primary, GisTheme.PrimaryLight))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                GisIcons.HealthAndSafety,
                null,
                tint = GisTheme.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "ГИС Дәрі-Дәрмек",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GisTheme.TextPrimary
        )
        Text(
            text = "Государственная информационная система\nоборота ветеринарных препаратов",
            fontSize = 14.sp,
            color = GisTheme.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = GisTheme.Accent.copy(alpha = 0.15f)
        ) {
            Text(
                text = "ЕАЭС • Республика Казахстан",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GisTheme.Accent
            )
        }

        Spacer(Modifier.height(32.dp))

        // Error banner
        if (loginError != null) {
            GisErrorBanner(
                message = loginError,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // eGov SSO button
        GisSubmitButton(
            label = "Войти через eGov (ЭЦП)",
            icon = GisIcons.Login,
            isLoading = isLoggingIn,
            enabled = true,
            onClick = { onLogin("code_applicant") }
        )

        Spacer(Modifier.height(24.dp))

        // Demo role selector
        Text(
            text = "Демо-режим: выберите роль",
            fontSize = 13.sp,
            color = GisTheme.TextMuted,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(12.dp))

        val roles = listOf(
            UserRole.APPLICANT, UserRole.COMMITTEE_STAFF, UserRole.NRCV_EXPERT,
            UserRole.LAB_ANALYST, UserRole.BORDER_INSPECTOR, UserRole.WAREHOUSE_CLERK,
            UserRole.FARMER_VET
        )

        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 820.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            roles.forEach { role ->
                val roleCode = when (role) {
                    UserRole.APPLICANT -> "code_applicant"
                    UserRole.COMMITTEE_STAFF -> "code_kvkn"
                    UserRole.NRCV_EXPERT -> "code_expert"
                    UserRole.LAB_ANALYST -> "code_lab"
                    UserRole.BORDER_INSPECTOR -> "code_border"
                    UserRole.WAREHOUSE_CLERK -> "code_warehouse"
                    UserRole.FARMER_VET -> "code_farmer"
                    UserRole.ADMIN -> "code_admin"
                }
                GisGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = if (isLoggingIn) null else ({ onLogin(roleCode) })
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(getRoleColor(role).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getRoleIcon(role),
                                null,
                                tint = if (isLoggingIn) GisTheme.TextMuted else getRoleColor(role),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getRoleName(role),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLoggingIn) GisTheme.TextMuted else GisTheme.TextPrimary
                            )
                            Text(
                                text = getRoleDescription(role),
                                fontSize = 12.sp,
                                color = GisTheme.TextMuted
                            )
                        }
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                color = GisTheme.Primary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(text = "v0.2.0 • Digital Experts Group", fontSize = 11.sp, color = GisTheme.TextMuted)
    }
    }
}

private fun getRoleDescription(role: UserRole): String = when (role) {
    UserRole.APPLICANT -> "Подача заявок на регистрацию"
    UserRole.COMMITTEE_STAFF -> "Рассмотрение и принятие решений"
    UserRole.NRCV_EXPERT -> "Экспертиза документов и образцов"
    UserRole.LAB_ANALYST -> "Лабораторные испытания"
    UserRole.BORDER_INSPECTOR -> "Контроль на границе"
    UserRole.WAREHOUSE_CLERK -> "Приёмка и распределение"
    UserRole.FARMER_VET -> "Применение и отчётность"
    UserRole.ADMIN -> "Администрирование системы"
}

// ─── DASHBOARD SCREEN ───

@Composable
fun DashboardScreen(
    user: UserDto,
    onNavigate: (GisScreen) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(
            title = "ГИС Дәрі-Дәрмек",
            user = user,
            onLogout = onLogout
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.contentMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    shadowElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(GisTheme.PrimaryDark, GisTheme.Primary, GisTheme.PrimaryLight)
                                ),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Добро пожаловать,",
                                fontSize = 14.sp,
                                color = GisTheme.AccentLight
                            )
                            Text(
                                text = user.fullName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GisTheme.White
                            )
                            Spacer(Modifier.height(4.dp))
                            val welcomeSubtitle = remember(user) {
                                listOfNotNull(
                                    getRoleName(user.role),
                                    user.organization?.takeIf {
                                        it.isNotBlank() && it != user.fullName
                                    }
                                ).joinToString(" • ")
                            }
                            Text(
                                text = welcomeSubtitle,
                                fontSize = 13.sp,
                                color = GisTheme.White.copy(alpha = 0.78f)
                            )
                            Spacer(Modifier.height(8.dp))
                            // Backend status indicator
                            var apiStatus by remember { mutableStateOf("Проверка…") }
                            var apiColor by remember { mutableStateOf(GisTheme.TextMuted) }
                            LaunchedEffect(Unit) {
                                val health = GisHttpClient.checkHealth()
                                if (health != null) {
                                    apiStatus = "API: ${health.version} — онлайн"
                                    apiColor = GisTheme.StatusActive
                                } else {
                                    apiStatus = "API: демо-режим (офлайн)"
                                    apiColor = GisTheme.StatusWarning
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(apiColor)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = apiStatus,
                                    fontSize = 11.sp,
                                    color = apiColor
                                )
                            }
                        }
                    }
                }
            }

            // Stats
            item {
                val apps = GisHttpClient.getApplications()
                val drugs = GisHttpClient.getDrugs()

                val stats = listOf(
                    StatSpec(
                        "Активные заявки",
                        apps.count { it.status != ApplicationStatus.APPROVED && it.status != ApplicationStatus.REJECTED }.toString(),
                        GisIcons.Assignment,
                        GisTheme.StatusInfo,
                        GisScreen.APPLICATIONS
                    ),
                    StatSpec(
                        "Реестр препаратов",
                        drugs.count { it.status == "ACTIVE" }.toString(),
                        GisIcons.Medication,
                        GisTheme.StatusActive,
                        GisScreen.DRUG_REGISTRY
                    ),
                    StatSpec(
                        "Одобрено",
                        apps.count { it.status == ApplicationStatus.APPROVED }.toString(),
                        GisIcons.Check,
                        GisTheme.Accent,
                        GisScreen.APPLICATIONS
                    ),
                    StatSpec(
                        "Требуют внимания",
                        apps.count {
                            it.isClockPaused ||
                                it.status == ApplicationStatus.QUERY_SENT ||
                                it.status == ApplicationStatus.COMPLETENESS_FAILED
                        }.toString(),
                        GisIcons.Warning,
                        GisTheme.StatusWarning,
                        GisScreen.APPLICATIONS
                    )
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = if (maxWidth < 900.dp) 2 else 4
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stats.chunked(columns).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { stat ->
                                    StatCard(
                                        title = stat.title,
                                        value = stat.value,
                                        icon = stat.icon,
                                        color = stat.color,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onNavigate(stat.screen) }
                                    )
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                GisSectionHeader(
                    title = "Быстрые действия",
                    subtitle = "Разделы, доступные вашей роли"
                )
            }

            item {
                val actions = getDashboardActions(user.role)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = when {
                        maxWidth < 560.dp -> 1
                        maxWidth < 900.dp -> 2
                        else -> 3
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        actions.chunked(columns).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { (label, icon, color, screen) ->
                                    DashboardActionCard(
                                        label = label,
                                        icon = icon,
                                        color = color,
                                        compact = columns == 1,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onNavigate(screen) }
                                    )
                                }
                                repeat(columns - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Recent applications
            item {
                GisSectionHeader(
                    title = "Последние заявки",
                    subtitle = "Актуальные обращения в системе"
                )
            }

            val recentApps = GisHttpClient.getApplications().take(3)
            items(recentApps, key = { it.id }) { app ->
                ApplicationCard(
                    app = app,
                    onClick = { onNavigate(GisScreen.APPLICATIONS) }
                )
            }
        }
    }
}

private data class StatSpec(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val screen: GisScreen
)

@Composable
private fun DashboardActionCard(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    if (compact) {
        GisGlassCard(modifier = modifier, onClick = onClick, accent = color) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GisTheme.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    GisIcons.ChevronRight,
                    null,
                    tint = GisTheme.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        return
    }

    GisGlassCard(
        modifier = modifier.heightIn(min = 132.dp),
        onClick = onClick,
        accent = color
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = GisTheme.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Открыть",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GisTheme.PrimaryLight
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                GisIcons.ChevronRight,
                null,
                tint = GisTheme.PrimaryLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun getDashboardActions(role: UserRole): List<QuadTuple> {
    val common = listOf(
        QuadTuple("Реестр препаратов", GisIcons.Medication, GisTheme.StatusActive, GisScreen.DRUG_REGISTRY),
        QuadTuple("Сканер QR-кода", GisIcons.QrCodeScanner, GisTheme.StatusInfo, GisScreen.QR_SCANNER),
        QuadTuple("Фармаконадзор", GisIcons.Pharmacovigilance, GisTheme.StatusWarning, GisScreen.SAFETY_REPORTS)
    )

    val roleSpecific = when (role) {
        UserRole.APPLICANT -> listOf(
            QuadTuple("Мои заявки", GisIcons.Assignment, GisTheme.StatusInfo, GisScreen.APPLICATIONS),
            QuadTuple("Подать заявку", GisIcons.Add, GisTheme.Primary, GisScreen.NEW_APPLICATION),
            QuadTuple("Списание и утилизация", GisIcons.Recycle, GisTheme.StatusDanger, GisScreen.DISPOSAL_LOG)
        )
        UserRole.COMMITTEE_STAFF -> listOf(
            QuadTuple("Заявки на рассмотрении", GisIcons.Gavel, GisTheme.Accent, GisScreen.APPLICATIONS)
        )
        UserRole.NRCV_EXPERT -> listOf(
            QuadTuple("Экспертизы", GisIcons.Science, GisTheme.RoleExpert, GisScreen.APPLICATIONS)
        )
        UserRole.LAB_ANALYST -> listOf(
            QuadTuple("Протоколы испытаний", GisIcons.Biotech, GisTheme.RoleLab, GisScreen.APPLICATIONS)
        )
        UserRole.BORDER_INSPECTOR -> listOf(
            QuadTuple("Импортные декларации", GisIcons.LocalShipping, GisTheme.RoleBorder, GisScreen.APPLICATIONS)
        )
        UserRole.WAREHOUSE_CLERK -> listOf(
            QuadTuple("Приёмка партий", GisIcons.Inventory, GisTheme.RoleWarehouse, GisScreen.APPLICATIONS),
            QuadTuple("Списание и утилизация", GisIcons.Recycle, GisTheme.StatusDanger, GisScreen.DISPOSAL_LOG)
        )
        UserRole.FARMER_VET -> listOf(
            QuadTuple("Рецепты", GisIcons.Receipt, GisTheme.RoleFarmer, GisScreen.APPLICATIONS),
            QuadTuple("Списание и утилизация", GisIcons.Recycle, GisTheme.StatusDanger, GisScreen.DISPOSAL_LOG)
        )
        UserRole.ADMIN -> listOf(
            QuadTuple("Все заявки", GisIcons.Assessment, GisTheme.Primary, GisScreen.APPLICATIONS)
        )
    }

    return roleSpecific + common
}

private data class QuadTuple(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val screen: GisScreen
)
