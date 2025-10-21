package com.ra.antiapp

import com.ra.antiapp.data.InMemoryUserRepository
import com.ra.antiapp.data.UserRepository
import com.ra.antiapp.leaderboard.LeaderboardService
import com.ra.antiapp.leaderboard.leaderboardRoutes
import com.ra.antiapp.plugins.configureSecurity
import com.ra.antiapp.session.SessionService
import com.ra.antiapp.session.sessionRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// This is our Koin DI module. It's like a Dagger/Hilt module.
// It tells Koin how to create instances of our classes.
val appModule = module {
    single<UserRepository> { InMemoryUserRepository() } // When someone asks for a UserRepository, give them a single instance of InMemoryUserRepository.
    single { LeaderboardService(get()) } // Koin will automatically provide the UserRepository dependency.
    single { SessionService(get()) }
}

fun Application.module() {
    // --- CONFIGURE PLUGINS ---
    install(ContentNegotiation) {
        json()
    }

    // Install the Koin plugin
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    configureSecurity()

    // --- CONFIGURE ROUTING ---
    routing {
        leaderboardRoutes()
        sessionRoutes()
    }
}