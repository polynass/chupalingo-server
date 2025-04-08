package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tables.UserStatisticsTable

fun Route.statisticsRoutes() {
    authenticate {
        get("/statistics") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("username")?.asString()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                return@get
            }

            val statistics = UserStatisticsTable.getStatistics(userId)
            if (statistics == null) {
                call.respond(HttpStatusCode.NotFound, "Statistics not found")
            } else {
                call.respond(statistics)
            }
        }

        post("/statistics/update") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("username")?.asString()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                return@post
            }

            val learnedWords = call.request.queryParameters["learnedWords"]?.toIntOrNull() ?: 0
            val solvedTests = call.request.queryParameters["solvedTests"]?.toIntOrNull() ?: 0
            val mistakes = call.request.queryParameters["mistakes"]?.toIntOrNull() ?: 0

            UserStatisticsTable.updateStatistics(userId, learnedWords, solvedTests, mistakes)
            call.respond(HttpStatusCode.OK, "Statistics updated")
        }

        get("/statistics/learned-words") {
            val userId = getAuthenticatedUserId(call) ?: return@get
            call.respond(UserStatisticsTable.getLearnedWords(userId))
        }

        get("/statistics/solved-tests") {
            val userId = getAuthenticatedUserId(call) ?: return@get
            call.respond(UserStatisticsTable.getSolvedTests(userId))
        }

        get("/statistics/mistakes") {
            val userId = getAuthenticatedUserId(call) ?: return@get
            call.respond(UserStatisticsTable.getMistakes(userId))
        }

        post("/statistics/learned-words/increment") {
            val userId = getAuthenticatedUserId(call) ?: return@post
            val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
            UserStatisticsTable.incrementLearnedWords(userId, count)
            call.respond(HttpStatusCode.OK, "Learned words incremented")
        }

        post("/statistics/solved-tests/increment") {
            val userId = getAuthenticatedUserId(call) ?: return@post
            val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
            UserStatisticsTable.incrementSolvedTests(userId, count)
            call.respond(HttpStatusCode.OK, "Solved tests incremented")
        }

        post("/statistics/mistakes/increment") {
            val userId = getAuthenticatedUserId(call) ?: return@post
            val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
            UserStatisticsTable.incrementMistakes(userId, count)
            call.respond(HttpStatusCode.OK, "Mistakes incremented")
        }
    }

}

private suspend fun getAuthenticatedUserId(call: ApplicationCall): String? {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("username")?.asString()
    if (userId == null) {
        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
        return null
    }
    return userId
}