package com.dari.dermek.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dari.dermek.api.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
// DRUG REGISTRY SCREEN
// ═══════════════════════════════════════════

@Composable
fun DrugRegistryScreen(
    user: UserDto,
    onBack: () -> Unit,
    onDrugClick: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var drugs by remember { mutableStateOf(emptyList<DrugDto>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadDrugs() {
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                drugs = GisHttpClient.getDrugs(searchQuery.takeIf { it.isNotBlank() })
            } catch (e: Exception) {
                errorMsg = "Ошибка загрузки реестра: ${e.message ?: "Проверьте подключение"}"
                drugs = GisHttpClient.getDrugs(searchQuery.takeIf { it.isNotBlank() }) // fallback to mock
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery) { loadDrugs() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GisTopBar(title = "Реестр ветеринарных препаратов", user = user, onBack = onBack)

        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GisTheme.SurfaceElevated
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text("Поиск по названию, МНН, регистрационному номеру...",
                        color = GisTheme.TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    if (isLoading)
                        CircularProgressIndicator(color = GisTheme.Primary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Icon(GisIcons.Search, null, tint = GisTheme.TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(GisIcons.Close, null, tint = GisTheme.TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GisTheme.Primary,
                    unfocusedBorderColor = GisTheme.SurfaceBorder,
                    cursorColor = GisTheme.Primary,
                    focusedTextColor = GisTheme.TextPrimary,
                    unfocusedTextColor = GisTheme.TextPrimary
                )
            )
        }

        // Error banner
        if (errorMsg != null) {
            GisErrorBanner(
                message = errorMsg!!,
                onRetry = { loadDrugs() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Stats bar
        if (!isLoading && errorMsg == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GisTheme.Surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Найдено: ${drugs.size}",
                        fontSize = 13.sp,
                        color = GisTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    drugs.groupBy { it.type }.forEach { (type, list) ->
                        val typeName = when (type) {
                            DrugType.PHARMACEUTICAL -> "Фарм."
                            DrugType.IMMUNOLOGICAL -> "Иммуно."
                            DrugType.DIAGNOSTIC -> "Диагн."
                            DrugType.DISINFECTANT -> "Дезинф."
                            DrugType.FEED_ADDITIVE -> "Корм.доб."
                        }
                        Text(
                            text = "$typeName: ${list.size}",
                            fontSize = 12.sp,
                            color = GisTheme.TextMuted
                        )
                    }
                }
            }
            HorizontalDivider(color = GisTheme.Divider, thickness = 1.dp)
        }

        // Content
            if (isLoading) {
                GisLoadingScreen("Поиск препаратов…")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = GisLayout.contentMaxWidth)
                        .fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (drugs.isEmpty()) {
                        item {
                            GisEmptyState(
                                icon = GisIcons.SearchOff,
                                title = if (searchQuery.isNotBlank()) "Препараты не найдены" else "Реестр пустой",
                                subtitle = if (searchQuery.isNotBlank())
                                    "По запросу \u00ab$searchQuery\u00bb ничего не найдено. Уточните параметры поиска."
                                else "Зарегистрированные препараты отсутствуют.",
                                action = if (searchQuery.isNotBlank()) "Очистить поиск" to { searchQuery = "" } else null
                            )
                        }
                    } else {
                        items(drugs, key = { it.id }) { drug ->
                            DrugCard(drug = drug, onClick = { onDrugClick(drug.id) })
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// DRUG DETAIL SCREEN
// ═══════════════════════════════════════════

@Composable
fun DrugDetailScreen(
    drugId: Long,
    user: UserDto,
    onBack: () -> Unit
) {
    val drug = remember { GisHttpClient.getDrug(drugId) }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = drug?.tradeName ?: "Препарат", user = user, onBack = onBack)

        if (drug == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Препарат не найден", color = GisTheme.TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.contentMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GisTheme.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = drug.tradeName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GisTheme.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            val statusColor = when (drug.status) {
                                "ACTIVE" -> GisTheme.StatusActive
                                else -> GisTheme.StatusWarning
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (drug.status == "ACTIVE") "Действующий" else drug.status,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }

                        if (drug.inn != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "МНН: ${drug.inn}",
                                fontSize = 15.sp,
                                color = GisTheme.TextSecondary
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = GisTheme.Divider)
                        Spacer(Modifier.height(16.dp))

                        // Details grid
                        DetailRow("Регистрационный номер", drug.registrationNumber ?: "—")
                        DetailRow("Дата регистрации", drug.registrationDate ?: "—")
                        DetailRow("Действителен до", drug.expiryDate ?: "—")
                        DetailRow("Тип", when (drug.type) {
                            DrugType.PHARMACEUTICAL -> "Фармацевтический"
                            DrugType.IMMUNOLOGICAL -> "Иммунологический"
                            DrugType.DIAGNOSTIC -> "Диагностический"
                            DrugType.DISINFECTANT -> "Дезинфицирующее средство"
                            DrugType.FEED_ADDITIVE -> "Кормовая добавка"
                        })
                        DetailRow("Лекарственная форма", drug.dosageForm ?: "—")
                        DetailRow("Производитель", drug.manufacturerName ?: "—")

                        if (drug.activeSubstances.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Действующие вещества:", fontSize = 13.sp,
                                color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
                            drug.activeSubstances.forEach { substance ->
                                Text("  • $substance", fontSize = 14.sp,
                                    color = GisTheme.TextPrimary)
                            }
                        }

                        if (drug.targetAnimals.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Виды животных:", fontSize = 13.sp,
                                color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
                            Text(
                                text = drug.targetAnimals.joinToString(", "),
                                fontSize = 14.sp,
                                color = GisTheme.TextPrimary
                            )
                        }

                        if (drug.isAnnex8) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GisTheme.StatusInfo.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(GisIcons.Info, null, tint = GisTheme.StatusInfo,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Приложение №8 ЕАЭС — ускоренные сроки экспертизы",
                                        fontSize = 12.sp, color = GisTheme.StatusInfo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = GisTheme.TextMuted,
            modifier = Modifier.weight(0.4f))
        Text(value, fontSize = 14.sp, color = GisTheme.TextPrimary,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End)
    }
}

// ═══════════════════════════════════════════
// APPLICATIONS SCREEN
// ═══════════════════════════════════════════

@Composable
fun ApplicationsScreen(
    user: UserDto,
    onBack: () -> Unit,
    onAppClick: (Long) -> Unit,
    onNewApp: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ApplicationStatus?>(null) }
    var apps by remember { mutableStateOf(emptyList<ApplicationDto>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadApps() {
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                apps = GisHttpClient.getApplications(status = selectedFilter)
            } catch (e: Exception) {
                errorMsg = "Ошибка загрузки: ${e.message}"
                apps = GisHttpClient.getApplications(status = selectedFilter)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedFilter) { loadApps() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GisTopBar(title = "Заявки на регистрацию", user = user, onBack = onBack)

        // Filter chips
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GisTheme.SurfaceElevated
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Все", fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GisTheme.Primary.copy(alpha = 0.2f),
                        selectedLabelColor = GisTheme.PrimaryLight
                    )
                )
                listOf(
                    ApplicationStatus.SUBMITTED to "Поданные",
                    ApplicationStatus.LAB_TESTING to "Испытания",
                    ApplicationStatus.EXPERT_CONCLUSION to "Экспертиза",
                    ApplicationStatus.APPROVED to "Одобренные"
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = if (selectedFilter == status) null else status },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GisTheme.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = GisTheme.PrimaryLight
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = GisTheme.Divider, thickness = 1.dp)

        if (errorMsg != null) {
            GisErrorBanner(
                message = errorMsg!!,
                onRetry = { loadApps() },
                modifier = Modifier.padding(16.dp)
            )
        }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    GisLoadingScreen("Загрузка заявок…")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = GisLayout.contentMaxWidth)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.TopCenter),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (apps.isEmpty()) {
                            item {
                                val filterName = when (selectedFilter) {
                                    ApplicationStatus.SUBMITTED -> "поданных"
                                    ApplicationStatus.LAB_TESTING -> "на испытаниях"
                                    ApplicationStatus.EXPERT_CONCLUSION -> "на экспертизе"
                                    ApplicationStatus.APPROVED -> "одобренных"
                                    else -> null
                                }
                                GisEmptyState(
                                    icon = GisIcons.Inbox,
                                    title = if (filterName != null) "Нет $filterName заявок" else "Заявок пока нет",
                                    subtitle = if (user.role == UserRole.APPLICANT)
                                        "Подайте первую заявку на регистрацию ветеринарного препарата."
                                    else "Заявки, соответствующие выбранному фильтру, отсутствуют.",
                                    action = if (user.role == UserRole.APPLICANT) "Подать заявку" to onNewApp else null
                                )
                            }
                        } else {
                            items(apps, key = { it.id }) { app ->
                                ApplicationCard(
                                    app = app,
                                    onClick = { onAppClick(app.id) }
                                )
                            }
                        }
                    }
                }

                if ((user.role == UserRole.APPLICANT || user.role == UserRole.ADMIN) && !isLoading) {
                    FloatingActionButton(
                        onClick = onNewApp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = GisTheme.Primary,
                        contentColor = GisTheme.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(GisIcons.Add, "Новая заявка")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// APPLICATION DETAIL SCREEN
// ═══════════════════════════════════════════

@Composable
fun ApplicationDetailScreen(
    appId: Long,
    user: UserDto,
    onBack: () -> Unit
) {
    val app = remember { GisHttpClient.getApplication(appId) }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Заявка №$appId", user = user, onBack = onBack)

        if (app == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Заявка не найдена", color = GisTheme.TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.contentMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GisTheme.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.drugTradeName, fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                                Text(app.manufacturerName ?: "", fontSize = 14.sp,
                                    color = GisTheme.TextSecondary)
                            }
                            StatusBadge(app.status)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PathwayBadge(app.pathway)
                            if (app.isClockPaused) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GisTheme.StatusPaused.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(GisIcons.Pause, null, tint = GisTheme.StatusPaused,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Часы остановлены", fontSize = 11.sp,
                                            color = GisTheme.StatusPaused, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timeline progress
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GisTheme.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Прогресс рассмотрения", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                        Spacer(Modifier.height(16.dp))

                        if (app.maxWorkingDays != null) {
                            val progress = (app.workingDaysElapsed.toFloat() /
                                app.maxWorkingDays.toFloat()).coerceIn(0f, 1f)
                            val progressColor = when {
                                progress >= 0.9f -> GisTheme.StatusDanger
                                progress >= 0.7f -> GisTheme.StatusWarning
                                else -> GisTheme.StatusActive
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${app.workingDaysElapsed} из ${app.maxWorkingDays} рабочих дней",
                                    fontSize = 14.sp, color = GisTheme.TextPrimary,
                                    fontWeight = FontWeight.Medium)
                                Text("${(progress * 100).toInt()}%", fontSize = 14.sp,
                                    color = progressColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = progressColor,
                                trackColor = GisTheme.SurfaceBorder
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = GisTheme.Divider)
                        Spacer(Modifier.height(16.dp))

                        DetailRow("Подана", app.submissionDate ?: "—")
                        DetailRow("Крайний срок", app.deadlineDate ?: "Рассчитывается...")
                        DetailRow("Запросов доп. информации", app.queryCount.toString())
                    }
                }
            }

            // Application info
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GisTheme.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Сведения о заявке", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        DetailRow("Заявитель", app.applicantName ?: "—")
                        DetailRow("Тип препарата", when (app.drugType) {
                            DrugType.PHARMACEUTICAL -> "Фармацевтический"
                            DrugType.IMMUNOLOGICAL -> "Иммунологический"
                            DrugType.DIAGNOSTIC -> "Диагностический"
                            DrugType.DISINFECTANT -> "Дезинфицирующее средство"
                            DrugType.FEED_ADDITIVE -> "Кормовая добавка"
                        })
                        DetailRow("Производитель", app.manufacturerName ?: "—")
                        if (app.notes != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("Примечание:", fontSize = 13.sp,
                                color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
                            Text(app.notes, fontSize = 14.sp, color = GisTheme.TextPrimary)
                        }
                    }
                }
            }

            // Workflow steps
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = GisTheme.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Этапы рассмотрения", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                        Spacer(Modifier.height(16.dp))

                        val steps = listOf(
                            "Подача заявки" to ApplicationStatus.SUBMITTED,
                            "Проверка комплектности" to ApplicationStatus.COMPLETENESS_CHECK,
                            "Запрос образцов" to ApplicationStatus.SAMPLES_REQUESTED,
                            "Лабораторные испытания" to ApplicationStatus.LAB_TESTING,
                            "Экспертное заключение" to ApplicationStatus.EXPERT_CONCLUSION,
                            "Рассмотрение Комитетом" to ApplicationStatus.COMMITTEE_REVIEW,
                            "Решение" to ApplicationStatus.APPROVED
                        )

                        val currentIndex = steps.indexOfFirst { it.second == app.status }
                            .takeIf { it >= 0 } ?: steps.size

                        steps.forEachIndexed { index, (label, _) ->
                            val isComplete = index < currentIndex
                            val isCurrent = index == currentIndex
                            val color = when {
                                isComplete -> GisTheme.StatusActive
                                isCurrent -> GisTheme.StatusInfo
                                else -> GisTheme.TextMuted
                            }

                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = if (isCurrent) 0.2f else 0.1f))
                                        .then(if (isCurrent) Modifier.border(
                                            2.dp, color, CircleShape
                                        ) else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isComplete) {
                                        Icon(GisIcons.Check, null, tint = color,
                                            modifier = Modifier.size(14.dp))
                                    } else {
                                        Text("${index + 1}", fontSize = 10.sp,
                                            color = color, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    color = color,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// NEW APPLICATION SCREEN
// ═══════════════════════════════════════════

@Composable
fun NewApplicationScreen(
    user: UserDto,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    var selectedPathway by remember { mutableStateOf<RegistrationPathway?>(null) }
    var drugName by remember { mutableStateOf("") }
    var selectedDrugType by remember { mutableStateOf(DrugType.PHARMACEUTICAL) }
    var manufacturerName by remember { mutableStateOf("") }
    var activeSubstances by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val prohibitedSubstances = remember {
        listOf(
            "chloramphenicol", "хлорамфеникол",
            "chloroform", "хлороформ",
            "chlorpromazine", "хлорпромазин",
            "stilbene", "стильбен", "стильбены",
            "abamectin", "абамектин",
            "avilamycin", "авиламицин",
            "azaglynapharelin", "азаглинарелин"
        )
    }

    val detectedProhibited = remember(activeSubstances) {
        activeSubstances.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .filter { sub -> prohibitedSubstances.any { sub.contains(it) || it.contains(sub) } }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Подача заявки", user = user, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.formMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Choose pathway
            item {
                Text("Шаг 1: Выберите процедуру", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Выберите тип регистрационной процедуры по правилам ЕАЭС",
                    fontSize = 13.sp, color = GisTheme.TextSecondary)
            }

            items(GisHttpClient.pathways) { pw ->
                val pathway = RegistrationPathway.valueOf(pw.key)
                val isSelected = selectedPathway == pathway
                val pwColor = when (pathway) {
                    RegistrationPathway.COMPLIANCE -> GisTheme.PathCompliance
                    RegistrationPathway.STANDARD -> GisTheme.PathStandard
                    RegistrationPathway.SIMPLIFIED -> GisTheme.PathSimplified
                    RegistrationPathway.CONFIRMATION -> GisTheme.PathConfirmation
                    RegistrationPathway.AMENDMENT_WITH_TESTING,
                    RegistrationPathway.AMENDMENT_WITHOUT_TESTING -> GisTheme.PathAmendment
                    RegistrationPathway.RECOGNITION -> GisTheme.PathRecognition
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPathway = pathway },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) pwColor.copy(alpha = 0.12f) else GisTheme.Surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) pwColor else GisTheme.SurfaceBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedPathway = pathway },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = pwColor
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pw.nameRu, fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) pwColor else GisTheme.TextPrimary)
                            Text(pw.nameEn, fontSize = 12.sp, color = GisTheme.TextMuted)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
 Text("${pw.maxDaysStandard} дн.", fontSize = 12.sp,
                                    color = GisTheme.TextSecondary)
                                if (pw.sampleRequired) {
 Text("Образцы", fontSize = 12.sp,
                                        color = GisTheme.StatusPending)
                                } else {
 Text("Документы", fontSize = 12.sp,
                                        color = GisTheme.StatusActive)
                                }
                                if (pw.deadline != null) {
 Text("до ${pw.deadline}", fontSize = 12.sp,
                                        color = GisTheme.StatusWarning)
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Drug info
            if (selectedPathway != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Шаг 2: Сведения о препарате", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                }

                item {
                    OutlinedTextField(
                        value = drugName,
                        onValueChange = { drugName = it },
                        label = { Text("Торговое наименование") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            cursorColor = GisTheme.Primary,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary,
                            focusedLabelColor = GisTheme.Primary,
                            unfocusedLabelColor = GisTheme.TextMuted
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = manufacturerName,
                        onValueChange = { manufacturerName = it },
                        label = { Text("Производитель") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            cursorColor = GisTheme.Primary,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary,
                            focusedLabelColor = GisTheme.Primary,
                            unfocusedLabelColor = GisTheme.TextMuted
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = activeSubstances,
                        onValueChange = { activeSubstances = it },
                        label = { Text("Действующие вещества (через запятую)") },
                        placeholder = { Text("Например: Амоксициллин, Хлорамфеникол") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (detectedProhibited.isNotEmpty()) GisTheme.StatusDanger else GisTheme.Primary,
                            unfocusedBorderColor = if (detectedProhibited.isNotEmpty()) GisTheme.StatusDanger else GisTheme.SurfaceBorder,
                            cursorColor = GisTheme.Primary,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary,
                            focusedLabelColor = if (detectedProhibited.isNotEmpty()) GisTheme.StatusDanger else GisTheme.Primary,
                            unfocusedLabelColor = GisTheme.TextMuted
                        )
                    )
                }

                if (detectedProhibited.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GisTheme.StatusDanger.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, GisTheme.StatusDanger),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    GisIcons.Warning,
                                    contentDescription = null,
                                    tint = GisTheme.StatusDanger,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Запрещенное вещество!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GisTheme.StatusDanger
                                    )
                                    Text(
                                        text = "Вещество '${detectedProhibited.first()}' запрещено к регистрации для использования у продуктивных животных на территории РК (согласно разделу 11 правил).",
                                        fontSize = 12.sp,
                                        color = GisTheme.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Примечание (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            cursorColor = GisTheme.Primary,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary,
                            focusedLabelColor = GisTheme.Primary,
                            unfocusedLabelColor = GisTheme.TextMuted
                        )
                    )
                }

                // Submit button
                item {
                    Spacer(Modifier.height(8.dp))
                    if (submitError != null) {
                        GisErrorBanner(message = submitError!!, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    if (submitSuccess) {
                        GisSuccessBanner(
                            message = "Заявка на регистрацию «$drugName» успешно подана!",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    GisSubmitButton(
                        label = "Подать заявку",
                        icon = GisIcons.Send,
                        isLoading = isSubmitting,
                        enabled = drugName.isNotBlank() && selectedPathway != null && detectedProhibited.isEmpty(),
                        onClick = {
                            if (drugName.isNotBlank() && selectedPathway != null && detectedProhibited.isEmpty()) {
                                isSubmitting = true
                                submitError = null
                                submitSuccess = false
                                scope.launch {
                                    try {
                                        GisHttpClient.submitApplication(
                                            ApplicationDto(
                                                applicantId = user.id,
                                                applicantName = user.fullName,
                                                pathway = selectedPathway!!,
                                                drugTradeName = drugName,
                                                drugType = selectedDrugType,
                                                manufacturerName = manufacturerName.takeIf { it.isNotBlank() },
                                                notes = notes.takeIf { it.isNotBlank() }
                                            )
                                        )
                                        submitSuccess = true
                                        kotlinx.coroutines.delay(1500)
                                        onSubmitted()
                                    } catch (e: Exception) {
                                        submitError = "Ошибка подачи: ${e.message ?: "Повторите позже"}"
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// QR SCANNER SCREEN
// ═══════════════════════════════════════════

@Composable
fun QrScannerScreen(
    user: UserDto,
    onBack: () -> Unit,
    onResult: (QrScanResultDto) -> Unit
) {
    var manualCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Сканер QR-кода", user = user, onBack = onBack)

        Column(
            modifier = Modifier
                .widthIn(max = GisLayout.formMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Camera placeholder
            Surface(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(20.dp),
                color = GisTheme.SurfaceElevated,
                border = BorderStroke(2.dp, GisTheme.SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        GisIcons.QrCodeScanner,
                        null,
                        tint = GisTheme.TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Наведите камеру на QR-код", fontSize = 16.sp,
                        color = GisTheme.TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("На мобильном устройстве камера\nзапустится автоматически",
                        fontSize = 13.sp, color = GisTheme.TextMuted,
                        textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Или введите код вручную:", fontSize = 14.sp,
                color = GisTheme.TextSecondary)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = manualCode,
                onValueChange = { manualCode = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("VET-FMD-A102-001", color = GisTheme.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GisTheme.Primary,
                    unfocusedBorderColor = GisTheme.SurfaceBorder,
                    cursorColor = GisTheme.Primary,
                    focusedTextColor = GisTheme.TextPrimary,
                    unfocusedTextColor = GisTheme.TextPrimary
                )
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val result = GisHttpClient.scanQr(manualCode)
                    onResult(result)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = manualCode.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GisTheme.Primary,
                    contentColor = GisTheme.White
                )
            ) {
                Icon(GisIcons.Search, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Проверить", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

            // Demo codes
            Text("Демо-коды для тестирования:", fontSize = 13.sp,
                color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            listOf(
"VET-FMD-A102-001" to "Вакцина ящур",
"VET-ALB-B205-003" to "Альбендазол",
"VET-AMOX-C301-007" to "Амоксициллин",
"VET-FAKE-0000" to "Контрафакт"
            ).forEach { (code, desc) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { manualCode = code },
                    shape = RoundedCornerShape(8.dp),
                    color = GisTheme.Surface
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(code, fontSize = 13.sp, color = GisTheme.Accent,
                            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(desc, fontSize = 12.sp, color = GisTheme.TextMuted)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════
// BATCH LOOKUP (QR RESULT) SCREEN
// ═══════════════════════════════════════════

@Composable
fun BatchLookupScreen(
    result: QrScanResultDto?,
    user: UserDto,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Результат проверки", user = user, onBack = onBack)

        if (result == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет данных", color = GisTheme.TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.formMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Result status
            item {
                val (bgColor, icon, title) = if (result.found && result.alerts.isEmpty()) {
                    Triple(GisTheme.StatusActive, GisIcons.VerifiedUser, "Препарат подлинный")
                } else if (result.found && result.alerts.isNotEmpty()) {
                    Triple(GisTheme.StatusWarning, GisIcons.Warning, "Обнаружены проблемы")
                } else {
                    Triple(GisTheme.StatusDanger, GisIcons.GppBad, "КОНТРАФАКТ!")
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor.copy(alpha = 0.12f),
                    border = BorderStroke(2.dp, bgColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, null, tint = bgColor, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = bgColor)

                        if (result.alerts.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            result.alerts.forEach { alert ->
                                val alertText = when (alert) {
"COLD_CHAIN_VIOLATION" -> "Нарушен температурный режим"
"BATCH_SUSPENDED" -> "Партия приостановлена"
"DRUG_NOT_ACTIVE" -> "Регистрация недействительна"
"COUNTERFEIT_ALERT" -> "Препарат не найден в реестре!"
                                    else -> alert
                                }
                                Text(alertText, fontSize = 14.sp, color = bgColor,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Drug info
            if (result.drug != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = GisTheme.Surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Сведения о препарате", fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                            Spacer(Modifier.height(12.dp))
                            DetailRow("Наименование", result.drug.tradeName)
                            DetailRow("МНН", result.drug.inn ?: "—")
                            DetailRow("Рег. номер", result.drug.registrationNumber ?: "—")
                            DetailRow("Производитель", result.drug.manufacturerName ?: "—")
                            DetailRow("Форма", result.drug.dosageForm ?: "—")
                        }
                    }
                }
            }

            // Batch info
            if (result.batch != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = GisTheme.Surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Сведения о партии", fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                            Spacer(Modifier.height(12.dp))
                            DetailRow("Номер партии", result.batch.batchNumber)
                            DetailRow("Статус", when (result.batch.status) {
                                BatchStatus.ACTIVE -> "В обороте"
                                BatchStatus.SUSPENDED -> "Приостановлена"
                                BatchStatus.RECALLED -> "Отозвана"
                                BatchStatus.DESTROYED -> "Уничтожена"
                                BatchStatus.EXPIRED -> "Просрочена"
                            })
                            DetailRow("Холодовая цепь",
                                if (result.coldChainIntact) "Не нарушена" else "Нарушена")
                        }
                    }
                }
            }
        }
    }
}


// 🌐 PHARMACOVIGILANCE (SAFETY REPORTS) SCREEN
// ═══════════════════════════════════════════

@Composable
fun SafetyReportsScreen(
    user: UserDto,
    onBack: () -> Unit,
    onNewReport: () -> Unit
) {
    var reports by remember { mutableStateOf(emptyList<AdverseEventDto>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var selectedReport by remember { mutableStateOf<AdverseEventDto?>(null) }
    var actionNotes by remember { mutableStateOf("") }

    fun refreshReports() {
        scope.launch {
            isLoading = true
            reports = GisHttpClient.getAdverseEvents()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshReports()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Мониторинг безопасности (Фармаконадзор)", user = user, onBack = onBack)

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GisTheme.Primary)
                }
            } else if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Сообщения о побочных реакциях отсутствуют", color = GisTheme.TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = GisLayout.contentMaxWidth)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.TopCenter),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Журнал регистрации побочных действий (Приказ №18-02/158)", 
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                    }

                    items(reports) { report ->
                        val statusColor = when (report.status) {
                            "REGISTERED" -> GisTheme.StatusInfo
                            "UNDER_REVIEW" -> GisTheme.StatusPending
                            "TESTING_ORDERED" -> GisTheme.StatusPaused
                            "CERTIFICATE_REVOKED" -> GisTheme.StatusDanger
                            else -> GisTheme.TextMuted
                        }
                        val statusText = when (report.status) {
                            "REGISTERED" -> "Зарегистрировано"
                            "UNDER_REVIEW" -> "На рассмотрении"
                            "TESTING_ORDERED" -> "Назначена экспертиза"
                            "CERTIFICATE_REVOKED" -> "Отозвано РУ"
                            else -> report.status
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (user.role == UserRole.NRCV_EXPERT || user.role == UserRole.COMMITTEE_STAFF) {
                                    selectedReport = report
                                    actionNotes = report.measuresTaken ?: ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = GisTheme.Surface,
                            border = BorderStroke(1.dp, GisTheme.SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Сообщение №${report.id}", fontSize = 13.sp, color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = statusColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Text("Препарат: ${report.drugName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                                if (!report.batchNumber.isNullOrBlank()) {
                                    Text("Партия/серия: ${report.batchNumber}", fontSize = 12.sp, color = GisTheme.TextSecondary)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(report.description, fontSize = 13.sp, color = GisTheme.TextPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = GisTheme.Divider)
                                Spacer(Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Отправитель", fontSize = 11.sp, color = GisTheme.TextMuted)
                                        Text(report.reporterName, fontSize = 12.sp, color = GisTheme.TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Дата выявления", fontSize = 11.sp, color = GisTheme.TextMuted)
                                        Text(report.detectionDate, fontSize = 12.sp, color = GisTheme.TextSecondary)
                                    }
                                }

                                if (!report.measuresTaken.isNullOrBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GisTheme.Background,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Принятые меры / Решение регулятора:", fontSize = 11.sp, color = GisTheme.Accent, fontWeight = FontWeight.Bold)
                                            Text(report.measuresTaken, fontSize = 12.sp, color = GisTheme.TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating action button for applicants
            if (user.role == UserRole.APPLICANT || user.role == UserRole.FARMER_VET) {
                FloatingActionButton(
                    onClick = onNewReport,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                    containerColor = GisTheme.Primary,
                    contentColor = GisTheme.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(GisIcons.Warning, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Сообщить о побочной реакции", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog for Expert Actions
    if (selectedReport != null) {
        AlertDialog(
            onDismissRequest = { selectedReport = null },
            title = { Text("Рассмотрение сообщения №${selectedReport!!.id}", color = GisTheme.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Препарат: ${selectedReport!!.drugName}", fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                    Text(selectedReport!!.description, fontSize = 13.sp, color = GisTheme.TextSecondary)
                    
                    OutlinedTextField(
                        value = actionNotes,
                        onValueChange = { actionNotes = it },
                        label = { Text("Принятые меры / Предписание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Action 1: Order testing
                    Button(
                        onClick = {
                            scope.launch {
                                GisHttpClient.submitAdverseEventAction(
                                    selectedReport!!.id,
                                    selectedReport!!.copy(
                                        status = "TESTING_ORDERED",
                                        measuresTaken = actionNotes.takeIf { it.isNotBlank() } ?: "Назначена дополнительная апробация/экспертиза образцов препарата."
                                    )
                                )
                                selectedReport = null
                                refreshReports()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GisTheme.StatusPending)
                    ) {
                        Text("Назначить экспертизу", fontSize = 12.sp)
                    }

                    // Action 2: Revoke registry certificate
                    Button(
                        onClick = {
                            scope.launch {
                                GisHttpClient.submitAdverseEventAction(
                                    selectedReport!!.id,
                                    selectedReport!!.copy(
                                        status = "CERTIFICATE_REVOKED",
                                        measuresTaken = actionNotes.takeIf { it.isNotBlank() } ?: "Приказ КВКН: Отозвать регистрационное удостоверение препарата, запретить производство, импорт и реализацию."
                                    )
                                )
                                selectedReport = null
                                refreshReports()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GisTheme.StatusDanger)
                    ) {
                        Text("Отозвать РУ", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReport = null }) {
                    Text("Отмена", color = GisTheme.TextSecondary)
                }
            },
            containerColor = GisTheme.Surface
        )
    }
}

@Composable
fun NewSafetyReportScreen(
    user: UserDto,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    var drugName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var dosageForm by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf(user.fullName) }
    var reporterOrg by remember { mutableStateOf(user.organization ?: "") }
    var phone by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Новое сообщение о побочном действии", user = user, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.formMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Карта-сообщение о подозрении на побочное действие (Приказ МСХ РК №18-02/158)",
                    fontSize = 13.sp, color = GisTheme.TextSecondary)
            }

            item {
                OutlinedTextField(
                    value = drugName,
                    onValueChange = { drugName = it },
                    label = { Text("Торговое наименование препарата") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Номер партии/серии") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = dosageForm,
                        onValueChange = { dosageForm = it },
                        label = { Text("Форма выпуска") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание побочного явления / клинические симптомы") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                HorizontalDivider(color = GisTheme.Divider)
                Spacer(Modifier.height(4.dp))
                Text("Сведения о заявителе (источник сообщения)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
            }

            item {
                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Ф.И.О. отправителя") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = reporterOrg,
                        onValueChange = { reporterOrg = it },
                        label = { Text("Организация") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                if (submitError != null) {
                    GisErrorBanner(message = submitError!!, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (submitSuccess) {
                    GisSuccessBanner("Сообщение о побочном действии препарата \u00ab$drugName\u00bb успешно зарегистрировано!", modifier = Modifier.padding(bottom = 8.dp))
                }
                GisSubmitButton(
                    label = "Отправить сообщение",
                    icon = GisIcons.Send,
                    isLoading = isSubmitting,
                    enabled = drugName.isNotBlank() && description.isNotBlank(),
                    onClick = {
                        if (drugName.isNotBlank() && description.isNotBlank()) {
                            isSubmitting = true
                            submitError = null
                            submitSuccess = false
                            scope.launch {
                                try {
                                    GisHttpClient.submitAdverseEvent(
                                        AdverseEventDto(
                                            reporterName = reporterName,
                                            reporterOrg = reporterOrg.takeIf { it.isNotBlank() },
                                            phone = phone.takeIf { it.isNotBlank() },
                                            drugName = drugName,
                                            batchNumber = batchNumber.takeIf { it.isNotBlank() },
                                            dosageForm = dosageForm.takeIf { it.isNotBlank() },
                                            description = description,
                                            detectionDate = "2026-07-14",
                                            status = "REGISTERED"
                                        )
                                    )
                                    submitSuccess = true
                                    kotlinx.coroutines.delay(1500)
                                    onSubmitted()
                                } catch (e: Exception) {
                                    submitError = "Ошибка отправки: ${e.message ?: "Повторите позже"}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// 🌐 DISPOSAL & DESTRUCTION LOG (ORDER NO. 16-07/443) SCREEN
// ═════════════════════════════════════════════════════════

@Composable
fun DisposalLogScreen(
    user: UserDto,
    onBack: () -> Unit,
    onNewAct: () -> Unit
) {
    var acts by remember { mutableStateOf(emptyList<DestructionActDto>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refreshActs() {
        scope.launch {
            isLoading = true
            acts = GisHttpClient.getDestructionActs()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshActs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Списание и утилизация препаратов", user = user, onBack = onBack)

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GisTheme.Primary)
                }
            } else if (acts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Акты уничтожения отсутствуют", color = GisTheme.TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = GisLayout.contentMaxWidth)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.TopCenter),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Реестр актов об уничтожении ветеринарных препаратов (Приказ №16-07/443)",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GisTheme.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                    }

                    items(acts) { act ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = GisTheme.Surface,
                            border = BorderStroke(1.dp, GisTheme.SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Акт утилизации №${act.id}", fontSize = 13.sp, color = GisTheme.TextMuted, fontWeight = FontWeight.Medium)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GisTheme.StatusDanger.copy(alpha = 0.12f)
                                    ) {
                                        Text("УНИЧТОЖЕНО", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GisTheme.StatusDanger)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Text("Препарат: ${act.drugName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                                Text("Серия/партия: ${act.batchNumber}  •  Объем: ${act.volume} шт.", fontSize = 13.sp, color = GisTheme.TextSecondary)

                                Spacer(Modifier.height(8.dp))
                                val groundsText = when (act.grounds) {
                                    "EXPIRED" -> "Истечение срока годности"
                                    "LAB_REJECTED" -> "Брак по результатам лабораторных испытаний"
                                    "SPOILED" -> "Визуальные признаки порчи / потеря герметичности"
                                    "NON_COMPLIANT" -> "Несоответствие стандартам ЕАЭС"
                                    else -> act.grounds
                                }
                                DetailRow("Основание для списания", groundsText)
                                DetailRow("Метод денатурации (обязательно)", act.denaturationMethod)
                                DetailRow("Способ уничтожения", act.destructionMethod)
                                DetailRow("Дата уничтожения", act.destructionDate)

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = GisTheme.Divider)
                                Spacer(Modifier.height(10.dp))

                                if (act.isPrivateSector) {
                                    Text("Сектор: Частный (утилизация проведена владельцем самостоятельно)", 
                                        fontSize = 12.sp, color = GisTheme.TextMuted, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Column {
                                        Text("Состав комиссии государственного органа:", fontSize = 11.sp, color = GisTheme.Accent, fontWeight = FontWeight.Bold)
                                        Text(act.commissionMembers.joinToString(", "), fontSize = 12.sp, color = GisTheme.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Write-off action button for warehouse and farmer vet roles
            if (user.role == UserRole.WAREHOUSE_CLERK || user.role == UserRole.FARMER_VET || user.role == UserRole.APPLICANT) {
                FloatingActionButton(
                    onClick = onNewAct,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                    containerColor = GisTheme.Primary,
                    contentColor = GisTheme.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(GisIcons.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Списать / Создать акт", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewDisposalActScreen(
    user: UserDto,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    var drugName by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }
    var selectedGrounds by remember { mutableStateOf("EXPIRED") }
    var denaturationMethod by remember { mutableStateOf("") }
    var destructionMethod by remember { mutableStateOf("") }
    var isPrivateSector by remember { mutableStateOf(user.role == UserRole.FARMER_VET || user.role == UserRole.APPLICANT) }
    var commission1 by remember { mutableStateOf("") }
    var commission2 by remember { mutableStateOf("") }
    var commission3 by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        GisTopBar(title = "Создание акта об уничтожении", user = user, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .widthIn(max = GisLayout.formMaxWidth)
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Форма акта о списании и уничтожении (Приказ МСХ РК №16-07/443)",
                    fontSize = 13.sp, color = GisTheme.TextSecondary)
            }

            item {
                OutlinedTextField(
                    value = drugName,
                    onValueChange = { drugName = it },
                    label = { Text("Торговое наименование препарата") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Номер партии/серии") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = volume,
                        onValueChange = { volume = it },
                        label = { Text("Объем (кол-во)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                }
            }

            item {
                Text("Основание для списания и утилизации", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val grounds = listOf(
                        "EXPIRED" to "Истечение срока годности",
                        "LAB_REJECTED" to "Брак по результатам лаб. испытаний",
                        "SPOILED" to "Визуальные признаки порчи / брак упаковки",
                        "NON_COMPLIANT" to "Несоответствие правилам ЕАЭС"
                    )
                    grounds.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedGrounds = key }
                        ) {
                            RadioButton(
                                selected = selectedGrounds == key,
                                onClick = { selectedGrounds = key },
                                colors = RadioButtonDefaults.colors(selectedColor = GisTheme.Primary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, color = GisTheme.TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = denaturationMethod,
                    onValueChange = { denaturationMethod = it },
                    label = { Text("Метод денатурации (смешивание с керосином, известью, phenol и т.д.)") },
                    placeholder = { Text("Обязательно согласно приказу перед утилизацией...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = destructionMethod,
                    onValueChange = { destructionMethod = it },
                    label = { Text("Метод физического уничтожения") },
                    placeholder = { Text("Например: термическое уничтожение, сжигание") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GisTheme.Primary,
                        unfocusedBorderColor = GisTheme.SurfaceBorder,
                        focusedTextColor = GisTheme.TextPrimary,
                        unfocusedTextColor = GisTheme.TextPrimary
                    )
                )
            }

            item {
                HorizontalDivider(color = GisTheme.Divider)
                Spacer(Modifier.height(4.dp))
                Text("Сектор списания", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPrivateSector,
                        onCheckedChange = { isPrivateSector = it },
                        colors = CheckboxDefaults.colors(checkedColor = GisTheme.Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Частный сектор (самостоятельное уничтожение владельцем)", color = GisTheme.TextPrimary)
                }
            }

            if (!isPrivateSector) {
                item {
                    Text("Состав государственной комиссии (минимум 3 участника)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GisTheme.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commission1,
                        onValueChange = { commission1 = it },
                        label = { Text("Ф.И.О. Председателя комиссии") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commission2,
                        onValueChange = { commission2 = it },
                        label = { Text("Ф.И.О. Члена комиссии 1") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commission3,
                        onValueChange = { commission3 = it },
                        label = { Text("Ф.И.О. Члена комиссии 2") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GisTheme.Primary,
                            unfocusedBorderColor = GisTheme.SurfaceBorder,
                            focusedTextColor = GisTheme.TextPrimary,
                            unfocusedTextColor = GisTheme.TextPrimary
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                if (submitError != null) {
                    GisErrorBanner(message = submitError!!, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (submitSuccess) {
                    GisSuccessBanner("Акт утилизации «$drugName» успешно зарегистрирован!", modifier = Modifier.padding(bottom = 8.dp))
                }
                GisSubmitButton(
                    label = "Подтвердить и уничтожить",
                    icon = GisIcons.Delete,
                    isLoading = isSubmitting,
                    enabled = drugName.isNotBlank() && batchNumber.isNotBlank() && volume.isNotBlank() && denaturationMethod.isNotBlank(),
                    onClick = {
                        if (drugName.isNotBlank() && batchNumber.isNotBlank() && volume.isNotBlank() && denaturationMethod.isNotBlank()) {
                            isSubmitting = true
                            submitError = null
                            submitSuccess = false
                            val volDouble = volume.toDoubleOrNull() ?: 1.0
                            val members = if (isPrivateSector) emptyList() else listOf(
                                commission1.takeIf { it.isNotBlank() } ?: "Представитель КВКН (Председатель)",
                                commission2.takeIf { it.isNotBlank() } ?: "Представитель НРЦВ",
                                commission3.takeIf { it.isNotBlank() } ?: "Уполномоченный инспектор"
                            )
                            scope.launch {
                                try {
                                    GisHttpClient.submitDestructionAct(
                                        DestructionActDto(
                                            drugName = drugName,
                                            batchNumber = batchNumber,
                                            volume = volDouble,
                                            grounds = selectedGrounds,
                                            denaturationMethod = denaturationMethod,
                                            destructionMethod = destructionMethod.takeIf { it.isNotBlank() } ?: "Сжигание",
                                            destructionDate = "2026-07-14",
                                            isPrivateSector = isPrivateSector,
                                            commissionMembers = members
                                        )
                                    )
                                    submitSuccess = true
                                    kotlinx.coroutines.delay(1500)
                                    onSubmitted()
                                } catch (e: Exception) {
                                    submitError = "Ошибка регистрации акта: ${e.message ?: "Повторите позже"}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

