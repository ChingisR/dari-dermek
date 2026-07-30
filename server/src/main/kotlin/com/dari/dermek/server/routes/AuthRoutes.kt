package com.dari.dermek.server.routes

import com.dari.dermek.server.models.*
import com.dari.dermek.server.db.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Date

fun Route.authRoutes() {
    route("/api/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            
            // Validate and map the code to user profile
            val userDto = if (DatabaseFactory.isAvailable()) {
                transaction {
                    val userRow = Users.selectAll().where { Users.login eq request.egovCode }.singleOrNull()
                    if (userRow != null) {
                        userRow.toUserDto()
                    } else {
                        // Try mapping by role-based demo code
                        val mappedRole = when (request.egovCode) {
                            "code_applicant" -> "APPLICANT"
                            "code_kvkn" -> "COMMITTEE_STAFF"
                            "code_expert" -> "NRCV_EXPERT"
                            "code_lab" -> "LAB_ANALYST"
                            "code_border" -> "BORDER_INSPECTOR"
                            "code_warehouse" -> "WAREHOUSE_CLERK"
                            "code_farmer" -> "FARMER_VET"
                            "code_admin" -> "ADMIN"
                            else -> "APPLICANT"
                        }
                        val fallbackLogin = "${mappedRole.lowercase()}@egov.kz"
                        val existingFallback = Users.selectAll().where { Users.login eq fallbackLogin }.singleOrNull()
                        if (existingFallback != null) {
                            existingFallback.toUserDto()
                        } else {
                            // Create user on-the-fly
                            val newId = Users.insertAndGetId {
                                it[login] = fallbackLogin
                                it[fullName] = when (mappedRole) {
                                    "APPLICANT" -> "ТОО «ВетФарм Казахстан»"
                                    "COMMITTEE_STAFF" -> "Серіков Б.А."
                                    "NRCV_EXPERT" -> "Нурбекова Ж.К."
                                    "LAB_ANALYST" -> "Алиева М.С."
                                    "BORDER_INSPECTOR" -> "Касымов Д.Т."
                                    "WAREHOUSE_CLERK" -> "Жумабаев А.К."
                                    "FARMER_VET" -> "Ермеков К.Н."
                                    else -> "Администратор ГИС"
                                }
                                it[role] = mappedRole
                                it[organization] = when (mappedRole) {
                                    "APPLICANT" -> "ТОО «ВетФарм Казахстан»"
                                    "COMMITTEE_STAFF" -> "КВКН МСХ РК"
                                    "NRCV_EXPERT" -> "НРЦВ"
                                    "LAB_ANALYST" -> "НРЦВ — Лаборатория микробиологии"
                                    "BORDER_INSPECTOR" -> "Таможенная служба — КПП «Хоргос»"
                                    "WAREHOUSE_CLERK" -> "Франко-склад «ВетМедСнаб»"
                                    "FARMER_VET" -> "КХ «Жайлау»"
                                    else -> "ГИС Дари-дермек"
                                }
                            }
                            UserDto(
                                id = newId.value,
                                login = fallbackLogin,
                                fullName = fallbackLogin,
                                role = UserRole.valueOf(mappedRole),
                                organization = mappedRole
                            )
                        }
                    }
                }
            } else {
                // Fallback for API-only mode when database is offline
                when (request.egovCode) {
                    "code_applicant" -> UserDto(1, "applicant@egov.kz", "ТОО «ВетФарм Казахстан»", UserRole.APPLICANT, "ТОО «ВетФарм Казахстан»")
                    "code_kvkn" -> UserDto(2, "kvkn@gov.kz", "Серіков Б.А.", UserRole.COMMITTEE_STAFF, "КВКН МСХ РК")
                    "code_expert" -> UserDto(3, "expert@nrcv.kz", "Нурбекова Ж.К.", UserRole.NRCV_EXPERT, "НРЦВ")
                    "code_lab" -> UserDto(4, "lab@nrcv.kz", "Алиева М.С.", UserRole.LAB_ANALYST, "НРЦВ — Лаборатория микробиологии")
                    "code_border" -> UserDto(5, "border@customs.kz", "Касымов Д.Т.", UserRole.BORDER_INSPECTOR, "Таможенная служба — КПП «Хоргос»")
                    "code_warehouse" -> UserDto(6, "warehouse@vetpharm.kz", "Жумабаев А.К.", UserRole.WAREHOUSE_CLERK, "Франко-склад «ВетМедСнаб»")
                    "code_farmer" -> UserDto(7, "farmer@mail.kz", "Ермеков К.Н.", UserRole.FARMER_VET, "КХ «Жайлау»")
                    "code_admin" -> UserDto(8, "admin@gis.kz", "Администратор", UserRole.ADMIN, "ГИС Дари-дермек")
                    else -> UserDto(1, "applicant@egov.kz", "ТОО «ВетФарм Казахстан»", UserRole.APPLICANT, "ТОО «ВетФарм Казахстан»")
                }
            }

            // Generate JWT
            val secret = System.getenv("JWT_SECRET")
                ?: return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<AuthResponse>(success = false, error = "JWT secret is not configured")
                )
            val token = JWT.create()
                .withAudience("gis-dermek-audience")
                .withIssuer("gis-dermek-issuer")
                .withClaim("login", userDto.login)
                .withClaim("role", userDto.role.name)
                .withClaim("userId", userDto.id)
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .sign(Algorithm.HMAC256(secret))

            call.respond(AuthResponse(token, userDto))
        }
    }
}
