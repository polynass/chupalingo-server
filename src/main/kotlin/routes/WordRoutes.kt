package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.TestResponse
import models.Word
import tables.Words


fun Route.wordRoutes() {
    get("/words") {
        val words = Words.getAllWords()
        call.respond(words)
    }

    post("/words/add") {
        try {
            val newWord = call.receive<Word>()
            Words.insertWord(newWord)
            call.respond(HttpStatusCode.Created, "Word added successfully")
        } catch (e: Exception) {
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

        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            return@delete
        }

        if (Words.deleteWord(id)) {
            call.respond(HttpStatusCode.OK, "Word deleted successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "Word not found")
        }
    }



    get("/test") {
        val word = Words.getRandomWord()
        if (word == null) {
            call.respond(HttpStatusCode.NotFound, "No words available")
            return@get
        }

        val incorrectTranslations = Words.getRandomTranslations(word.translation)
        val options = (incorrectTranslations + word.translation).shuffled()

        call.respond(TestResponse(word.word, options, word.translation))
    }

    get("/random-word") {
        val word = Words.getRandomWord()
        if (word == null) {
            call.respond(HttpStatusCode.NotFound, "No words available")
        } else {
            call.respond(word)
        }
    }


}
