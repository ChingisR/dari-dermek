package com.dari.dermek.server.models

import kotlinx.serialization.Serializable

@Serializable
enum class ParticipantType {
    MANUFACTURER,
    DISTRIBUTOR,
    CLINIC,
    PHARMACY,
    FARM,
    GOVERNMENT
}

@Serializable
data class ParticipantDto(
    val id: Long = 0,
    val type: ParticipantType,
    val name: String,
    val binIin: String? = null,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val licenseNumber: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class ModuleSpecDto(
    val key: String,
    val title: String,
    val description: String
)

@Serializable
data class SubsystemSpecDto(
    val key: String,
    val title: String,
    val purpose: String,
    val modules: List<ModuleSpecDto>
)

@Serializable
data class ExternalIntegrationDto(
    val system: String,
    val purpose: String
)

@Serializable
data class SecurityControlDto(
    val name: String,
    val description: String
)

@Serializable
data class RolePermissionDto(
    val role: UserRole,
    val permissions: List<String>
)

@Serializable
data class SystemBlueprintDto(
    val title: String,
    val source: String,
    val subsystems: List<SubsystemSpecDto>,
    val integrations: List<ExternalIntegrationDto>,
    val securityControls: List<SecurityControlDto>
)

@Serializable
data class OperationalSummaryDto(
    val participantsByType: Map<String, Long>,
    val activeApplications: Long,
    val activeDrugs: Long,
    val activeBatches: Long,
    val adverseEvents: Long
)
