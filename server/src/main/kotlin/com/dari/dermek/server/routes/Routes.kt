package com.dari.dermek.server.routes

import com.dari.dermek.server.models.*
import com.dari.dermek.server.db.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.ktor.server.auth.*

fun Route.drugRoutes() {
    route("/api/drugs") {
        // GET /api/drugs — list/search drugs
        get {
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            val result = transaction {
                val query = if (search != null) {
                    Drugs.selectAll().where {
                        (Drugs.tradeName.lowerCase() like "%${search.lowercase()}%") or
                        (Drugs.inn.lowerCase() like "%${search.lowercase()}%") or
                        (Drugs.registrationNumber.lowerCase() like "%${search.lowercase()}%")
                    }
                } else {
                    Drugs.selectAll()
                }

                val totalCount = query.count()
                val items = query
                    .orderBy(Drugs.tradeName)
                    .limit(pageSize)
                    .offset(((page - 1) * pageSize).toLong())
                    .map { it.toDrugDto() }

                ApiResponse(success = true, data = items, totalCount = totalCount)
            }
            call.respond(result)
        }

        // GET /api/drugs/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<DrugDto>(success = false, error = "Invalid ID"))

            val drug = transaction {
                Drugs.selectAll().where { Drugs.id eq id }.firstOrNull()?.toDrugDto()
            }

            if (drug != null) {
                call.respond(ApiResponse(success = true, data = drug))
            } else {
                call.respond(HttpStatusCode.NotFound,
                    ApiResponse<DrugDto>(success = false, error = "Drug not found"))
            }
        }

        // POST /api/drugs — create new drug (requires auth)
        authenticate("auth-jwt") {
            post {
                val dto = call.receive<DrugDto>()
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                val created = transaction {
                    val newId = Drugs.insertAndGetId {
                        it[tradeName] = dto.tradeName
                        it[inn] = dto.inn
                        it[type] = dto.type.name
                        it[dosageForm] = dto.dosageForm
                        it[activeSubstances] = Json.encodeToString(ListSerializer(String.serializer()), dto.activeSubstances)
                        it[isAnnex8] = dto.isAnnex8
                        it[isAnnex16] = dto.isAnnex16
                        it[targetAnimals] = Json.encodeToString(ListSerializer(String.serializer()), dto.targetAnimals)
                        it[status] = dto.status
                        it[createdAt] = now
                        it[updatedAt] = now
                    }
                    dto.copy(id = newId.value)
                }

                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
            }
        }
    }
}

