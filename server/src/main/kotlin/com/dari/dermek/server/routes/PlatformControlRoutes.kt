package com.dari.dermek.server.routes

import com.dari.dermek.server.db.*
import com.dari.dermek.server.models.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.platformControlRoutes() {
    securityPolicyRoutes()
    integrationRoutes()
    eventReliabilityRoutes()
    dossierRoutes()
    traceabilityRoutes()
    reportingRoutes()
}

private fun Route.securityPolicyRoutes() {
    route("/api/v2/security/policies") {
        get("/roles") {
            val rows = transaction {
                SecurityRoles.selectAll().orderBy(SecurityRoles.key).map { roleRow ->
                    val perms = (SecurityRolePermissions innerJoin SecurityPermissions)
                        .selectAll()
                        .where { SecurityRolePermissions.roleId eq roleRow[SecurityRoles.id].value }
                        .map { it[SecurityPermissions.key] }
                    SecurityRoleDto(
                        id = roleRow[SecurityRoles.id].value,
                        key = roleRow[SecurityRoles.key],
                        title = roleRow[SecurityRoles.title],
                        description = roleRow[SecurityRoles.description],
                        permissions = perms
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }

        post("/roles") {
            val payload = call.receive<CreateSecurityRoleRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val roleId = SecurityRoles.insertAndGetId {
                    it[key] = payload.key
                    it[title] = payload.title
                    it[description] = payload.description
                    it[isSystem] = false
                    it[createdAt] = now
                }

                payload.permissions.forEach { permissionKey ->
                    val permId = SecurityPermissions.selectAll().where { SecurityPermissions.key eq permissionKey }.firstOrNull()
                        ?.get(SecurityPermissions.id)?.value
                        ?: SecurityPermissions.insertAndGetId {
                            it[key] = permissionKey
                            it[title] = permissionKey
                            it[description] = null
                            it[createdAt] = now
                        }.value

                    SecurityRolePermissions.insertIgnore {
                        it[SecurityRolePermissions.roleId] = roleId.value
                        it[SecurityRolePermissions.permissionId] = permId
                    }
                }

                SecurityRoleDto(
                    id = roleId.value,
                    key = payload.key,
                    title = payload.title,
                    description = payload.description,
                    permissions = payload.permissions
                )
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        post("/assignments") {
            val payload = call.receive<AssignUserRoleRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val roleId = transaction {
                SecurityRoles.selectAll().where { SecurityRoles.key eq payload.roleKey }.firstOrNull()?.get(SecurityRoles.id)?.value
            } ?: return@post call.respond(HttpStatusCode.NotFound, ApiResponse<String>(false, error = "Role not found"))

            transaction {
                UserRoleAssignments.insert {
                    it[userId] = payload.userId
                    it[UserRoleAssignments.roleId] = roleId
                    it[startsAt] = payload.startsAt?.let { _ -> now }
                    it[endsAt] = payload.endsAt?.let { _ -> now }
                    it[assignedBy] = payload.assignedBy
                    it[createdAt] = now
                }
            }
            call.respond(ApiResponse(success = true, data = "ASSIGNED"))
        }

        get("/users/{userId}/roles") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<List<String>>(false, error = "Invalid user id"))
            val roles = transaction {
                (UserRoleAssignments innerJoin SecurityRoles)
                    .selectAll()
                    .where { UserRoleAssignments.userId eq userId }
                    .map { it[SecurityRoles.key] }
            }
            call.respond(ApiResponse(success = true, data = roles, totalCount = roles.size.toLong()))
        }
    }
}

private fun Route.integrationRoutes() {
    route("/api/v2/integrations") {
        get("/connectors") {
            val rows = transaction {
                IntegrationConnectors.selectAll().orderBy(IntegrationConnectors.systemKey).map {
                    IntegrationConnectorDto(
                        id = it[IntegrationConnectors.id].value,
                        systemKey = it[IntegrationConnectors.systemKey],
                        endpoint = it[IntegrationConnectors.endpoint],
                        protocol = it[IntegrationConnectors.protocol],
                        isActive = it[IntegrationConnectors.isActive],
                        retryPolicy = it[IntegrationConnectors.retryPolicy]
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }

        post("/connectors") {
            val payload = call.receive<CreateConnectorRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val id = IntegrationConnectors.insertAndGetId {
                    it[systemKey] = payload.systemKey
                    it[endpoint] = payload.endpoint
                    it[protocol] = payload.protocol
                    it[retryPolicy] = payload.retryPolicy
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                IntegrationConnectorDto(
                    id = id.value,
                    systemKey = payload.systemKey,
                    endpoint = payload.endpoint,
                    protocol = payload.protocol,
                    isActive = true,
                    retryPolicy = payload.retryPolicy
                )
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        post("/call") {
            val payload = call.receive<IntegrationCallRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val result = transaction {
                val connector = IntegrationConnectors.selectAll().where { IntegrationConnectors.systemKey eq payload.systemKey }.firstOrNull()
                    ?: return@transaction null

                val connectorId = connector[IntegrationConnectors.id].value
                val existing = IntegrationCalls.selectAll().where {
                    (IntegrationCalls.connectorId eq connectorId) and (IntegrationCalls.idempotencyKey eq payload.idempotencyKey)
                }.firstOrNull()
                if (existing != null) {
                    return@transaction IntegrationCallResultDto(
                        id = existing[IntegrationCalls.id].value,
                        systemKey = payload.systemKey,
                        idempotencyKey = existing[IntegrationCalls.idempotencyKey],
                        status = existing[IntegrationCalls.status],
                        attempts = existing[IntegrationCalls.attempts],
                        responsePayload = existing[IntegrationCalls.responsePayload],
                        errorMessage = existing[IntegrationCalls.errorMessage]
                    )
                }

                val simulatedSuccess = connector[IntegrationConnectors.isActive]
                val id = IntegrationCalls.insertAndGetId {
                    it[IntegrationCalls.connectorId] = connectorId
                    it[idempotencyKey] = payload.idempotencyKey
                    it[requestPayload] = payload.payload
                    it[status] = if (simulatedSuccess) "SUCCESS" else "FAILED"
                    it[attempts] = 1
                    it[responsePayload] = if (simulatedSuccess) """{"ok":true}""" else null
                    it[errorMessage] = if (simulatedSuccess) null else "Connector inactive"
                    it[createdAt] = now
                    it[updatedAt] = now
                }

                if (!simulatedSuccess) {
                    DeadLetters.insert {
                        it[sourceType] = "INTEGRATION"
                        it[DeadLetters.sourceId] = id.value.toString()
                        it[DeadLetters.reason] = "Connector inactive"
                        it[DeadLetters.payload] = payload.payload
                        it[DeadLetters.createdAt] = now
                    }
                }

                IntegrationCallResultDto(
                    id = id.value,
                    systemKey = payload.systemKey,
                    idempotencyKey = payload.idempotencyKey,
                    status = if (simulatedSuccess) "SUCCESS" else "FAILED",
                    attempts = 1,
                    responsePayload = if (simulatedSuccess) """{"ok":true}""" else null,
                    errorMessage = if (simulatedSuccess) null else "Connector inactive"
                )
            }

            if (result == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<IntegrationCallResultDto>(false, error = "Connector not found"))
            } else {
                call.respond(ApiResponse(success = true, data = result))
            }
        }
    }
}

private fun Route.eventReliabilityRoutes() {
    route("/api/v2/ops/events") {
        post("/outbox") {
            val payload = call.receive<EnqueueOutboxRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val id = OutboxEvents.insertAndGetId {
                    it[aggregateType] = payload.aggregateType
                    it[aggregateId] = payload.aggregateId
                    it[eventType] = payload.eventType
                    it[OutboxEvents.payload] = payload.payload
                    it[status] = "PENDING"
                    it[retries] = 0
                    it[nextAttemptAt] = null
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                OutboxEventDto(id.value, payload.aggregateType, payload.aggregateId, payload.eventType, payload.payload, "PENDING", 0)
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        get("/outbox") {
            val status = call.request.queryParameters["status"] ?: "PENDING"
            val rows = transaction {
                OutboxEvents.selectAll().where { OutboxEvents.status eq status }.orderBy(OutboxEvents.createdAt, SortOrder.DESC).limit(200).map {
                    OutboxEventDto(
                        id = it[OutboxEvents.id].value,
                        aggregateType = it[OutboxEvents.aggregateType],
                        aggregateId = it[OutboxEvents.aggregateId],
                        eventType = it[OutboxEvents.eventType],
                        payload = it[OutboxEvents.payload],
                        status = it[OutboxEvents.status],
                        retries = it[OutboxEvents.retries]
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }

        post("/outbox/{id}/retry") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<String>(false, error = "Invalid outbox id"))
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            transaction {
                val currentRetries = OutboxEvents
                    .selectAll()
                    .where { OutboxEvents.id eq id }
                    .firstOrNull()
                    ?.get(OutboxEvents.retries) ?: 0
                OutboxEvents.update({ OutboxEvents.id eq id }) {
                    it[status] = "PENDING"
                    it[retries] = currentRetries + 1
                    it[nextAttemptAt] = now
                    it[updatedAt] = now
                }
            }
            call.respond(ApiResponse(success = true, data = "RETRY_QUEUED"))
        }

        post("/inbox") {
            val payload = call.receive<InboxEventDto>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val existing = InboxEvents.selectAll().where { InboxEvents.messageKey eq payload.messageKey }.firstOrNull()
                if (existing != null) {
                    existing[InboxEvents.id].value
                } else {
                    InboxEvents.insertAndGetId {
                        it[sourceSystem] = payload.sourceSystem
                        it[messageKey] = payload.messageKey
                        it[eventType] = payload.eventType
                        it[InboxEvents.payload] = payload.payload
                        it[processed] = false
                        it[processedAt] = null
                        it[createdAt] = now
                    }.value
                }
            }
            call.respond(ApiResponse(success = true, data = mapOf("id" to created)))
        }

        get("/dead-letters") {
            val rows = transaction {
                DeadLetters.selectAll().orderBy(DeadLetters.createdAt, SortOrder.DESC).limit(200).map {
                    DeadLetterDto(
                        id = it[DeadLetters.id].value,
                        source = it[DeadLetters.sourceType],
                        sourceId = it[DeadLetters.sourceId],
                        reason = it[DeadLetters.reason],
                        payload = it[DeadLetters.payload]
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }
    }
}

private fun Route.dossierRoutes() {
    route("/api/v2/dossier") {
        post("/versions") {
            val payload = call.receive<CreateDossierDocumentVersionRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val id = DossierDocumentVersions.insertAndGetId {
                    it[applicationId] = payload.applicationId
                    it[partNumber] = payload.partNumber
                    it[fileName] = payload.fileName
                    it[fileHash] = payload.fileHash
                    it[storagePath] = payload.storagePath
                    it[signatureStatus] = payload.signatureStatus
                    it[uploadedBy] = payload.uploadedBy
                    it[uploadedAt] = now
                }
                DossierDocumentVersionDto(
                    id = id.value,
                    applicationId = payload.applicationId,
                    partNumber = payload.partNumber,
                    fileName = payload.fileName,
                    fileHash = payload.fileHash,
                    storagePath = payload.storagePath,
                    signatureStatus = payload.signatureStatus
                )
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        get("/applications/{applicationId}/versions") {
            val applicationId = call.parameters["applicationId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<List<DossierDocumentVersionDto>>(false, error = "Invalid application id"))
            val rows = transaction {
                DossierDocumentVersions.selectAll()
                    .where { DossierDocumentVersions.applicationId eq applicationId }
                    .orderBy(DossierDocumentVersions.uploadedAt, SortOrder.DESC).map {
                        DossierDocumentVersionDto(
                            id = it[DossierDocumentVersions.id].value,
                            applicationId = it[DossierDocumentVersions.applicationId],
                            partNumber = it[DossierDocumentVersions.partNumber],
                            fileName = it[DossierDocumentVersions.fileName],
                            fileHash = it[DossierDocumentVersions.fileHash],
                            storagePath = it[DossierDocumentVersions.storagePath],
                            signatureStatus = it[DossierDocumentVersions.signatureStatus]
                        )
                    }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }
    }
}

private fun Route.traceabilityRoutes() {
    route("/api/v2/traceability/events") {
        post {
            val payload = call.receive<CreateTraceabilityEventRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val id = TraceabilityEvents.insertAndGetId {
                    it[eventType] = payload.eventType
                    it[batchId] = payload.batchId
                    it[qrCode] = payload.qrCode
                    it[location] = payload.location
                    it[severity] = payload.severity
                    it[TraceabilityEvents.payload] = payload.payload
                    it[occurredAt] = now
                }
                TraceabilityEventDto(
                    id = id.value,
                    eventType = payload.eventType,
                    batchId = payload.batchId,
                    qrCode = payload.qrCode,
                    location = payload.location,
                    severity = payload.severity,
                    payload = payload.payload,
                    occurredAt = now.toString()
                )
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        get {
            val eventType = call.request.queryParameters["eventType"]
            val rows = transaction {
                val query = if (eventType.isNullOrBlank()) {
                    TraceabilityEvents.selectAll()
                } else {
                    TraceabilityEvents.selectAll().where { TraceabilityEvents.eventType eq eventType }
                }
                query.orderBy(TraceabilityEvents.occurredAt, SortOrder.DESC).limit(200).map {
                    TraceabilityEventDto(
                        id = it[TraceabilityEvents.id].value,
                        eventType = it[TraceabilityEvents.eventType],
                        batchId = it[TraceabilityEvents.batchId],
                        qrCode = it[TraceabilityEvents.qrCode],
                        location = it[TraceabilityEvents.location],
                        severity = it[TraceabilityEvents.severity],
                        payload = it[TraceabilityEvents.payload],
                        occurredAt = it[TraceabilityEvents.occurredAt].toString()
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }
    }
}

private fun Route.reportingRoutes() {
    route("/api/v2/reports") {
        get("/templates") {
            val rows = transaction {
                ReportTemplates.selectAll().where { ReportTemplates.isActive eq true }.orderBy(ReportTemplates.key).map {
                    ReportTemplateDto(
                        id = it[ReportTemplates.id].value,
                        key = it[ReportTemplates.key],
                        title = it[ReportTemplates.title],
                        description = it[ReportTemplates.description],
                        scheduleType = it[ReportTemplates.scheduleType],
                        querySpec = it[ReportTemplates.querySpec],
                        isActive = it[ReportTemplates.isActive]
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }

        post("/templates") {
            val payload = call.receive<CreateReportTemplateRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val created = transaction {
                val id = ReportTemplates.insertAndGetId {
                    it[key] = payload.key
                    it[title] = payload.title
                    it[description] = payload.description
                    it[scheduleType] = payload.scheduleType
                    it[querySpec] = payload.querySpec
                    it[isActive] = true
                    it[createdAt] = now
                }
                ReportTemplateDto(
                    id = id.value,
                    key = payload.key,
                    title = payload.title,
                    description = payload.description,
                    scheduleType = payload.scheduleType,
                    querySpec = payload.querySpec,
                    isActive = true
                )
            }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }

        post("/templates/{templateId}/run") {
            val templateId = call.parameters["templateId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<ReportRunDto>(false, error = "Invalid template id"))
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val run = transaction {
                val template = ReportTemplates.selectAll().where { ReportTemplates.id eq templateId }.firstOrNull()
                    ?: return@transaction null
                val id = ReportRuns.insertAndGetId {
                    it[ReportRuns.templateId] = templateId
                    it[status] = "COMPLETED"
                    it[outputRef] = "report://${template[ReportTemplates.key]}/${now.date}"
                    it[requestedBy] = null
                    it[errorMessage] = null
                    it[startedAt] = now
                    it[finishedAt] = now
                }
                ReportRunDto(
                    id = id.value,
                    templateId = templateId,
                    status = "COMPLETED",
                    outputRef = "report://${template[ReportTemplates.key]}/${now.date}",
                    requestedBy = null
                )
            }
            if (run == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<ReportRunDto>(false, error = "Template not found"))
            } else {
                call.respond(ApiResponse(success = true, data = run))
            }
        }

        post("/runs/{runId}/submit") {
            val runId = call.parameters["runId"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<ReportSubmissionDto>(false, error = "Invalid run id"))
            val payload = call.receive<SubmitReportRequest>()
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val submission = transaction {
                val run = ReportRuns.selectAll().where { ReportRuns.id eq runId }.firstOrNull() ?: return@transaction null
                val id = ReportSubmissions.insertAndGetId {
                    it[ReportSubmissions.runId] = runId
                    it[authority] = payload.authority
                    it[status] = "SUBMITTED"
                    it[submissionRef] = payload.submissionRef
                    it[submittedAt] = now
                }
                ReportSubmissionDto(
                    id = id.value,
                    runId = run[ReportRuns.id].value,
                    authority = payload.authority,
                    status = "SUBMITTED",
                    submissionRef = payload.submissionRef,
                    submittedAt = now.toString()
                )
            }
            if (submission == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<ReportSubmissionDto>(false, error = "Run not found"))
            } else {
                call.respond(ApiResponse(success = true, data = submission))
            }
        }

        get("/submissions") {
            val rows = transaction {
                ReportSubmissions.selectAll().orderBy(ReportSubmissions.id, SortOrder.DESC).limit(200).map {
                    ReportSubmissionDto(
                        id = it[ReportSubmissions.id].value,
                        runId = it[ReportSubmissions.runId],
                        authority = it[ReportSubmissions.authority],
                        status = it[ReportSubmissions.status],
                        submissionRef = it[ReportSubmissions.submissionRef],
                        submittedAt = it[ReportSubmissions.submittedAt]?.toString()
                    )
                }
            }
            call.respond(ApiResponse(success = true, data = rows, totalCount = rows.size.toLong()))
        }
    }
}
