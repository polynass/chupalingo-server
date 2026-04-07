package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import models.TestResponse
import models.Word
import tables.Words
import java.sql.SQLException

fun Route.wordRoutes() {
    get("/words") { call.respond(Words.getAllWords()) }

    post("/words/add") {
        try {
            val newWord = call.receive<Word>()
            Words.insertWord(newWord)
            call.respond(HttpStatusCode.Created, "Word added successfully")
        } catch (e: SQLException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid data: ${e.localizedMessage}")
        }
    }

    put("/words/update/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        val updatedWord = call.receive<Word>()

        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            return@put
        }

        if (Words.updateWord(id, updatedWord.copy(id = id))) {
            call.respond(HttpStatusCode.OK, "Word updated successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "Word not found")
        }
    }

    delete("/words/delete/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) { call.respond(HttpStatusCode.BadRequest, "Invalid ID"); return@delete }

        if (Words.deleteWord(id)) {
            call.respond(HttpStatusCode.OK, "Word deleted successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "Word not found")
        }
    }

    get("/test") {
        val word = Words.getRandomWord() ?: run {
            call.respond(HttpStatusCode.NotFound, "No words available")
            return@get
        }
        val incorrectTranslations = Words.getRandomTranslations(word.translation)
        val options = (incorrectTranslations + word.translation).shuffled()
        call.respond(TestResponse(word.word, options, word.translation))
    }

    get("/random-word") {
        Words.getRandomWord()?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound, "No words available")
    }
}