fun Route.applicationRoutes() {
    route("/api/applications") {
        // GET /api/applications
        get {
            val status = call.request.queryParameters["status"]
            val pathway = call.request.queryParameters["pathway"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            val result = transaction {
                var query = Applications.selectAll()

                if (status != null) {
                    query = query.andWhere { Applications.status eq status }
                }
                if (pathway != null) {
                    query = query.andWhere { Applications.pathway eq pathway }
                }

                val totalCount = query.count()
                val items = query
                    .orderBy(Applications.createdAt, SortOrder.DESC)
                    .limit(pageSize)
                    .offset(((page - 1) * pageSize).toLong())
                    .map { it.toApplicationDto() }

                ApiResponse(success = true, data = items, totalCount = totalCount)
            }
            call.respond(result)
        }

        // GET /api/applications/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<ApplicationDto>(success = false, error = "Invalid ID"))

            val app = transaction {
                val row = Applications.selectAll().where { Applications.id eq id }.firstOrNull()
                    ?: return@transaction null
                val history = ApplicationStatusHistory.selectAll()
                    .where { ApplicationStatusHistory.applicationId eq id }
                    .orderBy(ApplicationStatusHistory.changedAt, SortOrder.ASC)
                    .map {
                        StatusChangeDto(
                            status = ApplicationStatus.valueOf(it[ApplicationStatusHistory.status]),
                            changedAt = it[ApplicationStatusHistory.changedAt].toString(),
                            changedBy = it[ApplicationStatusHistory.changedBy],
                            comment = it[ApplicationStatusHistory.comment]
                        )
                    }
                row.toApplicationDto().copy(statusHistory = history)
            }

            if (app != null) {
                call.respond(ApiResponse(success = true, data = app))
            } else {
                call.respond(HttpStatusCode.NotFound,
                    ApiResponse<ApplicationDto>(success = false, error = "Application not found"))
            }
        }

        // POST /api/applications — submit new application (requires auth)
        authenticate("auth-jwt") {
            post {
                val dto = call.receive<ApplicationDto>()
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                val maxDays = computeMaxWorkingDays(dto.pathway, isAnnex8 = false) // TODO: lookup drug

                val created = transaction {
                    val newId = Applications.insertAndGetId {
                        it[applicantId] = dto.applicantId
                        it[pathway] = dto.pathway.name
                        it[status] = ApplicationStatus.SUBMITTED.name
                        it[drugTradeName] = dto.drugTradeName
                        it[drugType] = dto.drugType.name
                        it[manufacturerName] = dto.manufacturerName
                        it[submissionDate] = now.date.toString()
                        it[maxWorkingDays] = maxDays
                        it[notes] = dto.notes
                        it[createdAt] = now
                        it[updatedAt] = now
                    }

                    // Record initial status
                    ApplicationStatusHistory.insert {
                        it[applicationId] = newId.value
                        it[ApplicationStatusHistory.status] = ApplicationStatus.SUBMITTED.name
                        it[changedAt] = now
                        it[comment] = "Application submitted"
                    }

                    dto.copy(
                        id = newId.value,
                        status = ApplicationStatus.SUBMITTED,
                        submissionDate = now.date.toString(),
                        maxWorkingDays = maxDays
                    )
                }

                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
            }
        }
    }
}

fun Route.batchRoutes() {
    route("/api/batches") {
        get {
            val drugId = call.request.queryParameters["drugId"]?.toLongOrNull()
            val result = transaction {
                val query = if (drugId != null) {
                    Batches.selectAll().where { Batches.drugId eq drugId }
                } else {
                    Batches.selectAll()
                }
                val items = query.orderBy(Batches.createdAt, SortOrder.DESC).map { it.toBatchDto() }
                ApiResponse(success = true, data = items, totalCount = items.size.toLong())
            }
            call.respond(result)
        }
    }

    // QR lookup endpoint
    route("/api/qr") {
        get("{code}") {
            val code = call.parameters["code"]
                ?: return@get call.respond(HttpStatusCode.BadRequest,
                    ApiResponse<QrScanResultDto>(success = false, error = "Missing QR code"))

            val result = transaction {
                val vial = QrVials.selectAll().where { QrVials.serialNumber eq code }.firstOrNull()

                if (vial != null) {
                    val batch = Batches.selectAll()
                        .where { Batches.id eq vial[QrVials.batchId] }.firstOrNull()
                    val drug = batch?.let {
                        Drugs.selectAll().where { Drugs.id eq it[Batches.drugId] }.firstOrNull()
                    }

                    val alerts = mutableListOf<String>()
                    if (batch != null && !batch[Batches.coldChainOk]) {
                        alerts.add("COLD_CHAIN_VIOLATION")
                    }
                    if (batch != null && batch[Batches.status] == "SUSPENDED") {
                        alerts.add("BATCH_SUSPENDED")
                    }
                    if (drug != null && drug[Drugs.status] != "ACTIVE") {
                        alerts.add("DRUG_NOT_ACTIVE")
                    }

                    QrScanResultDto(
                        found = true,
                        drug = drug?.toDrugDto(),
                        batch = batch?.toBatchDto(),
                        registrationValid = drug?.get(Drugs.status) == "ACTIVE",
                        coldChainIntact = batch?.get(Batches.coldChainOk) ?: true,
                        alerts = alerts
                    )
                } else {
                    QrScanResultDto(
                        found = false,
                        alerts = listOf("COUNTERFEIT_ALERT")
                    )
                }
            }

            call.respond(ApiResponse(success = true, data = result))
        }
    }
}

