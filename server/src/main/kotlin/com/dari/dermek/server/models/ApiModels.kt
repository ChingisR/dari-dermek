package com.dari.dermek.server.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

// ─── Enums ───

@Serializable
enum class UserRole {
    APPLICANT,          // Заявитель (manufacturer/importer or proxy)
    COMMITTEE_STAFF,    // Сотрудник КВКН
    NRCV_EXPERT,        // Эксперт НРЦВ
    LAB_ANALYST,        // Лабораторный специалист (micro/chem/clinical)
    BORDER_INSPECTOR,   // Инспектор границы
    WAREHOUSE_CLERK,    // Кладовщик / Дистрибьютор
    FARMER_VET,         // Фермер / Ветеринар
    ADMIN               // System administrator
}

@Serializable
enum class RegistrationPathway {
    COMPLIANCE,         // 1. Приведение в соответствие (70–90 days)
    STANDARD,           // 2. Стандартная регистрация (95–100 days)
    SIMPLIFIED,         // 3. Упрощенная (генерик) (35–45 days)
    CONFIRMATION,       // 4. Подтверждение регистрации (30–40 days)
    AMENDMENT_WITH_TESTING,    // 5a. Изменения с экспертизой образцов (80–90 days)
    AMENDMENT_WITHOUT_TESTING, // 5b. Изменения без экспертизы (30–40 days)
    RECOGNITION         // 6. Процедура признания (45 days)
}

@Serializable
enum class ApplicationStatus {
    DRAFT,                  // Черновик
    SUBMITTED,              // Подана
    COMPLETENESS_CHECK,     // Проверка комплектности (10 working days)
    COMPLETENESS_FAILED,    // Комплектность не пройдена (query sent)
    QUERY_SENT,             // Запрос доп. информации (clock paused, up to 90+180 days)
    QUERY_RESPONDED,        // Ответ на запрос получен
    SAMPLES_REQUESTED,      // Образцы запрошены (45 working days to deliver)
    SAMPLES_RECEIVED,       // Образцы получены в НРЦВ
    LAB_TESTING,            // Лабораторные испытания
    LAB_COMPLETE,           // Испытания завершены
    EXPERT_CONCLUSION,      // Экспертное заключение
    COMMITTEE_REVIEW,       // На рассмотрении Комитета
    APPROVED,               // Зарегистрирован
    REJECTED,               // Отказано
    SUSPENDED,              // Приостановлен
    CANCELLED               // Аннулирован
}

@Serializable
enum class DrugType {
    PHARMACEUTICAL,         // Фармацевтический
    IMMUNOLOGICAL,          // Иммунологический (иммунобиологический)
    DIAGNOSTIC,             // Диагностический
    DISINFECTANT,           // Дезинфицирующее средство
    FEED_ADDITIVE           // Кормовая добавка
}

@Serializable
enum class BatchStatus {
    ACTIVE,                 // В обороте
    SUSPENDED,              // Приостановлен
    RECALLED,               // Отозван
    DESTROYED,              // Уничтожен (денатурирован)
    EXPIRED                 // Срок годности истёк
}

@Serializable
enum class LabDiscipline {
    MICROBIOLOGY,           // Микробиология
    CHEMISTRY,              // Химия
    CLINICAL                // Клиника
}

// ─── API Data Transfer Objects ───

@Serializable
data class UserDto(
    val id: Long = 0,
    val login: String,
    val fullName: String,
    val role: UserRole,
    val organization: String? = null,
    val egovId: String? = null,    // eGov SSO identifier
    val ecpSerial: String? = null  // ECP/EDS certificate serial
)

@Serializable
data class DrugDto(
    val id: Long = 0,
    val tradeName: String,                    // Торговое наименование
    val inn: String? = null,                  // МНН (International Nonproprietary Name)
    val type: DrugType,
    val dosageForm: String? = null,           // Лекарственная форма
    val activeSubstances: List<String> = emptyList(),
    val manufacturerId: Long? = null,
    val manufacturerName: String? = null,
    val registrationNumber: String? = null,   // Номер регистрационного удостоверения
    val registrationDate: String? = null,
    val expiryDate: String? = null,           // Срок действия регистрации
    val isAnnex8: Boolean = false,            // Приложение №8
    val isAnnex16: Boolean = false,           // Приложение №16
    val targetAnimals: List<String> = emptyList(),
    val status: String = "ACTIVE"             // ACTIVE, SUSPENDED, CANCELLED
)

