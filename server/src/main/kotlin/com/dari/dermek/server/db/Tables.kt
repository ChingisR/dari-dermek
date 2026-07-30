package com.dari.dermek.server.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

// ─── Users & Roles ───

object Users : LongIdTable("users") {
    val login = varchar("login", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 512).nullable()
    val fullName = varchar("full_name", 500)
    val role = varchar("role", 50)           // UserRole enum name
    val organization = varchar("organization", 500).nullable()
    val egovId = varchar("egov_id", 255).nullable().uniqueIndex()
    val ecpSerial = varchar("ecp_serial", 255).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Participants : LongIdTable("participants") {
    val type = varchar("type", 50) // MANUFACTURER, DISTRIBUTOR, CLINIC, PHARMACY, FARM, GOVERNMENT
    val name = varchar("name", 500)
    val binIin = varchar("bin_iin", 20).nullable()
    val contactPerson = varchar("contact_person", 255).nullable()
    val phone = varchar("phone", 100).nullable()
    val email = varchar("email", 255).nullable()
    val address = text("address").nullable()
    val licenseNumber = varchar("license_number", 100).nullable()
    val status = varchar("status", 50).default("ACTIVE")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Drug Registry ───

object Drugs : LongIdTable("drugs") {
    val tradeName = varchar("trade_name", 500)
    val inn = varchar("inn", 500).nullable()
    val type = varchar("type", 50)           // DrugType enum
    val dosageForm = varchar("dosage_form", 255).nullable()
    val activeSubstances = text("active_substances").default("[]") // JSON array
    val manufacturerId = long("manufacturer_id").references(Manufacturers.id).nullable()
    val registrationNumber = varchar("registration_number", 100).nullable().uniqueIndex()
    val registrationDate = varchar("registration_date", 20).nullable()
    val expiryDate = varchar("expiry_date", 20).nullable()
    val isAnnex8 = bool("is_annex8").default(false)
    val isAnnex16 = bool("is_annex16").default(false)
    val targetAnimals = text("target_animals").default("[]") // JSON array
    val status = varchar("status", 50).default("ACTIVE")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Manufacturers ───

object Manufacturers : LongIdTable("manufacturers") {
    val name = varchar("name", 500)
    val country = varchar("country", 100)
    val address = text("address").nullable()
    val gmpCertificateNumber = varchar("gmp_certificate_number", 255).nullable()
    val gmpExpiryDate = varchar("gmp_expiry_date", 20).nullable()
    val productionSites = text("production_sites").default("[]") // JSON array
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Registration Applications ───

object Applications : LongIdTable("applications") {
    val applicantId = long("applicant_id").references(Users.id)
    val pathway = varchar("pathway", 50)     // RegistrationPathway enum
    val status = varchar("status", 50)       // ApplicationStatus enum
    val drugTradeName = varchar("drug_trade_name", 500)
    val drugType = varchar("drug_type", 50)
    val manufacturerName = varchar("manufacturer_name", 500).nullable()
    val submissionDate = varchar("submission_date", 20).nullable()
    val deadlineDate = varchar("deadline_date", 20).nullable()
    val maxWorkingDays = integer("max_working_days").nullable()
    val workingDaysElapsed = integer("working_days_elapsed").default(0)
    val isClockPaused = bool("is_clock_paused").default(false)
    val clockPausedAt = varchar("clock_paused_at", 20).nullable()
    val queryCount = integer("query_count").default(0)
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Application Status History ───

object ApplicationStatusHistory : LongIdTable("application_status_history") {
    val applicationId = long("application_id").references(Applications.id)
    val status = varchar("status", 50)
    val changedAt = datetime("changed_at")
    val changedBy = varchar("changed_by", 255).nullable()
    val comment = text("comment").nullable()
}

// ─── Dossier Parts (CTD Parts 1–4) ───

object DossierParts : LongIdTable("dossier_parts") {
    val applicationId = long("application_id").references(Applications.id)
    val partNumber = integer("part_number")  // 1–4
    val partTitle = varchar("part_title", 500)
    val fileName = varchar("file_name", 500).nullable()
    val fileSize = long("file_size").nullable()
    val filePath = text("file_path").nullable()  // storage path
    val uploadedAt = datetime("uploaded_at").nullable()
    val isValid = bool("is_valid").nullable()
}

// ─── Batches (партии) ───

object Batches : LongIdTable("batches") {
    val drugId = long("drug_id").references(Drugs.id)
    val batchNumber = varchar("batch_number", 100)
    val manufacturingDate = varchar("manufacturing_date", 20).nullable()
    val expiryDate = varchar("expiry_date", 20).nullable()
    val volume = double("volume").nullable()
    val volumeUnit = varchar("volume_unit", 20).nullable()
    val status = varchar("status", 50).default("ACTIVE")
    val importDeclarationId = long("import_declaration_id").nullable()
    val borderVolume = double("border_volume").nullable()
    val warehouseVolume = double("warehouse_volume").nullable()
    val destinationWarehouse = varchar("destination_warehouse", 500).nullable()
    val coldChainOk = bool("cold_chain_ok").default(true)
    val temperatureMin = double("temperature_min").nullable()
    val temperatureMax = double("temperature_max").nullable()
    val qrCode = varchar("qr_code", 500).nullable().uniqueIndex()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── QR Vial Tracking ───

object QrVials : LongIdTable("qr_vials") {
    val batchId = long("batch_id").references(Batches.id)
    val serialNumber = varchar("serial_number", 255).uniqueIndex()
    val qrData = text("qr_data")
    val scannedCount = integer("scanned_count").default(0)
    val lastScannedAt = datetime("last_scanned_at").nullable()
    val lastScannedBy = varchar("last_scanned_by", 255).nullable()
    val lastScannedLocation = varchar("last_scanned_location", 500).nullable()
}

// ─── Lab Protocols (LIS) ───

object LabProtocols : LongIdTable("lab_protocols") {
    val applicationId = long("application_id").references(Applications.id)
    val discipline = varchar("discipline", 50)   // LabDiscipline enum
    val analystId = long("analyst_id").references(Users.id).nullable()
    val analystName = varchar("analyst_name", 255).nullable()
    val status = varchar("status", 50).default("PENDING")
    val findings = text("findings").nullable()
    val signedAt = datetime("signed_at").nullable()
    val signedBy = varchar("signed_by", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Control Purchases ───

object ControlPurchases : LongIdTable("control_purchases") {
    val inspectorId = long("inspector_id").references(Users.id)
    val batchId = long("batch_id").references(Batches.id)
    val purchaseDate = varchar("purchase_date", 20).nullable()
    val purchaseLocation = varchar("purchase_location", 500).nullable()
    val sampleCount = integer("sample_count").default(0)
    val labResult = varchar("lab_result", 50).nullable() // PASS, FAIL, PENDING
    val actionTaken = varchar("action_taken", 50).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Import Declarations ───

object ImportDeclarations : LongIdTable("import_declarations") {
    val importerId = long("importer_id").references(Users.id).nullable()
    val drugId = long("drug_id").references(Drugs.id).nullable()
    val declarationNumber = varchar("declaration_number", 255)
    val borderCheckpoint = varchar("border_checkpoint", 500).nullable()
    val declaredVolume = double("declared_volume").nullable()
    val volumeUnit = varchar("volume_unit", 20).nullable()
    val destination = text("destination").nullable()
    val isRegistered = bool("is_registered").default(false) // drug has valid EAEU registration?
    val isParked = bool("is_parked").default(false)         // held at customs?
    val arrivedAt = datetime("arrived_at").nullable()
    val clearedAt = datetime("cleared_at").nullable()
    val createdAt = datetime("created_at")
}

// ─── Vet Prescriptions (Antibiotic Control) ───

object VetPrescriptions : LongIdTable("vet_prescriptions") {
    val vetId = long("vet_id").references(Users.id)
    val drugId = long("drug_id").references(Drugs.id)
    val patientFarmName = varchar("patient_farm_name", 500).nullable()
    val animalSpecies = varchar("animal_species", 255).nullable()
    val dosage = varchar("dosage", 255).nullable()
    val quantity = integer("quantity").nullable()
    val prescriptionDate = varchar("prescription_date", 20)
    val dispensedAt = varchar("dispensed_at", 20).nullable()
    val createdAt = datetime("created_at")
}

// ─── Pharmacovigilance (Safety Reports) ───

object AdverseEvents : LongIdTable("adverse_events") {
    val reporterName = varchar("reporter_name", 500)
    val reporterOrg = varchar("reporter_org", 500).nullable()
    val phone = varchar("phone", 100).nullable()
    val drugName = varchar("drug_name", 500)
    val batchNumber = varchar("batch_number", 100).nullable()
    val dosageForm = varchar("dosage_form", 255).nullable()
    val description = text("description")
    val detectionDate = varchar("detection_date", 20)
    val measuresTaken = text("measures_taken").nullable()
    val status = varchar("status", 50).default("REGISTERED")
    val createdAt = datetime("created_at")
}

// ─── Destruction Acts (Order No. 16-07/443) ───

object DestructionActs : LongIdTable("destruction_acts") {
    val drugName = varchar("drug_name", 500)
    val batchNumber = varchar("batch_number", 100)
    val volume = double("volume")
    val grounds = varchar("grounds", 50)
    val denaturationMethod = varchar("denaturation_method", 255)
    val destructionMethod = varchar("destruction_method", 255)
    val destructionDate = varchar("destruction_date", 20)
    val isPrivateSector = bool("is_private_sector").default(true)
    val commissionMembers = text("commission_members").default("[]")
    val status = varchar("status", 50).default("COMPLETED")
    val createdAt = datetime("created_at")
}

object RegistrationWorkflows : LongIdTable("registration_workflows") {
    val applicationId = long("application_id").references(Applications.id).nullable()
    val pathway = varchar("pathway", 50)
    val currentStage = varchar("current_stage", 100)
    val state = varchar("state", 50).default("ACTIVE") // ACTIVE, PAUSED, COMPLETED, CANCELLED
    val slaWorkingDays = integer("sla_working_days")
    val elapsedWorkingDays = integer("elapsed_working_days").default(0)
    val pausedAt = datetime("paused_at").nullable()
    val dueDate = varchar("due_date", 20).nullable()
    val createdBy = long("created_by").references(Users.id).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object WorkflowStageHistory : LongIdTable("workflow_stage_history") {
    val workflowId = long("workflow_id").references(RegistrationWorkflows.id)
    val fromStage = varchar("from_stage", 100).nullable()
    val toStage = varchar("to_stage", 100)
    val changedBy = long("changed_by").references(Users.id).nullable()
    val note = text("note").nullable()
    val changedAt = datetime("changed_at")
}

object SecurityRoles : LongIdTable("security_roles") {
    val key = varchar("key", 100).uniqueIndex()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val isSystem = bool("is_system").default(false)
    val createdAt = datetime("created_at")
}

object SecurityPermissions : LongIdTable("security_permissions") {
    val key = varchar("key", 150).uniqueIndex()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val createdAt = datetime("created_at")
}

object SecurityRolePermissions : LongIdTable("security_role_permissions") {
    val roleId = long("role_id").references(SecurityRoles.id)
    val permissionId = long("permission_id").references(SecurityPermissions.id)
}

object UserRoleAssignments : LongIdTable("user_role_assignments") {
    val userId = long("user_id").references(Users.id)
    val roleId = long("role_id").references(SecurityRoles.id)
    val startsAt = datetime("starts_at").nullable()
    val endsAt = datetime("ends_at").nullable()
    val assignedBy = long("assigned_by").references(Users.id).nullable()
    val createdAt = datetime("created_at")
}

object IntegrationConnectors : LongIdTable("integration_connectors") {
    val systemKey = varchar("system_key", 100).uniqueIndex()
    val endpoint = varchar("endpoint", 500)
    val protocol = varchar("protocol", 50).default("REST")
    val isActive = bool("is_active").default(true)
    val retryPolicy = varchar("retry_policy", 100).default("exponential-3")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object IntegrationCalls : LongIdTable("integration_calls") {
    val connectorId = long("connector_id").references(IntegrationConnectors.id)
    val idempotencyKey = varchar("idempotency_key", 120)
    val requestPayload = text("request_payload").nullable()
    val responsePayload = text("response_payload").nullable()
    val status = varchar("status", 50).default("PENDING") // PENDING, SUCCESS, FAILED
    val attempts = integer("attempts").default(0)
    val errorMessage = text("error_message").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object OutboxEvents : LongIdTable("outbox_events") {
    val aggregateType = varchar("aggregate_type", 100)
    val aggregateId = varchar("aggregate_id", 100)
    val eventType = varchar("event_type", 100)
    val payload = text("payload")
    val status = varchar("status", 50).default("PENDING") // PENDING, SENT, FAILED, DEAD_LETTER
    val retries = integer("retries").default(0)
    val nextAttemptAt = datetime("next_attempt_at").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object InboxEvents : LongIdTable("inbox_events") {
    val sourceSystem = varchar("source_system", 100)
    val messageKey = varchar("message_key", 120).uniqueIndex()
    val eventType = varchar("event_type", 100)
    val payload = text("payload")
    val processed = bool("processed").default(false)
    val processedAt = datetime("processed_at").nullable()
    val createdAt = datetime("created_at")
}

object DeadLetters : LongIdTable("dead_letters") {
    val sourceType = varchar("source_type", 50) // OUTBOX, INBOX, INTEGRATION
    val sourceId = varchar("source_id", 100)
    val reason = text("reason")
    val payload = text("payload").nullable()
    val createdAt = datetime("created_at")
}

object DossierDocumentVersions : LongIdTable("dossier_document_versions") {
    val applicationId = long("application_id").references(Applications.id)
    val partNumber = integer("part_number")
    val fileName = varchar("file_name", 500)
    val fileHash = varchar("file_hash", 128)
    val storagePath = text("storage_path")
    val signatureStatus = varchar("signature_status", 50).default("PENDING") // PENDING, VALID, INVALID
    val uploadedBy = long("uploaded_by").references(Users.id).nullable()
    val uploadedAt = datetime("uploaded_at")
}

object TraceabilityEvents : LongIdTable("traceability_events") {
    val eventType = varchar("event_type", 100) // MOVEMENT, COLD_CHAIN_ALERT, INCIDENT, QR_SCAN
    val batchId = long("batch_id").references(Batches.id).nullable()
    val qrCode = varchar("qr_code", 255).nullable()
    val location = varchar("location", 500).nullable()
    val severity = varchar("severity", 30).default("INFO")
    val payload = text("payload").nullable()
    val occurredAt = datetime("occurred_at")
}

object ReportTemplates : LongIdTable("report_templates") {
    val key = varchar("key", 100).uniqueIndex()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val scheduleType = varchar("schedule_type", 30).default("ON_DEMAND") // ON_DEMAND, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    val querySpec = text("query_spec").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
}

object ReportRuns : LongIdTable("report_runs") {
    val templateId = long("template_id").references(ReportTemplates.id)
    val status = varchar("status", 30).default("PENDING") // PENDING, COMPLETED, FAILED
    val outputRef = text("output_ref").nullable()
    val requestedBy = long("requested_by").references(Users.id).nullable()
    val errorMessage = text("error_message").nullable()
    val startedAt = datetime("started_at")
    val finishedAt = datetime("finished_at").nullable()
}

object ReportSubmissions : LongIdTable("report_submissions") {
    val runId = long("run_id").references(ReportRuns.id)
    val authority = varchar("authority", 255)
    val status = varchar("status", 30).default("DRAFT") // DRAFT, SUBMITTED, ACCEPTED, REJECTED
    val submissionRef = varchar("submission_ref", 255).nullable()
    val submittedAt = datetime("submitted_at").nullable()
}
