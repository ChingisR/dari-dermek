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
import org.jetbrains.exposed.sql.transactions.transaction

private val fallbackParticipants = mutableListOf(
    ParticipantDto(
        id = 1,
        type = ParticipantType.MANUFACTURER,
        name = "ТОО «ВетФарм Казахстан»",
        binIin = "990140000001",
        contactPerson = "Абдрахманов А.А.",
        phone = "+7 727 000 00 01",
        email = "info@vetpharm.kz",
        address = "г. Алматы",
        licenseNumber = "KZ-GMP-2026-001"
    ),
    ParticipantDto(
        id = 2,
        type = ParticipantType.PHARMACY,
        name = "ВетАптека CentralVet",
        binIin = "120640000002",
        contactPerson = "Сейтова К.К.",
        phone = "+7 7172 000 002",
        email = "pharmacy@centralvet.kz",
        address = "г. Астана",
        licenseNumber = "PH-KZ-2026-045"
    )
)

fun Route.blueprintRoutes() {
    route("/api/v2/system") {
        get("/blueprint") {
            call.respond(ApiResponse(success = true, data = buildBlueprint()))
        }
    }

    route("/api/v2/security") {
        get("/roles") {
            call.respond(ApiResponse(success = true, data = rolePermissions()))
        }
    }

    route("/api/v2/reports") {
        get("/operational-summary") {
            val summary = if (DatabaseFactory.isAvailable()) {
                transaction {
                    val countExpr = Participants.id.count()
                    val participantsByType = Participants
                        .select(Participants.type, countExpr)
                        .groupBy(Participants.type)
                        .associate {
                            it[Participants.type] to it[countExpr]
                        }

                    OperationalSummaryDto(
                        participantsByType = participantsByType,
                        activeApplications = Applications.selectAll().where { Applications.status neq ApplicationStatus.CANCELLED.name }.count(),
                        activeDrugs = Drugs.selectAll().where { Drugs.status eq "ACTIVE" }.count(),
                        activeBatches = Batches.selectAll().where { Batches.status eq BatchStatus.ACTIVE.name }.count(),
                        adverseEvents = AdverseEvents.selectAll().count()
                    )
                }
            } else {
                OperationalSummaryDto(
                    participantsByType = fallbackParticipants.groupBy { it.type.name }.mapValues { it.value.size.toLong() },
                    activeApplications = 5,
                    activeDrugs = 8,
                    activeBatches = 3,
                    adverseEvents = 2
                )
            }
            call.respond(ApiResponse(success = true, data = summary))
        }
    }
}

fun Route.participantRoutes() {
    route("/api/v2/participants") {
        get {
            val typeParam = call.request.queryParameters["type"]
            val type = typeParam?.let {
                runCatching { ParticipantType.valueOf(it.uppercase()) }.getOrNull()
            }

            val items = if (DatabaseFactory.isAvailable()) {
                transaction {
                    val baseQuery = if (type != null) {
                        Participants.selectAll().where { Participants.type eq type.name }
                    } else {
                        Participants.selectAll()
                    }
                    baseQuery
                        .orderBy(Participants.createdAt, SortOrder.DESC)
                        .map { it.toParticipantDto() }
                }
            } else {
                if (type == null) fallbackParticipants.toList() else fallbackParticipants.filter { it.type == type }
            }

            call.respond(ApiResponse(success = true, data = items, totalCount = items.size.toLong()))
        }

        post {
            val payload = call.receive<ParticipantDto>()
            val created = if (DatabaseFactory.isAvailable()) {
                transaction {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val newId = Participants.insertAndGetId {
                        it[type] = payload.type.name
                        it[name] = payload.name
                        it[binIin] = payload.binIin
                        it[contactPerson] = payload.contactPerson
                        it[phone] = payload.phone
                        it[email] = payload.email
                        it[address] = payload.address
                        it[licenseNumber] = payload.licenseNumber
                        it[status] = payload.status
                        it[createdAt] = now
                        it[updatedAt] = now
                    }
                    payload.copy(id = newId.value)
                }
            } else {
                val newId = (fallbackParticipants.maxOfOrNull { it.id } ?: 0L) + 1
                val item = payload.copy(id = newId)
                fallbackParticipants.add(item)
                item
            }

            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = created))
        }
    }
}

private fun ResultRow.toParticipantDto() = ParticipantDto(
    id = this[Participants.id].value,
    type = ParticipantType.valueOf(this[Participants.type]),
    name = this[Participants.name],
    binIin = this[Participants.binIin],
    contactPerson = this[Participants.contactPerson],
    phone = this[Participants.phone],
    email = this[Participants.email],
    address = this[Participants.address],
    licenseNumber = this[Participants.licenseNumber],
    status = this[Participants.status]
)

private fun rolePermissions() = listOf(
    RolePermissionDto(UserRole.ADMIN, listOf("access.manage", "participants.manage", "security.audit", "reports.all")),
    RolePermissionDto(UserRole.COMMITTEE_STAFF, listOf("registration.review", "applications.decide", "reports.regulatory")),
    RolePermissionDto(UserRole.NRCV_EXPERT, listOf("lab.protocols", "quality.review", "applications.expert_conclusion")),
    RolePermissionDto(UserRole.LAB_ANALYST, listOf("lab.protocols.write", "samples.receive", "quality.testing")),
    RolePermissionDto(UserRole.BORDER_INSPECTOR, listOf("imports.inspect", "traceability.qr_scan", "batches.hold_release")),
    RolePermissionDto(UserRole.WAREHOUSE_CLERK, listOf("inventory.receive", "inventory.move", "expiry.monitor")),
    RolePermissionDto(UserRole.FARMER_VET, listOf("prescriptions.issue", "adverse_events.report", "batches.verify")),
    RolePermissionDto(UserRole.APPLICANT, listOf("applications.submit", "dossier.upload", "registration.track"))
)