@Serializable
data class ManufacturerDto(
    val id: Long = 0,
    val name: String,
    val country: String,
    val address: String? = null,
    val gmpCertificateNumber: String? = null,
    val gmpExpiryDate: String? = null,
    val productionSites: List<String> = emptyList()
)

@Serializable
data class ApplicationDto(
    val id: Long = 0,
    val applicantId: Long,
    val applicantName: String? = null,
    val pathway: RegistrationPathway,
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val drugTradeName: String,
    val drugType: DrugType,
    val manufacturerName: String? = null,
    val submissionDate: String? = null,
    val deadlineDate: String? = null,       // Computed from pathway + annex classification
    val maxWorkingDays: Int? = null,
    val workingDaysElapsed: Int = 0,
    val isClockPaused: Boolean = false,
    val queryCount: Int = 0,
    val notes: String? = null,
    val statusHistory: List<StatusChangeDto> = emptyList()
)

@Serializable
data class StatusChangeDto(
    val status: ApplicationStatus,
    val changedAt: String,
    val changedBy: String? = null,
    val comment: String? = null
)

@Serializable
data class DossierPartDto(
    val id: Long = 0,
    val applicationId: Long,
    val partNumber: Int,          // CTD Part 1–4
    val partTitle: String,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val uploadedAt: String? = null,
    val isValid: Boolean? = null  // Automated completeness check result
)

@Serializable
data class BatchDto(
    val id: Long = 0,
    val drugId: Long,
    val drugName: String? = null,
    val batchNumber: String,
    val manufacturingDate: String? = null,
    val expiryDate: String? = null,
    val volume: Double? = null,         // Liters or doses
    val volumeUnit: String? = null,     // "L", "doses", "vials"
    val status: BatchStatus = BatchStatus.ACTIVE,
    val importDeclarationId: Long? = null,
    val borderVolume: Double? = null,   // Volume at customs
    val warehouseVolume: Double? = null, // Volume received at warehouse
    val destinationWarehouse: String? = null,
    val coldChainOk: Boolean = true,
    val temperatureMin: Double? = null,
    val temperatureMax: Double? = null,
    val qrCode: String? = null
)

@Serializable
data class QrScanResultDto(
    val found: Boolean,
    val drug: DrugDto? = null,
    val batch: BatchDto? = null,
    val registrationValid: Boolean = false,
    val coldChainIntact: Boolean = true,
    val alerts: List<String> = emptyList()
)

@Serializable
data class LabProtocolDto(
    val id: Long = 0,
    val applicationId: Long,
    val discipline: LabDiscipline,
    val analystName: String? = null,
    val status: String = "PENDING",     // PENDING, IN_PROGRESS, SIGNED, REJECTED
    val findings: String? = null,
    val signedAt: String? = null,
    val signedBy: String? = null
)

@Serializable
data class ControlPurchaseDto(
    val id: Long = 0,
    val inspectorId: Long,
    val batchId: Long,
    val drugName: String? = null,
    val batchNumber: String? = null,
    val purchaseDate: String? = null,
    val purchaseLocation: String? = null,
    val labResult: String? = null,       // PASS, FAIL, PENDING
    val actionTaken: String? = null       // SUSPENDED, RECALLED, DESTROYED, NONE
)

// ─── Auth DTOs ───

@Serializable
data class LoginRequest(
    val egovCode: String  // OAuth2 authorization code from eGov SSO
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

// ─── API Response Wrappers ───

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val totalCount: Long? = null
)

@Serializable
data class PaginatedRequest(
    val page: Int = 1,
    val pageSize: Int = 20,
    val search: String? = null,
    val sortBy: String? = null,
    val sortDirection: String = "ASC"
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