fun Route.labRoutes() {
    route("/api/lab/protocols") {
        get {
            val applicationId = call.request.queryParameters["applicationId"]?.toLongOrNull()
            val result = transaction {
                val query = if (applicationId != null) {
                    LabProtocols.selectAll().where { LabProtocols.applicationId eq applicationId }
                } else {
                    LabProtocols.selectAll()
                }
                val items = query.map {
                    LabProtocolDto(
                        id = it[LabProtocols.id].value,
                        applicationId = it[LabProtocols.applicationId],
                        discipline = LabDiscipline.valueOf(it[LabProtocols.discipline]),
                        analystName = it[LabProtocols.analystName],
                        status = it[LabProtocols.status],
                        findings = it[LabProtocols.findings],
                        signedAt = it[LabProtocols.signedAt]?.toString(),
                        signedBy = it[LabProtocols.signedBy]
                    )
                }
                ApiResponse(success = true, data = items, totalCount = items.size.toLong())
            }
            call.respond(result)
        }
    }
}

fun Route.controlPurchaseRoutes() {
    route("/api/control-purchases") {
        get {
            val result = transaction {
                val items = ControlPurchases.selectAll()
                    .orderBy(ControlPurchases.createdAt, SortOrder.DESC)
                    .map {
                        ControlPurchaseDto(
                            id = it[ControlPurchases.id].value,
                            inspectorId = it[ControlPurchases.inspectorId],
                            batchId = it[ControlPurchases.batchId],
                            purchaseDate = it[ControlPurchases.purchaseDate],
                            purchaseLocation = it[ControlPurchases.purchaseLocation],
                            labResult = it[ControlPurchases.labResult],
                            actionTaken = it[ControlPurchases.actionTaken]
                        )
                    }
                ApiResponse(success = true, data = items, totalCount = items.size.toLong())
            }
            call.respond(result)
        }
    }
}

// ─── Helper: Row → DTO mappers ───

fun ResultRow.toUserDto() = UserDto(
    id = this[Users.id].value,
    login = this[Users.login],
    fullName = this[Users.fullName],
    role = UserRole.valueOf(this[Users.role]),
    organization = this[Users.organization],
    egovId = this[Users.egovId],
    ecpSerial = this[Users.ecpSerial]
)

private fun ResultRow.toDrugDto() = DrugDto(
    id = this[Drugs.id].value,
    tradeName = this[Drugs.tradeName],
    inn = this[Drugs.inn],
    type = DrugType.valueOf(this[Drugs.type]),
    dosageForm = this[Drugs.dosageForm],
    activeSubstances = try { Json.decodeFromString(this[Drugs.activeSubstances]) } catch (_: Exception) { emptyList() },
    registrationNumber = this[Drugs.registrationNumber],
    registrationDate = this[Drugs.registrationDate],
    expiryDate = this[Drugs.expiryDate],
    isAnnex8 = this[Drugs.isAnnex8],
    isAnnex16 = this[Drugs.isAnnex16],
    targetAnimals = try { Json.decodeFromString(this[Drugs.targetAnimals]) } catch (_: Exception) { emptyList() },
    status = this[Drugs.status]
)

private fun ResultRow.toApplicationDto() = ApplicationDto(
    id = this[Applications.id].value,
    applicantId = this[Applications.applicantId],
    pathway = RegistrationPathway.valueOf(this[Applications.pathway]),
    status = ApplicationStatus.valueOf(this[Applications.status]),
    drugTradeName = this[Applications.drugTradeName],
    drugType = DrugType.valueOf(this[Applications.drugType]),
    manufacturerName = this[Applications.manufacturerName],
    submissionDate = this[Applications.submissionDate],
    deadlineDate = this[Applications.deadlineDate],
    maxWorkingDays = this[Applications.maxWorkingDays],
    workingDaysElapsed = this[Applications.workingDaysElapsed],
    isClockPaused = this[Applications.isClockPaused],
    queryCount = this[Applications.queryCount],
    notes = this[Applications.notes]
)

