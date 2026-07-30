package com.dari.dermek.server.routes

import com.dari.dermek.server.db.RegistrationWorkflows
import com.dari.dermek.server.db.WorkflowStageHistory
import com.dari.dermek.server.models.*
import com.dari.dermek.server.service.WorkflowEngine
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.workflowRoutes() {
    route("/api/v2/workflows/registrations") {
        post {
            val payload = call.receive<CreateWorkflowRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val sla = computeMaxWorkingDays(payload.pathway, payload.isAnnex8)
            val due = now.date.plus(DatePeriod(days = sla))

            val workflow = transaction {
                val id = RegistrationWorkflows.insertAndGetId {
                    it[applicationId] = payload.applicationId
                    it[pathway] = payload.pathway.name
                    it[currentStage] = WorkflowStage.SUBMITTED.name
                    it[state] = WorkflowState.ACTIVE.name
                    it[slaWorkingDays] = sla
                    it[elapsedWorkingDays] = 0
                    it[dueDate] = due.toString()
                    it[createdBy] = payload.createdBy
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                WorkflowStageHistory.insert {
                    it[workflowId] = id.value
                    it[fromStage] = null
                    it[toStage] = WorkflowStage.SUBMITTED.name
                    it[changedBy] = payload.createdBy
                    it[note] = "Workflow created"
                    it[changedAt] = now
                }
                RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id.value }.first().toWorkflowDto()
            }

            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = workflow))
        }

        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<WorkflowTimelineDto>(false, error = "Invalid workflow id"))

            val data = transaction {
                val row = RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.firstOrNull()
                    ?: return@transaction null
                val wf = row.toWorkflowDto()
                val history = WorkflowStageHistory.selectAll()
                    .where { WorkflowStageHistory.workflowId eq id }
                    .orderBy(WorkflowStageHistory.changedAt, SortOrder.ASC)
                    .map { it.toStageEvent() }
                WorkflowTimelineDto(
                    workflow = wf,
                    history = history,
                    isOverdue = WorkflowEngine.isOverdue(wf)
                )
            }

            if (data == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<WorkflowTimelineDto>(false, error = "Workflow not found"))
            } else {
                call.respond(ApiResponse(success = true, data = data))
            }
        }

        post("{id}/advance") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<RegistrationWorkflowDto>(false, error = "Invalid workflow id"))
            val payload = call.receive<StageTransitionRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val updated = transaction {
                val row = RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.firstOrNull()
                    ?: return@transaction null
                val current = WorkflowStage.valueOf(row[RegistrationWorkflows.currentStage])
                val currentState = WorkflowState.valueOf(row[RegistrationWorkflows.state])
                if (currentState != WorkflowState.ACTIVE) {
                    throw IllegalStateException("Workflow state must be ACTIVE to advance")
                }
                if (!WorkflowEngine.isValidTransition(current, payload.toStage)) {
                    throw IllegalArgumentException("Invalid transition from $current to ${payload.toStage}")
                }

                val elapsed = row[RegistrationWorkflows.elapsedWorkingDays] + WorkflowEngine.stageIncrement(current, payload.toStage)
                val newState = if (payload.toStage == WorkflowStage.DECISION) WorkflowState.COMPLETED else WorkflowState.ACTIVE

                RegistrationWorkflows.update({ RegistrationWorkflows.id eq id }) {
                    it[currentStage] = payload.toStage.name
                    it[state] = newState.name
                    it[elapsedWorkingDays] = elapsed
                    it[updatedAt] = now
                }
                WorkflowStageHistory.insert {
                    it[workflowId] = id
                    it[fromStage] = current.name
                    it[toStage] = payload.toStage.name
                    it[changedBy] = payload.changedBy
                    it[note] = payload.note
                    it[changedAt] = now
                }
                RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.first().toWorkflowDto()
            }

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<RegistrationWorkflowDto>(false, error = "Workflow not found"))
            } else {
                call.respond(ApiResponse(success = true, data = updated))
            }
        }

        post("{id}/pause") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<RegistrationWorkflowDto>(false, error = "Invalid workflow id"))
            val payload = call.receive<PauseWorkflowRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val updated = transaction {
                val row = RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.firstOrNull()
                    ?: return@transaction null
                if (WorkflowState.valueOf(row[RegistrationWorkflows.state]) != WorkflowState.ACTIVE) {
                    throw IllegalStateException("Only ACTIVE workflow can be paused")
                }
                RegistrationWorkflows.update({ RegistrationWorkflows.id eq id }) {
                    it[state] = WorkflowState.PAUSED.name
                    it[pausedAt] = now
                    it[updatedAt] = now
                }
                WorkflowStageHistory.insert {
                    it[workflowId] = id
                    it[fromStage] = row[RegistrationWorkflows.currentStage]
                    it[toStage] = row[RegistrationWorkflows.currentStage]
                    it[changedBy] = payload.changedBy
                    it[note] = payload.note ?: "Workflow paused"
                    it[changedAt] = now
                }
                RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.first().toWorkflowDto()
            }

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<RegistrationWorkflowDto>(false, error = "Workflow not found"))
            } else {
                call.respond(ApiResponse(success = true, data = updated))
            }
        }

        post("{id}/resume") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<RegistrationWorkflowDto>(false, error = "Invalid workflow id"))
            val payload = call.receive<ResumeWorkflowRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val updated = transaction {
                val row = RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.firstOrNull()
                    ?: return@transaction null
                if (WorkflowState.valueOf(row[RegistrationWorkflows.state]) != WorkflowState.PAUSED) {
                    throw IllegalStateException("Only PAUSED workflow can be resumed")
                }
                RegistrationWorkflows.update({ RegistrationWorkflows.id eq id }) {
                    it[state] = WorkflowState.ACTIVE.name
                    it[pausedAt] = null
                    it[updatedAt] = now
                }
                WorkflowStageHistory.insert {
                    it[workflowId] = id
                    it[fromStage] = row[RegistrationWorkflows.currentStage]
                    it[toStage] = row[RegistrationWorkflows.currentStage]
                    it[changedBy] = payload.changedBy
                    it[note] = payload.note ?: "Workflow resumed"
                    it[changedAt] = now
                }
                RegistrationWorkflows.selectAll().where { RegistrationWorkflows.id eq id }.first().toWorkflowDto()
            }

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<RegistrationWorkflowDto>(false, error = "Workflow not found"))
            } else {
                call.respond(ApiResponse(success = true, data = updated))
            }
        }
    }
}

private fun ResultRow.toWorkflowDto(): RegistrationWorkflowDto = RegistrationWorkflowDto(
    id = this[RegistrationWorkflows.id].value,
    applicationId = this[RegistrationWorkflows.applicationId],
    pathway = RegistrationPathway.valueOf(this[RegistrationWorkflows.pathway]),
    currentStage = WorkflowStage.valueOf(this[RegistrationWorkflows.currentStage]),
    state = WorkflowState.valueOf(this[RegistrationWorkflows.state]),
    slaWorkingDays = this[RegistrationWorkflows.slaWorkingDays],
    elapsedWorkingDays = this[RegistrationWorkflows.elapsedWorkingDays],
    dueDate = this[RegistrationWorkflows.dueDate]
)

private fun ResultRow.toStageEvent(): WorkflowStageEventDto = WorkflowStageEventDto(
    fromStage = this[WorkflowStageHistory.fromStage]?.let { WorkflowStage.valueOf(it) },
    toStage = WorkflowStage.valueOf(this[WorkflowStageHistory.toStage]),
    changedBy = this[WorkflowStageHistory.changedBy],
    note = this[WorkflowStageHistory.note],
    changedAt = this[WorkflowStageHistory.changedAt].toString()
)
