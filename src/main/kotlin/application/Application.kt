package application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import routes.authRoutes
import routes.wordRoutes
import utils.TokenManager.verifier
import utils.myModule
import routes.statisticsRoutes
import routes.userRoutes


fun Application.module() {
    DatabaseFactory.init()
    DatabaseFactory.createTables()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = myModule
        })
    }

    install(Authentication) {
        jwt {
            realm = "german_words_server"
            verifier(verifier)  // Проверка токена
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
    install(Routing) {
        get("/") {
            call.respondText("Сервер работает!", ContentType.Text.Plain)
        }
        authRoutes()
        wordRoutes()
        statisticsRoutes()
        userRoutes()
    }

}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}