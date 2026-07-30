package com.dari.dermek.server.models

import kotlinx.serialization.Serializable

@Serializable
data class SecurityRoleDto(
    val id: Long = 0,
    val key: String,
    val title: String,
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
    val systemKey: String,
    val endpoint: String,
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
    val systemKey: String,
    val idempotencyKey: String,
    val status: String,
    val attempts: Int,
    val responsePayload: String? = null,
    val errorMessage: String? = null
)

@Serializable
data class OutboxEventDto(
    val id: Long = 0,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val status: String,
    val retries: Int
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
    val sourceSystem: String,
    val messageKey: String,
    val eventType: String,
    val payload: String,
    val processed: Boolean
)

@Serializable
data class DeadLetterDto(
    val id: Long = 0,
    val source: String,
    val sourceId: String,
    val reason: String,
    val payload: String? = null
)

@Serializable
data class DossierDocumentVersionDto(
    val id: Long = 0,
    val applicationId: Long,
    val partNumber: Int,
    val fileName: String,
    val fileHash: String,
    val storagePath: String,
    val signatureStatus: String
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
    val eventType: String,
    val batchId: Long? = null,
    val qrCode: String? = null,
    val location: String? = null,
    val severity: String = "INFO",
    val payload: String? = null,
    val occurredAt: String
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
    val key: String,
    val title: String,
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
    val templateId: Long,
    val status: String,
    val outputRef: String? = null,
    val requestedBy: Long? = null
)

@Serializable
data class ReportSubmissionDto(
    val id: Long = 0,
    val runId: Long,
    val authority: String,
    val status: String,
    val submissionRef: String? = null,
    val submittedAt: String? = null
)

@Serializable
data class SubmitReportRequest(
    val authority: String,
    val submissionRef: String? = null
)