private fun buildBlueprint(): SystemBlueprintDto {
    val subsystems = listOf(
        SubsystemSpecDto(
            key = "security-access",
            title = "Безопасность и управление доступом",
            purpose = "RBAC, аудит действий и контроль безопасности.",
            modules = listOf(
                ModuleSpecDto("access", "Управление доступом", "Назначение ролей и прав."),
                ModuleSpecDto("activity", "Активность пользователей", "Логи действий и поведенческий контроль."),
                ModuleSpecDto("security-audit", "Аудит безопасности", "Проверки инцидентов и соответствия.")
            )
        ),
        SubsystemSpecDto(
            key = "participant-registration",
            title = "Регистрация участников системы",
            purpose = "Регистрация производителей, дистрибьюторов, клиник, аптек, ферм и госорганизаций.",
            modules = listOf(
                ModuleSpecDto("manufacturer", "Регистрация производителей", "Учет регистрационных и лицензирующих сведений."),
                ModuleSpecDto("distributor", "Регистрация дистрибьюторов", "Учет сети поставок."),
                ModuleSpecDto("clinics-pharmacies", "Регистрация клиник и аптек", "Учет точек применения и реализации.")
            )
        ),
        SubsystemSpecDto(
            key = "drug-registration",
            title = "Регистрация и сертификация",
            purpose = "Управление заявками, досье и проверкой данных.",
            modules = listOf(
                ModuleSpecDto("registration", "Регистрация и сертификация", "Жизненный цикл заявки и статусов."),
                ModuleSpecDto("dossier", "Управление документацией", "Хранение и валидация регистрационного досье."),
                ModuleSpecDto("validation", "Проверка данных", "Форматно-логический контроль.")
            )
        ),
        SubsystemSpecDto(
            key = "traceability-pharmacovigilance",
            title = "Прослеживаемость, контроль качества и фармаконадзор",
            purpose = "Мониторинг партий, качества, условий транспортировки и побочных эффектов.",
            modules = listOf(
                ModuleSpecDto("quality", "Проверка качества продукции", "Контроль результатов лабораторий."),
                ModuleSpecDto("transport", "Условия перевозки", "Контроль температурных режимов и маршрутов."),
                ModuleSpecDto("pharmacovigilance", "Фармаконадзор", "Учет и обработка сообщений о нежелательных реакциях.")
            )
        ),
        SubsystemSpecDto(
            key = "inventory-logistics",
            title = "Управление запасами и логистикой",
            purpose = "Учет поступлений, запасов, поставок и сроков годности.",
            modules = listOf(
                ModuleSpecDto("receipts", "Учет поступлений", "Фиксация прихода партий."),
                ModuleSpecDto("stock", "Идентификация запасов", "Мониторинг складских остатков."),
                ModuleSpecDto("expiry", "Контроль сроков годности", "Оповещение о рисках списания.")
            )
        ),
        SubsystemSpecDto(
            key = "analytics-reporting",
            title = "Аналитика и отчетность",
            purpose = "Регламентные отчеты и дашборды для контрольных органов.",
            modules = listOf(
                ModuleSpecDto("dashboards", "Отчеты и дашборды", "Срезы по заявкам, партиям, качеству и рискам.")
            )
        )
    )

    val integrations = listOf(
        ExternalIntegrationDto("ГБД ФЛ/ЮЛ", "Верификация реквизитов участников."),
        ExternalIntegrationDto("eLicense.kz", "Проверка лицензий и разрешительных документов."),
        ExternalIntegrationDto("ИС МЗ РК", "Сверка с отраслевыми медицинскими данными."),
        ExternalIntegrationDto("Enbek.kz", "Сверка данных по участникам и сотрудникам."),
        ExternalIntegrationDto("ИС ГАСК", "Обмен данными по надзорным активностям."),
        ExternalIntegrationDto("ИС ЕАСУ", "Передача данных для государственных реестров."),
        ExternalIntegrationDto("ИС ИСЖ", "Прослеживаемость применения на животных."),
        ExternalIntegrationDto("ИС ЕЭС", "Межгосударственный обмен по ЕАЭС контуру.")
    )

    val securityControls = listOf(
        SecurityControlDto("RBAC", "Ролевое разграничение доступа на основе полномочий."),
        SecurityControlDto("TLS 1.3+", "Шифрование транспортного канала для клиент-серверного взаимодействия."),
        SecurityControlDto("Audit logging", "Непрерывное журналирование критичных действий."),
        SecurityControlDto("IDS/IPS integration", "Интеграция с системами обнаружения и предотвращения вторжений."),
        SecurityControlDto("Data protection", "Соблюдение требований РК по защите персональных данных.")
    )

    return SystemBlueprintDto(
        title = "ИС «ДӘРІ-ДӘРМЕК» — целевая модель",
        source = "docs/ТЗ по ИС Дари Дармек финиш.docx + docs/Рабочая программа для ТЗ.pdf + docs/Презентация Дари-Дармек 1.26.pdf",
        subsystems = subsystems,
        integrations = integrations,
        securityControls = securityControls
    )
}
