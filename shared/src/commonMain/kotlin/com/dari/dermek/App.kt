package com.dari.dermek

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

@Composable
fun App() {
    // Launch the GIS platform application
    com.dari.dermek.ui.GisApp()
}

class MainScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val platform = remember { getPlatform() }
        val screenModel = rememberScreenModel { MainScreenModel() }
        val state by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val filteredItems = state.items.filter { item ->
            val desc = if (state.selectedLanguage == Language.RU) item.descriptionRu else item.descriptionKk
            val details = if (state.selectedLanguage == Language.RU) item.detailsRu else item.detailsKk
            val matchesSearch = item.title.contains(state.searchQuery, ignoreCase = true) ||
                                desc.contains(state.searchQuery, ignoreCase = true) ||
                                details.contains(state.searchQuery, ignoreCase = true)

            val matchesTab = when (state.selectedTab) {
                0 -> true
                1 -> item.category == "Regulations" && !item.isRegistrationProcedure
                2 -> item.category == "KZ National"
                3 -> item.category == "Safety & Disposal"
                4 -> item.category == "Systems" && !item.title.startsWith("gis_")
                5 -> item.isRegistrationProcedure || item.title.startsWith("gis_")
                else -> true
            }
            matchesSearch && matchesTab
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00B0FF), // Professional Steel Blue
                onPrimary = Color(0xFF003750),
                primaryContainer = Color(0xFF004B70),
                onPrimaryContainer = Color(0xFFE1F5FE),
                secondary = Color(0xFF80D8FF),
                background = Color(0xFF0F1216), // Dark charcoal slate
                surface = Color(0xFF181C24), // Sleek metal panels
                onBackground = Color(0xFFECEFF1),
                onSurface = Color(0xFFECEFF1)
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        Localization.getString("app_title", state.selectedLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "${Localization.getString("running_on", state.selectedLanguage)}${platform.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        actions = {
                            Button(
                                onClick = { screenModel.toggleLanguage() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Lang",
                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                )
                                Text(
                                    Localization.getString("language_toggle", state.selectedLanguage),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        val allLabel = Localization.getString("all", state.selectedLanguage)
                        val eaeuLabel = Localization.getString("category_regulations", state.selectedLanguage)
                        val kzLabel = Localization.getString("category_kz", state.selectedLanguage)
                        val safetyLabel = Localization.getString("category_safety", state.selectedLanguage)
                        val systemsLabel = Localization.getString("category_systems", state.selectedLanguage)
                        val digitalLabel = Localization.getString("category_digital", state.selectedLanguage)

                        NavigationBarItem(
                            selected = state.selectedTab == 0,
                            onClick = { screenModel.onTabSelected(0) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "All") },
                            label = { Text(allLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        NavigationBarItem(
                            selected = state.selectedTab == 1,
                            onClick = { screenModel.onTabSelected(1) },
                            icon = { Icon(Icons.Default.List, contentDescription = "EAEU") },
                            label = { Text(eaeuLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        NavigationBarItem(
                            selected = state.selectedTab == 2,
                            onClick = { screenModel.onTabSelected(2) },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "KZ") },
                            label = { Text(kzLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        NavigationBarItem(
                            selected = state.selectedTab == 3,
                            onClick = { screenModel.onTabSelected(3) },
                            icon = { Icon(Icons.Default.Warning, contentDescription = "Safety") },
                            label = { Text(safetyLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        NavigationBarItem(
                            selected = state.selectedTab == 4,
                            onClick = { screenModel.onTabSelected(4) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "Systems") },
                            label = { Text(systemsLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        NavigationBarItem(
                            selected = state.selectedTab == 5,
                            onClick = { screenModel.onTabSelected(5) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Digital") },
                            label = { Text(digitalLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            ) { paddingValues ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val isWideScreen = maxWidth > 800.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left List Pane
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { screenModel.onSearchQueryChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(Localization.getString("search_hint", state.selectedLanguage)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sync Status Bar (Professional Data Indicator)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Sync",
                                            tint = if (state.syncSource.contains("Online")) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "${Localization.getString("sync_source", state.selectedLanguage)}${state.syncSource}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Text(
                                        Localization.getString("sync_now", state.selectedLanguage),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { screenModel.loadRegulations() }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (filteredItems.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        Localization.getString("no_items", state.selectedLanguage),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredItems) { item ->
                                        val desc = if (state.selectedLanguage == Language.RU) item.descriptionRu else item.descriptionKk
                                        val details = if (state.selectedLanguage == Language.RU) item.detailsRu else item.detailsKk

                                        val isSelected = isWideScreen && state.selectedItem == item

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    if (isWideScreen) {
                                                        screenModel.onItemSelected(item)
                                                    } else {
                                                        navigator.push(DetailScreen(item, state.selectedLanguage))
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected)
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                                else
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        desc,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface
                                                    )
                                                    val categoryLabel = when (item.category) {
                                                        "Regulations" -> Localization.getString("category_regulations", state.selectedLanguage)
                                                        "KZ National" -> Localization.getString("category_kz", state.selectedLanguage)
                                                        "Safety & Disposal" -> Localization.getString("category_safety", state.selectedLanguage)
                                                        "Systems" -> Localization.getString("category_systems", state.selectedLanguage)
                                                        else -> item.category
                                                    }
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text(categoryLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    details,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Right Detail Pane (Widescreen Split Panel)
                        if (isWideScreen) {
                            AnimatedVisibility(
                                visible = state.selectedItem != null,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .fillMaxHeight()
                            ) {
                                state.selectedItem?.let { item ->
                                    val desc = if (state.selectedLanguage == Language.RU) item.descriptionRu else item.descriptionKk
                                    val details = if (state.selectedLanguage == Language.RU) item.detailsRu else item.detailsKk
                                    val checklist = if (state.selectedLanguage == Language.RU) item.checklistRu else item.checklistKk

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(24.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    desc,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(onClick = { screenModel.onItemSelected(null) }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                                }
                                            }

                                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                            Text(
                                                "${Localization.getString("category", state.selectedLanguage)}${item.category}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "${Localization.getString("key_symbol", state.selectedLanguage)}${item.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                details,
                                                style = MaterialTheme.typography.bodyLarge,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            if (item.title.startsWith("gis_")) {
                                                GisSystemSimulator(item.title, state.selectedLanguage)
                                            }

                                            Spacer(modifier = Modifier.height(28.dp))

                                            if (item.isRegistrationProcedure) {
                                                RegistrationCalculatorAndSimulator(item, state.selectedLanguage)
                                            } else {
                                                // Compliance self-assessment section
                                                Text(
                                                    Localization.getString("compliance_assessment", state.selectedLanguage),
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                if (checklist.isEmpty()) {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.background,
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            Localization.getString("no_checklist", state.selectedLanguage),
                                                            modifier = Modifier.padding(16.dp),
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                } else {
                                                    // Dynamic Compliance Badge
                                                    val checkedCount = checklist.count { state.checkedItems.contains(it) }
                                                    val (statusTextKey, badgeColor) = when (checkedCount) {
                                                        checklist.size -> "compliant" to Color(0xFF2E7D32) // Soft Green
                                                        0 -> "non_compliant" to Color(0xFFC62828) // Soft Red
                                                        else -> "partially_compliant" to Color(0xFFEF6C00) // Soft Orange
                                                    }

                                                    Surface(
                                                        color = badgeColor.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(badgeColor)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                "${Localization.getString("compliance_status", state.selectedLanguage)} " +
                                                                        Localization.getString(statusTextKey, state.selectedLanguage) +
                                                                        " ($checkedCount/${checklist.size})",
                                                                color = badgeColor,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    // List checkable boxes
                                                    checklist.forEach { task ->
                                                        val isChecked = state.checkedItems.contains(task)
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { screenModel.toggleChecklistItem(task) }
                                                                .padding(vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Checkbox(
                                                                checked = isChecked,
                                                                onCheckedChange = { screenModel.toggleChecklistItem(task) },
                                                                colors = CheckboxDefaults.colors(
                                                                    checkedColor = MaterialTheme.colorScheme.primary,
                                                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                                )
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                task,
                                                                fontSize = 13.sp,
                                                                color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                lineHeight = 18.sp
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
                    }
                }
            }
        }
    }
}

class DetailScreen(
    private val item: RegulationItem,
    private val lang: Language
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val desc = if (lang == Language.RU) item.descriptionRu else item.descriptionKk
        val details = if (lang == Language.RU) item.detailsRu else item.detailsKk
        val checklist = if (lang == Language.RU) item.checklistRu else item.checklistKk

        var checkedItems by remember { mutableStateOf(setOf<String>()) }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00B0FF),
                onPrimary = Color(0xFF003750),
                primaryContainer = Color(0xFF004B70),
                onPrimaryContainer = Color(0xFFE1F5FE),
                secondary = Color(0xFF80D8FF),
                background = Color(0xFF0F1216),
                surface = Color(0xFF181C24),
                onBackground = Color(0xFFECEFF1),
                onSurface = Color(0xFFECEFF1)
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(desc, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        "${Localization.getString("category", lang)}${item.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${Localization.getString("key_symbol", lang)}${item.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        details,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.title.startsWith("gis_")) {
                        GisSystemSimulator(item.title, lang)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    if (item.isRegistrationProcedure) {
                        RegistrationCalculatorAndSimulator(item, lang)
                    } else {
                        // Compliance self-assessment section
                        Text(
                            Localization.getString("compliance_assessment", lang),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (checklist.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    Localization.getString("no_checklist", lang),
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            val checkedCount = checklist.count { checkedItems.contains(it) }
                            val (statusTextKey, badgeColor) = when (checkedCount) {
                                checklist.size -> "compliant" to Color(0xFF2E7D32)
                                0 -> "non_compliant" to Color(0xFFC62828)
                                else -> "partially_compliant" to Color(0xFFEF6C00)
                            }

                            Surface(
                                color = badgeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${Localization.getString("compliance_status", lang)} " +
                                                Localization.getString(statusTextKey, lang) +
                                                " ($checkedCount/${checklist.size})",
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            checklist.forEach { task ->
                                val isChecked = checkedItems.contains(task)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            checkedItems = if (isChecked) checkedItems - task else checkedItems + task
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            checkedItems = if (isChecked) checkedItems - task else checkedItems + task
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        task,
                                        fontSize = 13.sp,
                                        color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        lineHeight = 18.sp
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
fun RegistrationCalculatorAndSimulator(
    item: RegulationItem,
    lang: Language
) {
    var isSubstanceListed by remember(item.title) { mutableStateOf(false) }
    var simStage by remember(item.title) { mutableStateOf(0) }

    val calculatedDays = when (item.title) {
        "eaeu_standard_reg" -> if (isSubstanceListed) 95 else 100
        "eaeu_compliance" -> if (isSubstanceListed) 70 else 90
        "eaeu_simplified_reg" -> if (isSubstanceListed) 35 else 45
        "eaeu_recognition" -> 45
        else -> item.maxTimelineDays
    }

    val docs = if (lang == Language.RU) item.documentChecklistRu else item.documentChecklistKk
    val trials = if (lang == Language.RU) item.trialChecklistRu else item.trialChecklistKk

    Column(modifier = Modifier.fillMaxWidth()) {
        // Timeline Calculator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    Localization.getString("timeline_calc", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (item.title != "eaeu_recognition") {
                    // Substance Status Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            Localization.getString("active_substance_status", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !isSubstanceListed,
                                onClick = { isSubstanceListed = false }
                            )
                            Text(Localization.getString("substance_new", lang), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = isSubstanceListed,
                                onClick = { isSubstanceListed = true }
                            )
                            Text(Localization.getString("substance_listed", lang), fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Result timeline box
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Localization.getString("calculated_timeline", lang),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$calculatedDays ${Localization.getString("working_days", lang)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Documents & Trials checklists
        if (docs.isNotEmpty()) {
            Text(
                Localization.getString("required_docs", lang),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            docs.forEach { doc ->
                var checked by remember(doc) { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { checked = !checked }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        doc,
                        fontSize = 13.sp,
                        color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (item.title != "eaeu_recognition") {
            Text(
                Localization.getString("required_trials", lang),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (trials.isEmpty()) {
                Text(
                    Localization.getString("no_trials_needed", lang),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                trials.forEach { trial ->
                    var checked by remember(trial) { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { checked = !checked }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            trial,
                            fontSize = 13.sp,
                            color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Digital Workflow Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    Localization.getString("sim_workflow", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val stateTextKey = when (simStage) {
                    0 -> "sim_state_idle"
                    1 -> "sim_state_submitted"
                    2 -> "sim_state_dossier"
                    3 -> "sim_state_samples"
                    4 -> "sim_state_testing"
                    5 -> "sim_state_approved"
                    else -> "sim_state_idle"
                }

                // Status Description box
                Surface(
                    color = if (simStage == 5) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (simStage == 5) Color(0xFF2E7D32).copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (simStage == 5) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Info",
                            tint = if (simStage == 5) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Localization.getString(stateTextKey, lang),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 1..5) {
                        val active = step <= simStage
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                step.toString(),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (step < 5) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(
                                        if (step < simStage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (simStage == 5) {
                        Button(
                            onClick = { simStage = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(Localization.getString("sim_restart", lang), fontSize = 12.sp)
                        }
                    } else {
                        val btnTextKey = when (simStage) {
                            0 -> "sim_submit"
                            1 -> "sim_upload"
                            2 -> "sim_samples"
                            3 -> "sim_test"
                            4 -> "sim_approve"
                            else -> "sim_submit"
                        }
                        Button(
                            onClick = { simStage += 1 }
                        ) {
                            Text(Localization.getString(btnTextKey, lang), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}


