package com.dari.dermek.server.models

import kotlinx.serialization.Serializable

@Serializable
enum class WorkflowState {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class WorkflowStage {
    SUBMITTED,
    COMPLETENESS_CHECK,
    QUERY,
    SAMPLES,
    LAB_TESTING,
    EXPERT_CONCLUSION,
    COMMITTEE_REVIEW,
    DECISION
}

@Serializable
data class RegistrationWorkflowDto(
    val id: Long = 0,
    val applicationId: Long? = null,
    val pathway: RegistrationPathway,
    val currentStage: WorkflowStage,
    val state: WorkflowState = WorkflowState.ACTIVE,
    val slaWorkingDays: Int,
    val elapsedWorkingDays: Int = 0,
    val dueDate: String? = null
)

@Serializable
data class CreateWorkflowRequest(
    val applicationId: Long? = null,
    val pathway: RegistrationPathway,
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
    val toStage: WorkflowStage,
    val changedBy: Long? = null,
    val note: String? = null,
    val changedAt: String
)

@Serializable
data class WorkflowTimelineDto(
    val workflow: RegistrationWorkflowDto,
    val history: List<WorkflowStageEventDto>,
    val isOverdue: Boolean
)
