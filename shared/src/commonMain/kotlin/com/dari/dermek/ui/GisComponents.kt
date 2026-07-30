package com.dari.dermek.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dari.dermek.api.*

// ─── Status Badge ───

@Composable
fun GisGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val interactive = onClick != null

    val scale by animateFloatAsState(
        targetValue = when {
            interactive && isPressed -> 0.985f
            interactive && isHovered -> 1.006f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 140),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (interactive && isHovered) 16.dp else 8.dp,
        animationSpec = tween(durationMillis = 160),
        label = "cardElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            interactive && isHovered -> (accent ?: GisTheme.PrimaryLight).copy(alpha = 0.55f)
            else -> GisTheme.SurfaceBorder.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "cardBorder"
    )

    Surface(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(18.dp),
        color = GisTheme.Surface.copy(alpha = 0.82f),
        tonalElevation = 2.dp,
        shadowElevation = elevation,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun StatusBadge(status: ApplicationStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        ApplicationStatus.DRAFT -> GisTheme.TextMuted to "Черновик"
        ApplicationStatus.SUBMITTED -> GisTheme.StatusInfo to "Подана"
        ApplicationStatus.COMPLETENESS_CHECK -> GisTheme.StatusPending to "Проверка"
        ApplicationStatus.COMPLETENESS_FAILED -> GisTheme.StatusDanger to "Некомплектна"
        ApplicationStatus.QUERY_SENT -> GisTheme.StatusWarning to "Запрос"
        ApplicationStatus.QUERY_RESPONDED -> GisTheme.StatusInfo to "Ответ получен"
        ApplicationStatus.SAMPLES_REQUESTED -> GisTheme.StatusPending to "Образцы запрошены"
        ApplicationStatus.SAMPLES_RECEIVED -> GisTheme.StatusInfo to "Образцы получены"
        ApplicationStatus.LAB_TESTING -> GisTheme.StatusPaused to "Лаб. испытания"
        ApplicationStatus.LAB_COMPLETE -> GisTheme.StatusInfo to "Испытания завершены"
        ApplicationStatus.EXPERT_CONCLUSION -> GisTheme.StatusPending to "Экспертиза"
        ApplicationStatus.COMMITTEE_REVIEW -> GisTheme.StatusPending to "Рассмотрение"
        ApplicationStatus.APPROVED -> GisTheme.StatusActive to "Зарегистрирован"
        ApplicationStatus.REJECTED -> GisTheme.StatusDanger to "Отказано"
        ApplicationStatus.SUSPENDED -> GisTheme.StatusWarning to "Приостановлен"
        ApplicationStatus.CANCELLED -> GisTheme.TextMuted to "Аннулирован"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

// ─── Pathway Badge ───

@Composable
fun PathwayBadge(pathway: RegistrationPathway, modifier: Modifier = Modifier) {
    val (color, label) = when (pathway) {
        RegistrationPathway.COMPLIANCE -> GisTheme.PathCompliance to "Соответствие"
        RegistrationPathway.STANDARD -> GisTheme.PathStandard to "Стандартная"
        RegistrationPathway.SIMPLIFIED -> GisTheme.PathSimplified to "Упрощённая"
        RegistrationPathway.CONFIRMATION -> GisTheme.PathConfirmation to "Подтверждение"
        RegistrationPathway.AMENDMENT_WITH_TESTING -> GisTheme.PathAmendment to "Изменения (эксп.)"
        RegistrationPathway.AMENDMENT_WITHOUT_TESTING -> GisTheme.PathAmendment to "Изменения"
        RegistrationPathway.RECOGNITION -> GisTheme.PathRecognition to "Признание"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

// ─── Dashboard Stat Card ───

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GisGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GisTheme.TextPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = GisTheme.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = GisTheme.TextMuted
                )
            }
    }
}

// ─── Application Card ───

@Composable
fun ApplicationCard(
    app: ApplicationDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GisGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.drugTradeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GisTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Заявка №${app.id} • ${app.manufacturerName ?: ""}",
                        fontSize = 13.sp,
                        color = GisTheme.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(app.status)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PathwayBadge(app.pathway)

                if (app.maxWorkingDays != null) {
                    val progress = app.workingDaysElapsed.toFloat() / app.maxWorkingDays.toFloat()
                    val progressColor = when {
                        progress >= 0.9f -> GisTheme.StatusDanger
                        progress >= 0.7f -> GisTheme.StatusWarning
                        else -> GisTheme.StatusActive
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            GisIcons.Schedule,
                            null,
                            tint = progressColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${app.workingDaysElapsed}/${app.maxWorkingDays} дн.",
                            fontSize = 12.sp,
                            color = progressColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (app.isClockPaused) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(GisIcons.Pause, null, tint = GisTheme.StatusPaused, modifier = Modifier.size(14.dp))
                        Text("Часы остановлены", fontSize = 11.sp, color = GisTheme.StatusPaused)
                    }
                }
            }

            if (app.submissionDate != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Подана: ${app.submissionDate}",
                    fontSize = 12.sp,
                    color = GisTheme.TextMuted
                )
            }
    }
}

// ─── Drug Card ───

