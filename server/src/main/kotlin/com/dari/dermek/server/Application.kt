package com.dari.dermek.server

import com.dari.dermek.server.db.DatabaseFactory
import com.dari.dermek.server.routes.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configurePlugins()
        configureRouting()
    }.start(wait = true)
}

fun Application.configurePlugins() {
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET is required. Set a strong secret in environment variables.")

    // JSON Serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // CORS — allow frontend clients from any origin during development
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        val allowedOrigins = (System.getenv("CORS_ALLOWED_ORIGINS")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: listOf("http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:3000"))
        allowedOrigins.forEach { origin ->
            allowHost(origin.removePrefix("http://").removePrefix("https://"), schemes = listOf(if (origin.startsWith("https://")) "https" else "http"))
        }
    }

    // Authentication — JWT setup
    install(Authentication) {
        jwt("auth-jwt") {
            val secret = jwtSecret
            val issuer = "gis-dermek-issuer"
            val audience = "gis-dermek-audience"
            realm = "Access to GIS Dari-dermek"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("login").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    // Call Logging
    install(CallLogging)

    // Error Handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(false, cause.message ?: "Internal Server Error")
            )
        }
    }

    // Initialize Database
    DatabaseFactory.init()
}

@Serializable
data class ErrorResponse(val success: Boolean, val error: String)

@Serializable
data class HealthResponse(val status: String, val service: String, val version: String)

@Serializable
data class PathwayInfoResponse(
    val key: String,
    val nameRu: String,
    val nameEn: String,
    val maxDaysStandard: Int,
    val maxDaysAnnex: Int,
    val sampleRequired: Boolean,
    val deadline: String? = null
)

@Serializable
data class PathwaysResponse(val success: Boolean, val data: List<PathwayInfoResponse>)

fun Application.configureRouting() {
    routing {
        // Health check
        get("/api/health") {
            call.respond(HealthResponse("ok", "GIS Дари-дермек API", "0.1.0"))
        }

        // API routes
        authRoutes()
        drugRoutes()
        applicationRoutes()
        batchRoutes()
        labRoutes()
        controlPurchaseRoutes()
        pharmacovigilanceRoutes()
        disposalRoutes()
        blueprintRoutes()
        participantRoutes()
        workflowRoutes()
        platformControlRoutes()

        // Registration pathways info (static reference data)
        get("/api/pathways") {
            val pathways = listOf(
                PathwayInfoResponse("COMPLIANCE", "Приведение в соответствие", "Bringing into Compliance", 90, 70, true, "2030-12-31"),
                PathwayInfoResponse("STANDARD", "Стандартная регистрация", "Standard Registration", 100, 95, true),
                PathwayInfoResponse("SIMPLIFIED", "Упрощённая регистрация (генерик)", "Simplified Registration (Generic)", 45, 35, true),
                PathwayInfoResponse("CONFIRMATION", "Подтверждение регистрации", "Registration Confirmation", 40, 30, false),
                PathwayInfoResponse("AMENDMENT_WITH_TESTING", "Изменения (с экспертизой образцов)", "Amendment (with sample testing)", 90, 80, true),
                PathwayInfoResponse("AMENDMENT_WITHOUT_TESTING", "Изменения (без экспертизы)", "Amendment (no sample testing)", 40, 30, false),
                PathwayInfoResponse("RECOGNITION", "Процедура признания", "Mutual Recognition", 45, 45, false)
            )
            call.respond(PathwaysResponse(true, pathways))
        }

        // Regulations list endpoint
        get("/api/regulations") {
            call.respond(emptyList<String>())
        }
    }
}
