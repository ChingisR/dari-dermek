package com.dari.dermek.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.russhwolf.settings.Settings

/**
 * HTTP client that connects to the Ktor backend API.
 * Falls back to GisApiClient (mock data) when the backend is unavailable.
 * Persists user sessions using Multiplatform Settings and automatically injects JWT tokens.
 */
object GisHttpClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val settings = Settings()

    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "auth_user"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(this@GisHttpClient.json)
        }
        defaultRequest {
            val token = getToken()
            if (token != null) {
                header("Authorization", "Bearer $token")
            }
        }
    }

    // Backend API base URL — for the web app, API runs on port 8081
    private val baseUrl: String = "http://127.0.0.1:8081"

    private var backendAvailable: Boolean? = null
    private var currentUser: UserDto? = null

    // ─── Token & Cache Helpers ───

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)

    fun getCachedUser(): UserDto? {
        val jsonStr = settings.getStringOrNull(KEY_USER) ?: return null
        return try {
            json.decodeFromString<UserDto>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun cacheSession(token: String, user: UserDto) {
        settings.putString(KEY_TOKEN, token)
        settings.putString(KEY_USER, json.encodeToString(user))
    }

    fun clearCachedSession() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER)
    }

    // ─── User Session & eGov SSO ───

    @Serializable
    data class LoginRequest(val egovCode: String)

    @Serializable
    data class AuthResponse(val token: String, val user: UserDto)

    suspend fun loginWithEgov(code: String): UserDto {
        return try {
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(LoginRequest(code))
            }
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val authRes = response.body<AuthResponse>()
                cacheSession(authRes.token, authRes.user)
                currentUser = authRes.user
                authRes.user
            } else {
                throw Exception("Auth server returned ${response.status}")
            }
        } catch (e: Throwable) {
            println("Auth server login failed, falling back to mock login: ${e.message}")
            // Fallback: Map the code to a demo role and login via mock
            val mappedRole = when (code) {
                "code_applicant" -> UserRole.APPLICANT
                "code_kvkn" -> UserRole.COMMITTEE_STAFF
                "code_expert" -> UserRole.NRCV_EXPERT
                "code_lab" -> UserRole.LAB_ANALYST
                "code_border" -> UserRole.BORDER_INSPECTOR
                "code_warehouse" -> UserRole.WAREHOUSE_CLERK
                "code_farmer" -> UserRole.FARMER_VET
                "code_admin" -> UserRole.ADMIN
                else -> UserRole.APPLICANT
            }
            val mockUser = GisApiClient.login(mappedRole)
            cacheSession("mock-jwt-token-12345", mockUser)
            currentUser = mockUser
            mockUser
        }
    }

    fun login(role: UserRole): UserDto {
        val mockUser = GisApiClient.login(role)
        cacheSession("mock-jwt-token-12345", mockUser)
        currentUser = mockUser
        return mockUser
    }

    fun getCurrentUser(): UserDto? {
        if (currentUser == null) {
            currentUser = getCachedUser()
        }
        return currentUser
    }

    fun logout() {
        currentUser = null
        clearCachedSession()
        GisApiClient.logout()
    }

    fun isBackendAvailable(): Boolean = backendAvailable == true

    // ─── Health Check ───

    @Serializable
    data class HealthResponse(val status: String, val service: String, val version: String)

    suspend fun checkHealth(): HealthResponse? {
        return try {
            val response = client.get("$baseUrl/api/health")
            val result = response.body<HealthResponse>()
            backendAvailable = true
            result
        } catch (e: Throwable) {
            backendAvailable = false
            null
        }
    }

    // ─── Pathways ───

    @Serializable
    data class PathwaysApiResponse(val success: Boolean, val data: List<PathwayInfo>)

    // Synchronous access to pathways (for LazyColumn items)
    val pathways: List<PathwayInfo> get() = GisApiClient.pathways

    // Async version that tries the backend first
    suspend fun getPathways(): List<PathwayInfo> {
        return try {
            val response = client.get("$baseUrl/api/pathways")
            val result = response.body<PathwaysApiResponse>()
            result.data
        } catch (e: Throwable) {
            // Fallback to local mock data
            GisApiClient.pathways
        }
    }

    // ─── Drugs ───
    // Currently uses mock data; will switch to HTTP when DB is available

    fun getDrugs(search: String? = null): List<DrugDto> {
        return GisApiClient.getDrugs(search)
    }

    fun getDrug(id: Long): DrugDto? {
        return GisApiClient.getDrug(id)
    }

    // ─── Applications ───
    // Currently uses mock data; will switch to HTTP when DB is available

    fun getApplications(
        status: ApplicationStatus? = null,
        pathway: RegistrationPathway? = null
    ): List<ApplicationDto> {
        return GisApiClient.getApplications(status, pathway)
    }

    fun getApplication(id: Long): ApplicationDto? {
        return GisApiClient.getApplication(id)
    }

    fun submitApplication(app: ApplicationDto): ApplicationDto {
        return GisApiClient.submitApplication(app)
    }

    // ─── QR Scanning ───

    fun scanQr(code: String): QrScanResultDto {
        return GisApiClient.scanQr(code)
    }

    // ─── Pharmacovigilance (Safety Monitoring) ───

    private val localAdverseEvents = mutableListOf<AdverseEventDto>(
        AdverseEventDto(
            id = 1,
            reporterName = "Жандос Ахметов",
            reporterOrg = "Ветеринарная клиника 'ВетЗабота'",
            phone = "+77015551122",
            drugName = "Тилозин 200",
            batchNumber = "BATCH-T200-001",
            dosageForm = "Раствор для инъекций",
            description = "У двух коров после введения препарата наблюдался анафилактический шок и отек легких.",
            detectionDate = "2026-07-10",
            measuresTaken = "Введение кортикостероидов и антигистаминных средств. Животные выжили.",
            status = "REGISTERED"
        ),
        AdverseEventDto(
            id = 2,
            reporterName = "Иван Иванов",
            reporterOrg = "ТОО 'АгроСоюз'",
            phone = "+77771234567",
            drugName = "Интермектин",
            batchNumber = "BATCH-INT-777",
            dosageForm = "Раствор",
            description = "Аллергическая реакция у овец, сильный зуд и покраснение кожи.",
            detectionDate = "2026-07-12",
            measuresTaken = "Симптоматическая терапия.",
            status = "UNDER_REVIEW"
        )
    )

    suspend fun getAdverseEvents(): List<AdverseEventDto> {
        return try {
            val response = client.get("$baseUrl/api/pharmacovigilance/reports")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<AdverseEventDto>>>().data ?: localAdverseEvents
            } else {
                localAdverseEvents
            }
        } catch (e: Throwable) {
            localAdverseEvents
        }
    }

    suspend fun submitAdverseEvent(report: AdverseEventDto): AdverseEventDto {
        return try {
            val response = client.post("$baseUrl/api/pharmacovigilance/report") {
                contentType(ContentType.Application.Json)
                setBody(report)
            }
            if (response.status == HttpStatusCode.OK) {
                val newReport = response.body<ApiResponse<AdverseEventDto>>().data!!
                val existingIndex = localAdverseEvents.indexOfFirst { it.id == newReport.id }
                if (existingIndex != -1) {
                    localAdverseEvents[existingIndex] = newReport
                } else {
                    localAdverseEvents.add(newReport)
                }
                newReport
            } else {
                val fallback = report.copy(id = (localAdverseEvents.maxOfOrNull { it.id } ?: 0) + 1, status = "REGISTERED")
                localAdverseEvents.add(fallback)
                fallback
            }
        } catch (e: Throwable) {
            val fallback = report.copy(id = (localAdverseEvents.maxOfOrNull { it.id } ?: 0) + 1, status = "REGISTERED")
            localAdverseEvents.add(fallback)
            fallback
        }
    }

    suspend fun submitAdverseEventAction(id: Long, payload: AdverseEventDto): AdverseEventDto {
        return try {
            val response = client.post("$baseUrl/api/pharmacovigilance/report/$id/action") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            if (response.status == HttpStatusCode.OK) {
                val updated = response.body<ApiResponse<AdverseEventDto>>().data!!
                val index = localAdverseEvents.indexOfFirst { it.id == id }
                if (index != -1) {
                    localAdverseEvents[index] = updated
                }
                updated
            } else {
                val index = localAdverseEvents.indexOfFirst { it.id == id }
                if (index != -1) {
                    val existing = localAdverseEvents[index]
                    val updated = existing.copy(measuresTaken = payload.measuresTaken, status = payload.status)
                    localAdverseEvents[index] = updated
                    updated
                } else {
                    payload
                }
            }
        } catch (e: Throwable) {
            val index = localAdverseEvents.indexOfFirst { it.id == id }
            if (index != -1) {
                val existing = localAdverseEvents[index]
                val updated = existing.copy(measuresTaken = payload.measuresTaken, status = payload.status)
                localAdverseEvents[index] = updated
                updated
            } else {
                payload
            }
        }
    }

    // ─── Disposal & Destruction acts (Order No. 16-07/443) ───

    private val localDestructionActs = mutableListOf<DestructionActDto>(
        DestructionActDto(
            id = 1,
            drugName = "Интермектин",
            batchNumber = "BATCH-INT-777",
            volume = 250.0,
            grounds = "LAB_REJECTED",
            denaturationMethod = "Смешивание с фенолом (phenol)",
            destructionMethod = "Сжигание на специализированном полигоне ветеринарных отходов",
            destructionDate = "2026-07-13",
            isPrivateSector = false,
            commissionMembers = listOf("И. С. Сатпаев (Председатель)", "А. К. Байтурсынов", "М. О. Ауэзов"),
            status = "COMPLETED"
        )
    )

    suspend fun getDestructionActs(): List<DestructionActDto> {
        return try {
            val response = client.get("$baseUrl/api/disposal/acts")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<DestructionActDto>>>().data ?: localDestructionActs
            } else {
                localDestructionActs
            }
        } catch (e: Throwable) {
            localDestructionActs
        }
    }

    suspend fun submitDestructionAct(act: DestructionActDto): DestructionActDto {
        return try {
            val response = client.post("$baseUrl/api/disposal/act") {
                contentType(ContentType.Application.Json)
                setBody(act)
            }
            if (response.status == HttpStatusCode.OK) {
                val newAct = response.body<ApiResponse<DestructionActDto>>().data!!
                val existingIndex = localDestructionActs.indexOfFirst { it.id == newAct.id }
                if (existingIndex != -1) {
                    localDestructionActs[existingIndex] = newAct
                } else {
                    localDestructionActs.add(newAct)
                }
                newAct
            } else {
                val fallback = act.copy(id = (localDestructionActs.maxOfOrNull { it.id } ?: 0) + 1, status = "COMPLETED")
                localDestructionActs.add(fallback)
                fallback
            }
        } catch (e: Throwable) {
            val fallback = act.copy(id = (localDestructionActs.maxOfOrNull { it.id } ?: 0) + 1, status = "COMPLETED")
            localDestructionActs.add(fallback)
            fallback
        }
    }

    // ─── System Rebuild (document-driven v2 API) ───

    suspend fun getSystemBlueprint(): SystemBlueprintDto? {
        return try {
            val response = client.get("$baseUrl/api/v2/system/blueprint")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<SystemBlueprintDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getParticipants(type: ParticipantType? = null): List<ParticipantDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/participants") {
                if (type != null) {
                    parameter("type", type.name)
                }
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<ParticipantDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun registerParticipant(participant: ParticipantDto): ParticipantDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/participants") {
                contentType(ContentType.Application.Json)
                setBody(participant)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<ParticipantDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getRolePermissions(): List<RolePermissionDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/security/roles")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<RolePermissionDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun getOperationalSummary(): OperationalSummaryDto? {
        return try {
            val response = client.get("$baseUrl/api/v2/reports/operational-summary")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<OperationalSummaryDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun createRegistrationWorkflow(request: CreateWorkflowRequest): RegistrationWorkflowDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/workflows/registrations") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<RegistrationWorkflowDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getRegistrationWorkflowTimeline(workflowId: Long): WorkflowTimelineDto? {
        return try {
            val response = client.get("$baseUrl/api/v2/workflows/registrations/$workflowId")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<WorkflowTimelineDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun advanceRegistrationWorkflow(workflowId: Long, request: StageTransitionRequest): RegistrationWorkflowDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/workflows/registrations/$workflowId/advance") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<RegistrationWorkflowDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun pauseRegistrationWorkflow(workflowId: Long, request: PauseWorkflowRequest = PauseWorkflowRequest()): RegistrationWorkflowDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/workflows/registrations/$workflowId/pause") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<RegistrationWorkflowDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun resumeRegistrationWorkflow(workflowId: Long, request: ResumeWorkflowRequest = ResumeWorkflowRequest()): RegistrationWorkflowDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/workflows/registrations/$workflowId/resume") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<RegistrationWorkflowDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getSecurityRoles(): List<SecurityRoleDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/security/policies/roles")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<SecurityRoleDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun createSecurityRole(request: CreateSecurityRoleRequest): SecurityRoleDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/security/policies/roles") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<SecurityRoleDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun assignUserRole(request: AssignUserRoleRequest): Boolean {
        return try {
            val response = client.post("$baseUrl/api/v2/security/policies/assignments") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status == HttpStatusCode.OK
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun getIntegrationConnectors(): List<IntegrationConnectorDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/integrations/connectors")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<IntegrationConnectorDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun createIntegrationConnector(request: CreateConnectorRequest): IntegrationConnectorDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/integrations/connectors") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<IntegrationConnectorDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun callIntegration(request: IntegrationCallRequest): IntegrationCallResultDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/integrations/call") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<IntegrationCallResultDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun enqueueOutboxEvent(request: EnqueueOutboxRequest): OutboxEventDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/ops/events/outbox") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<OutboxEventDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getOutboxEvents(status: String = "PENDING"): List<OutboxEventDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/ops/events/outbox") {
                parameter("status", status)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<OutboxEventDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun getDeadLetters(): List<DeadLetterDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/ops/events/dead-letters")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<DeadLetterDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun createDossierVersion(request: CreateDossierDocumentVersionRequest): DossierDocumentVersionDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/dossier/versions") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<DossierDocumentVersionDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getDossierVersions(applicationId: Long): List<DossierDocumentVersionDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/dossier/applications/$applicationId/versions")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<DossierDocumentVersionDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun createTraceabilityEvent(request: CreateTraceabilityEventRequest): TraceabilityEventDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/traceability/events") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<TraceabilityEventDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getTraceabilityEvents(eventType: String? = null): List<TraceabilityEventDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/traceability/events") {
                if (!eventType.isNullOrBlank()) parameter("eventType", eventType)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<TraceabilityEventDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun getReportTemplates(): List<ReportTemplateDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/reports/templates")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<ReportTemplateDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun createReportTemplate(request: CreateReportTemplateRequest): ReportTemplateDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/reports/templates") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<ReportTemplateDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun runReportTemplate(templateId: Long): ReportRunDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/reports/templates/$templateId/run")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<ReportRunDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun submitReport(runId: Long, request: SubmitReportRequest): ReportSubmissionDto? {
        return try {
            val response = client.post("$baseUrl/api/v2/reports/runs/$runId/submit") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<ReportSubmissionDto>>().data
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun getReportSubmissions(): List<ReportSubmissionDto> {
        return try {
            val response = client.get("$baseUrl/api/v2/reports/submissions")
            if (response.status == HttpStatusCode.OK) {
                response.body<ApiResponse<List<ReportSubmissionDto>>>().data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
