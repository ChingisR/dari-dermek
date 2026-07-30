package com.dari.dermek.api

import kotlinx.serialization.Serializable

// ─── Shared API Models (mirror of server DTOs for client use) ───

@Serializable
enum class UserRole {
    APPLICANT, COMMITTEE_STAFF, NRCV_EXPERT, LAB_ANALYST,
    BORDER_INSPECTOR, WAREHOUSE_CLERK, FARMER_VET, ADMIN
}

@Serializable
enum class RegistrationPathway {
    COMPLIANCE, STANDARD, SIMPLIFIED, CONFIRMATION,
    AMENDMENT_WITH_TESTING, AMENDMENT_WITHOUT_TESTING, RECOGNITION
}

@Serializable
enum class ApplicationStatus {
    DRAFT, SUBMITTED, COMPLETENESS_CHECK, COMPLETENESS_FAILED,
    QUERY_SENT, QUERY_RESPONDED, SAMPLES_REQUESTED, SAMPLES_RECEIVED,
    LAB_TESTING, LAB_COMPLETE, EXPERT_CONCLUSION, COMMITTEE_REVIEW,
    APPROVED, REJECTED, SUSPENDED, CANCELLED
}

@Serializable
enum class DrugType {
    PHARMACEUTICAL, IMMUNOLOGICAL, DIAGNOSTIC, DISINFECTANT, FEED_ADDITIVE
}

@Serializable
enum class BatchStatus {
    ACTIVE, SUSPENDED, RECALLED, DESTROYED, EXPIRED
}

@Serializable
enum class WorkflowState {
    ACTIVE, PAUSED, COMPLETED, CANCELLED
}

@Serializable
enum class WorkflowStage {
    SUBMITTED, COMPLETENESS_CHECK, QUERY, SAMPLES, LAB_TESTING, EXPERT_CONCLUSION, COMMITTEE_REVIEW, DECISION
}

@Serializable
enum class ParticipantType {
    MANUFACTURER, DISTRIBUTOR, CLINIC, PHARMACY, FARM, GOVERNMENT
}

@Serializable
data class UserDto(
    val id: Long = 0,
    val login: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.APPLICANT,
    val organization: String? = null
)

@Serializable
data class DrugDto(
    val id: Long = 0,
    val tradeName: String = "",
    val inn: String? = null,
    val type: DrugType = DrugType.PHARMACEUTICAL,
    val dosageForm: String? = null,
    val activeSubstances: List<String> = emptyList(),
    val manufacturerName: String? = null,
    val registrationNumber: String? = null,
    val registrationDate: String? = null,
    val expiryDate: String? = null,
    val isAnnex8: Boolean = false,
    val targetAnimals: List<String> = emptyList(),
    val status: String = "ACTIVE"
)

@Serializable
data class ApplicationDto(
    val id: Long = 0,
    val applicantId: Long = 0,
    val applicantName: String? = null,
    val pathway: RegistrationPathway = RegistrationPathway.STANDARD,
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val drugTradeName: String = "",
    val drugType: DrugType = DrugType.PHARMACEUTICAL,
    val manufacturerName: String? = null,
    val submissionDate: String? = null,
    val deadlineDate: String? = null,
    val maxWorkingDays: Int? = null,
    val workingDaysElapsed: Int = 0,
    val isClockPaused: Boolean = false,
    val queryCount: Int = 0,
    val notes: String? = null
)

@Serializable
data class BatchDto(
    val id: Long = 0,
    val drugId: Long = 0,
    val drugName: String? = null,
    val batchNumber: String = "",
    val status: BatchStatus = BatchStatus.ACTIVE,
    val coldChainOk: Boolean = true,
    val qrCode: String? = null
)

@Serializable
data class QrScanResultDto(
    val found: Boolean = false,
    val drug: DrugDto? = null,
    val batch: BatchDto? = null,
    val registrationValid: Boolean = false,
    val coldChainIntact: Boolean = true,
    val alerts: List<String> = emptyList()
)

