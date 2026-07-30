package com.dari.dermek.server.service

import com.dari.dermek.server.models.RegistrationPathway
import com.dari.dermek.server.models.RegistrationWorkflowDto
import com.dari.dermek.server.models.WorkflowStage
import com.dari.dermek.server.models.WorkflowState
import com.dari.dermek.server.routes.computeMaxWorkingDays
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class WorkflowEngineTest {

    @Test
    fun `transition graph allows expected path`() {
        assertTrue(WorkflowEngine.isValidTransition(WorkflowStage.SUBMITTED, WorkflowStage.COMPLETENESS_CHECK))
        assertTrue(WorkflowEngine.isValidTransition(WorkflowStage.COMPLETENESS_CHECK, WorkflowStage.SAMPLES))
        assertTrue(WorkflowEngine.isValidTransition(WorkflowStage.COMMITTEE_REVIEW, WorkflowStage.DECISION))
    }

    @Test
    fun `transition graph rejects invalid jumps`() {
        assertFalse(WorkflowEngine.isValidTransition(WorkflowStage.SUBMITTED, WorkflowStage.LAB_TESTING))
        assertFalse(WorkflowEngine.isValidTransition(WorkflowStage.DECISION, WorkflowStage.QUERY))
    }

    @Test
    fun `annex pathway computes tighter sla`() {
        val standard = computeMaxWorkingDays(RegistrationPathway.STANDARD, isAnnex8 = false)
        val annex = computeMaxWorkingDays(RegistrationPathway.STANDARD, isAnnex8 = true)
        assertTrue(annex < standard)
        assertEquals(100, standard)
        assertEquals(95, annex)
    }

    @Test
    fun `overdue check respects completion state`() {
        val overdueActive = RegistrationWorkflowDto(
            id = 1,
            pathway = RegistrationPathway.STANDARD,
            currentStage = WorkflowStage.LAB_TESTING,
            state = WorkflowState.ACTIVE,
            slaWorkingDays = 100,
            elapsedWorkingDays = 90,
            dueDate = "2000-01-01"
        )
        val overdueCompleted = overdueActive.copy(state = WorkflowState.COMPLETED)
        assertTrue(WorkflowEngine.isOverdue(overdueActive))
        assertFalse(WorkflowEngine.isOverdue(overdueCompleted))
    }
}
