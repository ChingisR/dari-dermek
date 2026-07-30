package com.dari.dermek.server.service

import com.dari.dermek.server.models.WorkflowStage
import com.dari.dermek.server.models.WorkflowState
import com.dari.dermek.server.models.RegistrationWorkflowDto
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object WorkflowEngine {
    fun isValidTransition(from: WorkflowStage, to: WorkflowStage): Boolean {
        val graph = mapOf(
            WorkflowStage.SUBMITTED to setOf(WorkflowStage.COMPLETENESS_CHECK),
            WorkflowStage.COMPLETENESS_CHECK to setOf(WorkflowStage.QUERY, WorkflowStage.SAMPLES, WorkflowStage.LAB_TESTING, WorkflowStage.EXPERT_CONCLUSION),
            WorkflowStage.QUERY to setOf(WorkflowStage.COMPLETENESS_CHECK, WorkflowStage.SAMPLES),
            WorkflowStage.SAMPLES to setOf(WorkflowStage.LAB_TESTING),
            WorkflowStage.LAB_TESTING to setOf(WorkflowStage.EXPERT_CONCLUSION),
            WorkflowStage.EXPERT_CONCLUSION to setOf(WorkflowStage.COMMITTEE_REVIEW),
            WorkflowStage.COMMITTEE_REVIEW to setOf(WorkflowStage.DECISION),
            WorkflowStage.DECISION to emptySet()
        )
        return graph[from]?.contains(to) == true
    }

    fun stageIncrement(from: WorkflowStage, to: WorkflowStage): Int {
        return when {
            from == WorkflowStage.SUBMITTED && to == WorkflowStage.COMPLETENESS_CHECK -> 10
            to == WorkflowStage.QUERY -> 5
            to == WorkflowStage.SAMPLES -> 15
            to == WorkflowStage.LAB_TESTING -> 45
            to == WorkflowStage.EXPERT_CONCLUSION -> 20
            to == WorkflowStage.COMMITTEE_REVIEW -> 15
            to == WorkflowStage.DECISION -> 10
            else -> 5
        }
    }

    fun isOverdue(workflow: RegistrationWorkflowDto): Boolean {
        val due = workflow.dueDate ?: return false
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return workflow.state != WorkflowState.COMPLETED && due < today
    }
}
