package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import tables.UserStatisticsTable

fun Route.statisticsRoutes() {
    authenticate {
        get("/statistics") { respondStatistics(call) }
        post("/statistics/update") { updateStatistics(call) }

        get("/statistics/learned-words") { respondLearnedWords(call) }
        get("/statistics/solved-tests") { respondSolvedTests(call) }
        get("/statistics/mistakes") { respondMistakes(call) }

        post("/statistics/learned-words/increment") { incrementLearnedWords(call) }
        post("/statistics/solved-tests/increment") { incrementSolvedTests(call) }
        post("/statistics/mistakes/increment") { incrementMistakes(call) }
    }
}

private suspend fun respondStatistics(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    val statistics = UserStatisticsTable.getStatistics(userId)
    if (statistics == null) {
        call.respond(HttpStatusCode.NotFound, "Statistics not found")
    } else {
        call.respond(statistics)
    }
}

private suspend fun updateStatistics(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return

    val learnedWords = call.request.queryParameters["learnedWords"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val solvedTests = call.request.queryParameters["solvedTests"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val mistakes = call.request.queryParameters["mistakes"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0

    UserStatisticsTable.updateStatistics(userId, learnedWords, solvedTests, mistakes)
    call.respond(HttpStatusCode.OK, "Statistics updated")
}

private suspend fun respondLearnedWords(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    call.respond(UserStatisticsTable.getLearnedWords(userId))
}

private suspend fun respondSolvedTests(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    call.respond(UserStatisticsTable.getSolvedTests(userId))
}

private suspend fun respondMistakes(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    call.respond(UserStatisticsTable.getMistakes(userId))
}

private suspend fun incrementLearnedWords(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
    UserStatisticsTable.incrementLearnedWords(userId, count)
    call.respond(HttpStatusCode.OK, "Learned words incremented")
}

private suspend fun incrementSolvedTests(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
    UserStatisticsTable.incrementSolvedTests(userId, count)
    call.respond(HttpStatusCode.OK, "Solved tests incremented")
}

private suspend fun incrementMistakes(call: ApplicationCall) {
    val userId = getAuthenticatedUserId(call) ?: return
    val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 1
    UserStatisticsTable.incrementMistakes(userId, count)
    call.respond(HttpStatusCode.OK, "Mistakes incremented")
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