@Composable
fun DrugCard(
    drug: DrugDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (drug.status) {
        "ACTIVE" -> GisTheme.StatusActive
        "SUSPENDED" -> GisTheme.StatusWarning
        "PENDING" -> GisTheme.StatusPending
        else -> GisTheme.StatusDanger
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = GisTheme.Surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, GisTheme.SurfaceBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            val typeIcon = when (drug.type) {
                DrugType.PHARMACEUTICAL -> GisIcons.Medication
                DrugType.IMMUNOLOGICAL -> GisIcons.Vaccines
                DrugType.DIAGNOSTIC -> GisIcons.Biotech
                DrugType.DISINFECTANT -> GisIcons.CleaningServices
                DrugType.FEED_ADDITIVE -> GisIcons.Restaurant
            }
            val typeColor = when (drug.type) {
                DrugType.PHARMACEUTICAL -> GisTheme.StatusInfo
                DrugType.IMMUNOLOGICAL -> GisTheme.StatusPaused
                DrugType.DIAGNOSTIC -> GisTheme.PathCompliance
                DrugType.DISINFECTANT -> GisTheme.StatusActive
                DrugType.FEED_ADDITIVE -> GisTheme.PathAmendment
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(26.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drug.tradeName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GisTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (drug.inn != null) {
                    Text(
                        text = drug.inn,
                        fontSize = 13.sp,
                        color = GisTheme.TextSecondary,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (drug.registrationNumber != null) {
                        Text(
                            text = drug.registrationNumber,
                            fontSize = 11.sp,
                            color = GisTheme.Accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (drug.manufacturerName != null) {
                        Text(
                            text = "• ${drug.manufacturerName}",
                            fontSize = 11.sp,
                            color = GisTheme.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

// ─── GIS Header Bar ───

@Composable
fun GisTopBar(
    title: String,
    user: UserDto?,
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            GisTheme.PrimaryDark.copy(alpha = 0.94f),
                            GisTheme.Primary.copy(alpha = 0.92f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(GisIcons.Back, "Назад", tint = GisTheme.White)
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GisTheme.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user != null) {
                        Text(
                            text = "${user.fullName} • ${getRoleName(user.role)}",
                            fontSize = 12.sp,
                            color = GisTheme.AccentLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onLogout != null && user != null) {
                    IconButton(onClick = onLogout) {
                        Icon(GisIcons.Logout, "Выйти", tint = GisTheme.White)
                    }
                }
            }
        }
    }
}

// ─── Role Helper ───

fun getRoleName(role: UserRole): String = when (role) {
    UserRole.APPLICANT -> "Заявитель"
    UserRole.COMMITTEE_STAFF -> "Сотрудник КВКН"
    UserRole.NRCV_EXPERT -> "Эксперт НРЦВ"
    UserRole.LAB_ANALYST -> "Лаборант"
    UserRole.BORDER_INSPECTOR -> "Инспектор границы"
    UserRole.WAREHOUSE_CLERK -> "Кладовщик"
    UserRole.FARMER_VET -> "Фермер / Ветеринар"
    UserRole.ADMIN -> "Администратор"
}

fun getRoleColor(role: UserRole): Color = when (role) {
    UserRole.APPLICANT -> GisTheme.RoleApplicant
    UserRole.COMMITTEE_STAFF -> GisTheme.RoleCommittee
    UserRole.NRCV_EXPERT -> GisTheme.RoleExpert
    UserRole.LAB_ANALYST -> GisTheme.RoleLab
    UserRole.BORDER_INSPECTOR -> GisTheme.RoleBorder
    UserRole.WAREHOUSE_CLERK -> GisTheme.RoleWarehouse
    UserRole.FARMER_VET -> GisTheme.RoleFarmer
    UserRole.ADMIN -> GisTheme.Primary
}

fun getRoleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.APPLICANT -> GisIcons.Business
    UserRole.COMMITTEE_STAFF -> GisIcons.AccountBalance
    UserRole.NRCV_EXPERT -> GisIcons.Science
    UserRole.LAB_ANALYST -> GisIcons.Biotech
    UserRole.BORDER_INSPECTOR -> GisIcons.Security
    UserRole.WAREHOUSE_CLERK -> GisIcons.Warehouse
    UserRole.FARMER_VET -> GisIcons.Agriculture
    UserRole.ADMIN -> GisIcons.AdminPanelSettings
}

// ─── Empty State ───

@Composable
fun GisEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    action: Pair<String, () -> Unit>? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                GisTheme.Primary.copy(alpha = 0.22f),
                                GisTheme.SurfaceElevated.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        GisTheme.SurfaceBorder.copy(alpha = 0.8f),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GisTheme.PrimaryLight, modifier = Modifier.size(40.dp))
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = GisTheme.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = GisTheme.TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            if (action != null) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = action.second,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GisTheme.Primary)
                ) {
                    Text(action.first, color = GisTheme.Primary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─── Error Banner ───

@Composable
fun GisErrorBanner(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GisTheme.StatusDanger.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GisTheme.StatusDanger.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                GisIcons.Warning,
                null,
                tint = GisTheme.StatusDanger,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = GisTheme.StatusDanger,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onRetry) {
                    Text("Повторить", color = GisTheme.StatusDanger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Full-screen loading placeholder ───

@Composable
fun GisLoadingScreen(message: String = "Загрузка…") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = GisTheme.Primary,
                    trackColor = GisTheme.SurfaceBorder.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 3.dp
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(GisTheme.Primary, GisTheme.PrimaryLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        GisIcons.HealthAndSafety,
                        null,
                        tint = GisTheme.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(message, fontSize = 14.sp, color = GisTheme.TextSecondary)
        }
    }
}

// ─── Submit Button with loading state ───

@Composable
fun GisSubmitButton(
    label: String,
    icon: ImageVector,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GisTheme.Primary,
            contentColor = GisTheme.White,
            disabledContainerColor = GisTheme.SurfaceElevated,
            disabledContentColor = GisTheme.TextMuted
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = GisTheme.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text("Отправка…", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        } else {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

// ─── Success Snackbar Banner ───

@Composable
fun GisSuccessBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GisTheme.StatusActive.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GisTheme.StatusActive.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(GisIcons.Check, null, tint = GisTheme.StatusActive, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = message, fontSize = 13.sp, color = GisTheme.StatusActive, modifier = Modifier.weight(1f))
        }
    }
}
