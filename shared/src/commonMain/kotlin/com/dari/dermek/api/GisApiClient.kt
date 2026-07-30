package com.dari.dermek.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Mock API client for prototype development.
 * In production, this will make real HTTP calls to the Ktor backend.
 * For the September prototype, we use in-memory data to demonstrate workflows
 * without requiring a running PostgreSQL instance.
 */
object GisApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ─── Mock User Session ───
    private var currentUser: UserDto? = null

    fun login(role: UserRole): UserDto {
        currentUser = when (role) {
            UserRole.APPLICANT -> UserDto(1, "applicant@egov.kz", "ТОО «ВетФарм Казахстан»", UserRole.APPLICANT, "ТОО «ВетФарм Казахстан»")
            UserRole.COMMITTEE_STAFF -> UserDto(2, "kvkn@gov.kz", "Серіков Б.А.", UserRole.COMMITTEE_STAFF, "КВКН МСХ РК")
            UserRole.NRCV_EXPERT -> UserDto(3, "expert@nrcv.kz", "Нурбекова Ж.К.", UserRole.NRCV_EXPERT, "НРЦВ")
            UserRole.LAB_ANALYST -> UserDto(4, "lab@nrcv.kz", "Алиева М.С.", UserRole.LAB_ANALYST, "НРЦВ — Лаборатория микробиологии")
            UserRole.BORDER_INSPECTOR -> UserDto(5, "border@customs.kz", "Касымов Д.Т.", UserRole.BORDER_INSPECTOR, "Таможенная служба — КПП «Хоргос»")
            UserRole.WAREHOUSE_CLERK -> UserDto(6, "warehouse@vetpharm.kz", "Жумабаев А.К.", UserRole.WAREHOUSE_CLERK, "Франко-склад «ВетМедСнаб»")
            UserRole.FARMER_VET -> UserDto(7, "farmer@mail.kz", "Ермеков К.Н.", UserRole.FARMER_VET, "КХ «Жайлау»")
            UserRole.ADMIN -> UserDto(8, "admin@gis.kz", "Администратор", UserRole.ADMIN, "ГИС Дари-дермек")
        }
        return currentUser!!
    }

    fun getCurrentUser(): UserDto? = currentUser
    fun logout() { currentUser = null }

    // ─── Mock Drug Registry ───

    private val mockDrugs = listOf(
        DrugDto(1, "Ивермектин 1%", "Ivermectin", DrugType.PHARMACEUTICAL, "Раствор для инъекций",
            listOf("Ивермектин"), "Bayer AG", "RK-VET-001-2024", "2024-03-15", "2029-03-15",
            isAnnex8 = false, targetAnimals = listOf("КРС", "МРС", "Лошади"), status = "ACTIVE"),
        DrugDto(2, "Вакцина против ящура", null, DrugType.IMMUNOLOGICAL, "Суспензия для инъекций",
            listOf("Инактивированный вирус ящура"), "ФГБНУ ВИЭВ", "RK-VET-002-2023", "2023-07-01", "2028-07-01",
            isAnnex8 = true, targetAnimals = listOf("КРС", "МРС", "Свиньи"), status = "ACTIVE"),
        DrugDto(3, "Альбендазол 10%", "Albendazole", DrugType.PHARMACEUTICAL, "Суспензия для перорального применения",
            listOf("Альбендазол"), "ТОО «ВетФарм Казахстан»", "RK-VET-003-2025", "2025-01-20", "2030-01-20",
            isAnnex8 = false, targetAnimals = listOf("КРС", "МРС", "Лошади", "Собаки"), status = "ACTIVE"),
        DrugDto(4, "Амоксициллин 15%", "Amoxicillin", DrugType.PHARMACEUTICAL, "Суспензия для инъекций",
            listOf("Амоксициллина тригидрат"), "Ceva Santé Animale", "RK-VET-004-2024", "2024-09-10", "2029-09-10",
            isAnnex8 = true, targetAnimals = listOf("КРС", "Свиньи"), status = "ACTIVE"),
        DrugDto(5, "Вакцина против бруцеллёза Rev-1", null, DrugType.IMMUNOLOGICAL, "Лиофилизат",
            listOf("Brucella melitensis Rev-1"), "ФГБНУ ВНИИЗЖ", "RK-VET-005-2022", "2022-05-15", "2027-05-15",
            isAnnex8 = true, targetAnimals = listOf("МРС"), status = "ACTIVE"),
        DrugDto(6, "Креолин-Х", null, DrugType.DISINFECTANT, "Эмульсия",
            listOf("Циперметрин"), "НВЦ «Агроветзащита»", "RK-VET-006-2024", "2024-11-01", "2029-11-01",
            isAnnex8 = false, targetAnimals = listOf("КРС", "МРС", "Лошади"), status = "ACTIVE"),
        DrugDto(7, "Тилозин 200", "Tylosin", DrugType.PHARMACEUTICAL, "Раствор для инъекций",
            listOf("Тилозина тартрат"), "Interchemie", null, null, null,
            isAnnex8 = false, targetAnimals = listOf("КРС", "Свиньи", "Птица"), status = "PENDING"),
        DrugDto(8, "Диагностикум бруцеллёзный", null, DrugType.DIAGNOSTIC, "Антиген",
            listOf("Инактивированный антиген Brucella abortus"), "Щёлковский биокомбинат", "RK-VET-008-2023",
            "2023-04-10", "2028-04-10", isAnnex8 = false, targetAnimals = emptyList(), status = "ACTIVE")
    )

    fun getDrugs(search: String? = null): List<DrugDto> {
        if (search.isNullOrBlank()) return mockDrugs
        val q = search.lowercase()
        return mockDrugs.filter {
            it.tradeName.lowercase().contains(q) ||
            (it.inn?.lowercase()?.contains(q) == true) ||
            (it.registrationNumber?.lowercase()?.contains(q) == true) ||
            it.activeSubstances.any { s -> s.lowercase().contains(q) }
        }
    }

    fun getDrug(id: Long): DrugDto? = mockDrugs.find { it.id == id }

    // ─── Mock Applications ───

    private val mockApplications = mutableListOf(
        ApplicationDto(1, 1, "ТОО «ВетФарм Казахстан»", RegistrationPathway.STANDARD,
            ApplicationStatus.LAB_TESTING, "Тилозин 200", DrugType.PHARMACEUTICAL,
            "Interchemie", "2026-06-01", "2026-10-15", 100, 28, false, 0,
            "Стандартная регистрация нового антибиотика для КРС"),
        ApplicationDto(2, 1, "ТОО «ВетФарм Казахстан»", RegistrationPathway.COMPLIANCE,
            ApplicationStatus.COMPLETENESS_CHECK, "Ивермектин 1%", DrugType.PHARMACEUTICAL,
            "Bayer AG", "2026-07-10", "2026-11-20", 90, 3, false, 0,
            "Приведение в соответствие по правилам ЕАЭС"),
        ApplicationDto(3, 1, "ТОО «ВетФарм Казахстан»", RegistrationPathway.SIMPLIFIED,
            ApplicationStatus.APPROVED, "Альбендазол 10%", DrugType.PHARMACEUTICAL,
            "ТОО «ВетФарм Казахстан»", "2025-11-01", "2025-12-16", 45, 45, false, 0,
            "Упрощённая регистрация генерика"),
        ApplicationDto(4, 1, "ФГБНУ ВНИИЗЖ", RegistrationPathway.CONFIRMATION,
            ApplicationStatus.SUBMITTED, "Вакцина против бруцеллёза Rev-1", DrugType.IMMUNOLOGICAL,
            "ФГБНУ ВНИИЗЖ", "2026-07-12", null, 30, 1, false, 0,
            "Подтверждение регистрации вакцины (5-летний срок)"),
        ApplicationDto(5, 2, "Ceva Santé Animale", RegistrationPathway.RECOGNITION,
            ApplicationStatus.EXPERT_CONCLUSION, "Амоксициллин 15%", DrugType.PHARMACEUTICAL,
            "Ceva Santé Animale", "2026-05-20", "2026-07-18", 45, 38, false, 0,
            "Процедура признания — препарат зарегистрирован в РФ")
    )

    fun getApplications(status: ApplicationStatus? = null, pathway: RegistrationPathway? = null): List<ApplicationDto> {
        var result = mockApplications.toList()
        if (status != null) result = result.filter { it.status == status }
        if (pathway != null) result = result.filter { it.pathway == pathway }
        return result
    }

    fun getApplication(id: Long): ApplicationDto? = mockApplications.find { it.id == id }

    fun submitApplication(app: ApplicationDto): ApplicationDto {
        val maxDays = computeMaxWorkingDays(app.pathway, false)
        val newApp = app.copy(
            id = (mockApplications.maxOfOrNull { it.id } ?: 0) + 1,
            status = ApplicationStatus.SUBMITTED,
            submissionDate = "2026-07-13",
            maxWorkingDays = maxDays,
            workingDaysElapsed = 0
        )
        mockApplications.add(newApp)
        return newApp
    }

    // ─── Mock QR Scanning ───

    private val mockQrData = mapOf(
        "VET-FMD-A102-001" to QrScanResultDto(
            found = true,
            drug = mockDrugs[1], // Вакцина против ящура
            batch = BatchDto(1, 2, "Вакцина против ящура", "A-102", BatchStatus.ACTIVE, true, "VET-FMD-A102-001"),
            registrationValid = true, coldChainIntact = true, alerts = emptyList()
        ),
        "VET-ALB-B205-003" to QrScanResultDto(
            found = true,
            drug = mockDrugs[2], // Альбендазол
            batch = BatchDto(2, 3, "Альбендазол 10%", "B-205", BatchStatus.ACTIVE, true, "VET-ALB-B205-003"),
            registrationValid = true, coldChainIntact = true, alerts = emptyList()
        ),
        "VET-AMOX-C301-007" to QrScanResultDto(
            found = true,
            drug = mockDrugs[3], // Амоксициллин
            batch = BatchDto(3, 4, "Амоксициллин 15%", "C-301", BatchStatus.SUSPENDED, false, "VET-AMOX-C301-007"),
            registrationValid = true, coldChainIntact = false, alerts = listOf("COLD_CHAIN_VIOLATION", "BATCH_SUSPENDED")
        )
    )

    fun scanQr(code: String): QrScanResultDto {
        return mockQrData[code] ?: QrScanResultDto(
            found = false, alerts = listOf("COUNTERFEIT_ALERT")
        )
    }

    // ─── Pathways Reference ───

    val pathways = listOf(
        PathwayInfo("COMPLIANCE", "Приведение в соответствие", "Bringing into Compliance", 90, 70, true, "2030-12-31"),
        PathwayInfo("STANDARD", "Стандартная регистрация", "Standard Registration", 100, 95, true),
        PathwayInfo("SIMPLIFIED", "Упрощённая регистрация", "Simplified (Generic)", 45, 35, true),
        PathwayInfo("CONFIRMATION", "Подтверждение регистрации", "Registration Confirmation", 40, 30, false),
        PathwayInfo("AMENDMENT_WITH_TESTING", "Изменения (с экспертизой)", "Amendment (with testing)", 90, 80, true),
        PathwayInfo("AMENDMENT_WITHOUT_TESTING", "Изменения (без экспертизы)", "Amendment (no testing)", 40, 30, false),
        PathwayInfo("RECOGNITION", "Процедура признания", "Mutual Recognition", 45, 45, false)
    )

    private fun computeMaxWorkingDays(pathway: RegistrationPathway, isAnnex8: Boolean): Int {
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
}