@Serializable
data class PathwayInfo(
    val key: String = "",
    val nameRu: String = "",
    val nameEn: String = "",
    val maxDaysStandard: Int = 0,
    val maxDaysAnnex: Int = 0,
    val sampleRequired: Boolean = false,
    val deadline: String? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val totalCount: Long? = null
)

@Serializable
data class AdverseEventDto(
    val id: Long = 0,
    val reporterName: String = "",
    val reporterOrg: String? = null,
    val phone: String? = null,
    val drugName: String = "",
    val batchNumber: String? = null,
    val dosageForm: String? = null,
    val description: String = "",
    val detectionDate: String = "",
    val measuresTaken: String? = null,
    val status: String = "REGISTERED"
)

@Serializable
data class DestructionActDto(
    val id: Long = 0,
    val drugName: String = "",
    val batchNumber: String = "",
    val volume: Double = 0.0,
    val grounds: String = "EXPIRED",
    val denaturationMethod: String = "",
    val destructionMethod: String = "",
    val destructionDate: String = "",
    val isPrivateSector: Boolean = true,
    val commissionMembers: List<String> = emptyList(),
    val status: String = "COMPLETED"
)

@Serializable
data class ParticipantDto(
    val id: Long = 0,
    val type: ParticipantType = ParticipantType.MANUFACTURER,
    val name: String = "",
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
    val key: String = "",
    val title: String = "",
    val description: String = ""
)

@Serializable
data class SubsystemSpecDto(
    val key: String = "",
    val title: String = "",
    val purpose: String = "",
    val modules: List<ModuleSpecDto> = emptyList()
)

@Serializable
data class ExternalIntegrationDto(
    val system: String = "",
    val purpose: String = ""
)

@Serializable
data class SecurityControlDto(
    val name: String = "",
    val description: String = ""
)

@Serializable
data class RolePermissionDto(
    val role: UserRole = UserRole.APPLICANT,
    val permissions: List<String> = emptyList()
)

@Serializable
data class SystemBlueprintDto(
    val title: String = "",
    val source: String = "",
    val subsystems: List<SubsystemSpecDto> = emptyList(),
    val integrations: List<ExternalIntegrationDto> = emptyList(),
    val securityControls: List<SecurityControlDto> = emptyList()
)

@Serializable
data class OperationalSummaryDto(
    val participantsByType: Map<String, Long> = emptyMap(),
    val activeApplications: Long = 0,
    val activeDrugs: Long = 0,
    val activeBatches: Long = 0,
    val adverseEvents: Long = 0
)

@Serializable
data class RegistrationWorkflowDto(
    val id: Long = 0,
    val applicationId: Long? = null,
    val pathway: RegistrationPathway = RegistrationPathway.STANDARD,
    val currentStage: WorkflowStage = WorkflowStage.SUBMITTED,
    val state: WorkflowState = WorkflowState.ACTIVE,
    val slaWorkingDays: Int = 0,
    val elapsedWorkingDays: Int = 0,
    val dueDate: String? = null
)

@Serializable
data class CreateWorkflowRequest(
    val applicationId: Long? = null,
    val pathway: RegistrationPathway = RegistrationPathway.STANDARD,
    val isAnnex8: Boolean = false,
    val createdBy: Long? = null
)

@Serializable
data class StageTransitionRequest(
    val toStage: WorkflowStage,
    val changedBy: Long? = null,
    val note: String? = null
)

@Serializable
data class PauseWorkflowRequest(
    val changedBy: Long? = null,
    val note: String? = null
)

@Serializable
data class ResumeWorkflowRequest(
    val changedBy: Long? = null,
    val note: String? = null
)

@Serializable
data class WorkflowStageEventDto(
    val fromStage: WorkflowStage? = null,
    val toStage: WorkflowStage = WorkflowStage.SUBMITTED,
    val changedBy: Long? = null,
    val note: String? = null,
    val changedAt: String = ""
)

@Serializable
data class WorkflowTimelineDto(
    val workflow: RegistrationWorkflowDto = RegistrationWorkflowDto(),
    val history: List<WorkflowStageEventDto> = emptyList(),
    val isOverdue: Boolean = false
)

