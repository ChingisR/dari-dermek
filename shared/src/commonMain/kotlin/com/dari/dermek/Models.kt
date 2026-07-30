package com.dari.dermek

import kotlinx.serialization.Serializable

@Serializable
data class RegulationItem(
    val title: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val descriptionKk: String,
    val category: String,
    val detailsEn: String,
    val detailsRu: String,
    val detailsKk: String,
    val checklistRu: List<String> = emptyList(),
    val checklistKk: List<String> = emptyList(),
    
    // Modernization metadata for Timeline Calculator & Workflow Simulator
    val isRegistrationProcedure: Boolean = false,
    val minTimelineDays: Int = 0,
    val maxTimelineDays: Int = 0,
    val documentChecklistRu: List<String> = emptyList(),
    val documentChecklistKk: List<String> = emptyList(),
    val trialChecklistRu: List<String> = emptyList(),
    val trialChecklistKk: List<String> = emptyList()
)
