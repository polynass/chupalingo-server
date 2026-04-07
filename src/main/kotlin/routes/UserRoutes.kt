package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import models.PasswordUpdateRequest
import models.UpdateResponse
import models.UsernameUpdateRequest
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