private fun ResultRow.toBatchDto() = BatchDto(
    id = this[Batches.id].value,
    drugId = this[Batches.drugId],
    batchNumber = this[Batches.batchNumber],
    manufacturingDate = this[Batches.manufacturingDate],
    expiryDate = this[Batches.expiryDate],
    volume = this[Batches.volume],
    volumeUnit = this[Batches.volumeUnit],
    status = BatchStatus.valueOf(this[Batches.status]),
    importDeclarationId = this[Batches.importDeclarationId],
    borderVolume = this[Batches.borderVolume],
    warehouseVolume = this[Batches.warehouseVolume],
    destinationWarehouse = this[Batches.destinationWarehouse],
    coldChainOk = this[Batches.coldChainOk],
    temperatureMin = this[Batches.temperatureMin],
    temperatureMax = this[Batches.temperatureMax],
    qrCode = this[Batches.qrCode]
)

private fun ResultRow.toAdverseEventDto() = AdverseEventDto(
    id = this[AdverseEvents.id].value,
    reporterName = this[AdverseEvents.reporterName],
    reporterOrg = this[AdverseEvents.reporterOrg],
    phone = this[AdverseEvents.phone],
    drugName = this[AdverseEvents.drugName],
    batchNumber = this[AdverseEvents.batchNumber],
    dosageForm = this[AdverseEvents.dosageForm],
    description = this[AdverseEvents.description],
    detectionDate = this[AdverseEvents.detectionDate],
    measuresTaken = this[AdverseEvents.measuresTaken],
    status = this[AdverseEvents.status]
)

private fun ResultRow.toDestructionActDto() = DestructionActDto(
    id = this[DestructionActs.id].value,
    drugName = this[DestructionActs.drugName],
    batchNumber = this[DestructionActs.batchNumber],
    volume = this[DestructionActs.volume],
    grounds = this[DestructionActs.grounds],
    denaturationMethod = this[DestructionActs.denaturationMethod],
    destructionMethod = this[DestructionActs.destructionMethod],
    destructionDate = this[DestructionActs.destructionDate],
    isPrivateSector = this[DestructionActs.isPrivateSector],
    commissionMembers = Json.decodeFromString(ListSerializer(String.serializer()), this[DestructionActs.commissionMembers]),
    status = this[DestructionActs.status]
)



// ─── Business Logic: Timeline Computation ───

fun computeMaxWorkingDays(pathway: RegistrationPathway, isAnnex8: Boolean): Int {
    return when (pathway) {
        RegistrationPathway.COMPLIANCE -> if (isAnnex8) 70 else 90
        RegistrationPathway.STANDARD -> if (isAnnex8) 95 else 100
        RegistrationPathway.SIMPLIFIED -> if (isAnnex8) 35 else 45
        RegistrationPathway.CONFIRMATION -> if (isAnnex8) 30 else 40
        RegistrationPathway.AMENDMENT_WITH_TESTING -> if (isAnnex8) 80 else 90
        RegistrationPathway.AMENDMENT_WITHOUT_TESTING -> if (isAnnex8) 30 else 40
        RegistrationPathway.RECOGNITION -> 45
    }
}