@Serializable
data class SecurityRoleDto(
    val id: Long = 0,
    val key: String = "",
    val title: String = "",
    val description: String? = null,
    val permissions: List<String> = emptyList()
)

@Serializable
data class CreateSecurityRoleRequest(
    val key: String,
    val title: String,
    val description: String? = null,
    val permissions: List<String> = emptyList()
)

@Serializable
data class AssignUserRoleRequest(
    val userId: Long,
    val roleKey: String,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val assignedBy: Long? = null
)

@Serializable
data class IntegrationConnectorDto(
    val id: Long = 0,
    val systemKey: String = "",
    val endpoint: String = "",
    val protocol: String = "REST",
    val isActive: Boolean = true,
    val retryPolicy: String = "exponential-3"
)

@Serializable
data class CreateConnectorRequest(
    val systemKey: String,
    val endpoint: String,
    val protocol: String = "REST",
    val retryPolicy: String = "exponential-3"
)

@Serializable
data class IntegrationCallRequest(
    val systemKey: String,
    val idempotencyKey: String,
    val payload: String? = null
)

@Serializable
data class IntegrationCallResultDto(
    val id: Long = 0,
    val systemKey: String = "",
    val idempotencyKey: String = "",
    val status: String = "",
    val attempts: Int = 0,
    val responsePayload: String? = null,
    val errorMessage: String? = null
)

@Serializable
data class OutboxEventDto(
    val id: Long = 0,
    val aggregateType: String = "",
    val aggregateId: String = "",
    val eventType: String = "",
    val payload: String = "",
    val status: String = "",
    val retries: Int = 0
)

@Serializable
data class EnqueueOutboxRequest(
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String
)

@Serializable
data class InboxEventDto(
    val id: Long = 0,
    val sourceSystem: String = "",
    val messageKey: String = "",
    val eventType: String = "",
    val payload: String = "",
    val processed: Boolean = false
)

@Serializable
data class DeadLetterDto(
    val id: Long = 0,
    val source: String = "",
    val sourceId: String = "",
    val reason: String = "",
    val payload: String? = null
)

@Serializable
data class DossierDocumentVersionDto(
    val id: Long = 0,
    val applicationId: Long = 0,
    val partNumber: Int = 0,
    val fileName: String = "",
    val fileHash: String = "",
    val storagePath: String = "",
    val signatureStatus: String = "PENDING"
)

@Serializable
data class CreateDossierDocumentVersionRequest(
    val applicationId: Long,
    val partNumber: Int,
    val fileName: String,
    val fileHash: String,
    val storagePath: String,
    val signatureStatus: String = "PENDING",
    val uploadedBy: Long? = null
)

@Serializable
data class TraceabilityEventDto(
    val id: Long = 0,
    val eventType: String = "",
    val batchId: Long? = null,
    val qrCode: String? = null,
    val location: String? = null,
    val severity: String = "INFO",
    val payload: String? = null,
    val occurredAt: String = ""
)

@Serializable
data class CreateTraceabilityEventRequest(
    val eventType: String,
    val batchId: Long? = null,
    val qrCode: String? = null,
    val location: String? = null,
    val severity: String = "INFO",
    val payload: String? = null
)

@Serializable
data class ReportTemplateDto(
    val id: Long = 0,
    val key: String = "",
    val title: String = "",
    val description: String? = null,
    val scheduleType: String = "ON_DEMAND",
    val querySpec: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateReportTemplateRequest(
    val key: String,
    val title: String,
    val description: String? = null,
    val scheduleType: String = "ON_DEMAND",
    val querySpec: String? = null
)

@Serializable
data class ReportRunDto(
    val id: Long = 0,
    val templateId: Long = 0,
    val status: String = "",
    val outputRef: String? = null,
    val requestedBy: Long? = null
)

@Serializable
data class ReportSubmissionDto(
    val id: Long = 0,
    val runId: Long = 0,
    val authority: String = "",
    val status: String = "",
    val submissionRef: String? = null,
    val submittedAt: String? = null
)

@Serializable
data class SubmitReportRequest(
    val authority: String,
    val submissionRef: String? = null
)
