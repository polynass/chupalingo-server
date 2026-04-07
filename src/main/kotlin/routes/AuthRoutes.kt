package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import models.User
import tables.Users
import utils.PasswordHasher
import utils.TokenManager.generateToken

fun Route.authRoutes() {
    post("/register") {
        val credentials = call.receive<User>()
        val hashedPassword = PasswordHasher.hash(credentials.password)

        if (Users.getUser(credentials.username) != null) {
            call.respond(HttpStatusCode.Conflict)
        } else {
            Users.insertUser(credentials.username, hashedPassword)
            call.respond(HttpStatusCode.Created)
        }
    }

    post("/login") {
        val credentials = call.receive<User>()
        val user = Users.getUser(credentials.username)

        if (user != null && PasswordHasher.verify(credentials.password, user[Users.passwordHash])) {
            val token = generateToken(credentials.username)
            Users.updateToken(credentials.username, token)
            call.respond(HttpStatusCode.OK, mapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    authenticate {
        get("/auth") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username")?.asString()

            if (username != null) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