private val adverseEvents = mutableListOf<AdverseEventDto>(
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

fun Route.pharmacovigilanceRoutes() {
    route("/api/pharmacovigilance") {
        get("/reports") {
            if (DatabaseFactory.isAvailable()) {
                val items = transaction {
                    AdverseEvents.selectAll().orderBy(AdverseEvents.id, SortOrder.DESC).map { it.toAdverseEventDto() }
                }
                call.respond(ApiResponse(success = true, data = items))
            } else {
                call.respond(ApiResponse(success = true, data = adverseEvents))
            }
        }

        post("/report") {
            val report = call.receive<AdverseEventDto>()
            if (DatabaseFactory.isAvailable()) {
                val newReport = transaction {
                    val idValue = AdverseEvents.insertAndGetId {
                        it[reporterName] = report.reporterName
                        it[reporterOrg] = report.reporterOrg
                        it[phone] = report.phone
                        it[drugName] = report.drugName
                        it[batchNumber] = report.batchNumber
                        it[dosageForm] = report.dosageForm
                        it[description] = report.description
                        it[detectionDate] = report.detectionDate
                        it[measuresTaken] = report.measuresTaken
                        it[status] = "REGISTERED"
                        it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    }
                    AdverseEvents.selectAll().where { AdverseEvents.id eq idValue }.first().toAdverseEventDto()
                }
                call.respond(ApiResponse(success = true, data = newReport))
            } else {
                val newReport = report.copy(
                    id = (adverseEvents.maxOfOrNull { it.id } ?: 0) + 1,
                    status = "REGISTERED"
                )
                adverseEvents.add(newReport)
                call.respond(ApiResponse(success = true, data = newReport))
            }
        }

        post("/report/{id}/action") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<AdverseEventDto>(success = false, error = "Invalid ID"))
            
            val payload = call.receive<AdverseEventDto>()
            if (DatabaseFactory.isAvailable()) {
                val updated = transaction {
                    AdverseEvents.update({ AdverseEvents.id eq id }) {
                        if (payload.measuresTaken != null) {
                            it[measuresTaken] = payload.measuresTaken
                        }
                        it[status] = payload.status
                    }
                    AdverseEvents.selectAll().where { AdverseEvents.id eq id }.firstOrNull()?.toAdverseEventDto()
                }
                if (updated != null) {
                    call.respond(ApiResponse(success = true, data = updated))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse<AdverseEventDto>(success = false, error = "Report not found"))
                }
            } else {
                val index = adverseEvents.indexOfFirst { it.id == id }
                if (index == -1) {
                    return@post call.respond(HttpStatusCode.NotFound, ApiResponse<AdverseEventDto>(success = false, error = "Report not found"))
                }

                val existing = adverseEvents[index]
                val updated = existing.copy(
                    measuresTaken = payload.measuresTaken ?: existing.measuresTaken,
                    status = payload.status
                )
                adverseEvents[index] = updated
                call.respond(ApiResponse(success = true, data = updated))
            }
        }
    }
}

private val destructionActs = mutableListOf<DestructionActDto>(
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

fun Route.disposalRoutes() {
    route("/api/disposal") {
        get("/acts") {
            if (DatabaseFactory.isAvailable()) {
                val items = transaction {
                    DestructionActs.selectAll().orderBy(DestructionActs.id, SortOrder.DESC).map { it.toDestructionActDto() }
                }
                call.respond(ApiResponse(success = true, data = items))
            } else {
                call.respond(ApiResponse(success = true, data = destructionActs))
            }
        }

        post("/act") {
            val act = call.receive<DestructionActDto>()
            if (DatabaseFactory.isAvailable()) {
                val newAct = transaction {
                    val idValue = DestructionActs.insertAndGetId {
                        it[drugName] = act.drugName
                        it[batchNumber] = act.batchNumber
                        it[volume] = act.volume
                        it[grounds] = act.grounds
                        it[denaturationMethod] = act.denaturationMethod
                        it[destructionMethod] = act.destructionMethod
                        it[destructionDate] = act.destructionDate
                        it[isPrivateSector] = act.isPrivateSector
                        it[commissionMembers] = Json.encodeToString(ListSerializer(String.serializer()), act.commissionMembers)
                        it[status] = "COMPLETED"
                        it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    }
                    DestructionActs.selectAll().where { DestructionActs.id eq idValue }.first().toDestructionActDto()
                }
                call.respond(ApiResponse(success = true, data = newAct))
            } else {
                val newAct = act.copy(
                    id = (destructionActs.maxOfOrNull { it.id } ?: 0) + 1,
                    status = "COMPLETED"
                )
                destructionActs.add(newAct)
                call.respond(ApiResponse(success = true, data = newAct))
            }
        }
    }
}
