package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import tables.Users
import utils.PasswordHasher

fun Route.userRoutes() {
    authenticate {
        route("/user") {
            post("/update-password") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    return@post
                }

                val request = call.receive<PasswordUpdateRequest>()
                val user = Users.getUser(username) ?: run {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                if (!PasswordHasher.verify(request.currentPassword, user[Users.passwordHash])) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UpdateResponse(false, "Current password is incorrect")
                    )
                    return@post
                }

                val newHash = PasswordHasher.hash(request.newPassword)
                val success = Users.updatePassword(username, newHash)

                call.respond(
                    if (success) {
                        UpdateResponse(true, "Password updated successfully")
                    } else {
                        UpdateResponse(false, "Failed to update password")
                    }
                )
            }

            post("/update-username") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    return@post
                }

                val request = call.receive<UsernameUpdateRequest>()
                val user = Users.getUser(username) ?: run {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                if (!PasswordHasher.verify(request.password, user[Users.passwordHash])) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UpdateResponse(false, "Password is incorrect")
                    )
                    return@post
                }

                val success = Users.updateUsername(username, request.newUsername)

                call.respond(
                    if (success) {
                        UpdateResponse(true, "Username updated successfully")
                    } else {
                        UpdateResponse(false, "Username already taken or update failed")
                    }
                )
            }
        }
    }
}