package com.dari.dermek.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.flywaydb.core.Flyway

object DatabaseFactory {

    private var isConnected = false

    fun isAvailable(): Boolean = isConnected

    fun init(
        jdbcUrl: String = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/dari_dermek",
        dbUser: String = System.getenv("DB_USER") ?: "dari",
        dbPassword: String = System.getenv("DB_PASSWORD") ?: "dari_password"
    ) {
        try {
            val dataSource = hikari(jdbcUrl, dbUser, dbPassword)

            // Run Flyway migrations (if migration files exist)
            try {
                val flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load()
                flyway.migrate()
                println("✅ Flyway migrations applied successfully")
            } catch (e: Exception) {
                println("⚠️ Flyway migration skipped: ${e.message}")
            }

            Database.connect(dataSource)

            // Create tables if they don't exist (development convenience)
            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    Users,
                    Participants,
                    Drugs,
                    Manufacturers,
                    Applications,
                    ApplicationStatusHistory,
                    DossierParts,
                    Batches,
                    QrVials,
                    LabProtocols,
                    ControlPurchases,
                    ImportDeclarations,
                    VetPrescriptions,
                    AdverseEvents,
                    DestructionActs,
                    RegistrationWorkflows,
                    WorkflowStageHistory,
                    SecurityRoles,
                    SecurityPermissions,
                    SecurityRolePermissions,
                    UserRoleAssignments,
                    IntegrationConnectors,
                    IntegrationCalls,
                    OutboxEvents,
                    InboxEvents,
                    DeadLetters,
                    DossierDocumentVersions,
                    TraceabilityEvents,
                    ReportTemplates,
                    ReportRuns,
                    ReportSubmissions
                )
            }

            isConnected = true
            println("✅ Database connected: $jdbcUrl")
        } catch (e: Exception) {
            isConnected = false
            println("⚠️ Database unavailable (server running in API-only mode): ${e.message}")
            println("   Start PostgreSQL with: docker compose up postgres -d")
        }
    }

    private fun hikari(jdbcUrl: String, dbUser: String, dbPassword: String): HikariDataSource {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = dbUser
            this.password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 60_000
            connectionTimeout = 30_000
            maxLifetime = 600_000
            isAutoCommit = false
            validate()
        }
        return HikariDataSource(config)
    }
}
