package application

import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import routes.authRoutes
import routes.statisticsRoutes
import routes.userRoutes
import routes.wordRoutes
import utils.TokenManager.verifier
import utils.myModule

fun Application.module() {
    DatabaseFactory.init()
    DatabaseFactory.createTables()

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                serializersModule = myModule
            }
        )
    }

    install(Authentication) {
        jwt {
            realm = "german_words_server"
            verifier(verifier)
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val expiresAt = credential.payload.expiresAt

                if (username != null && expiresAt != null && expiresAt.after(java.util.Date())) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
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
