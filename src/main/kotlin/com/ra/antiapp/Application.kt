package com.ra.antiapp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import com.ra.antiapp.data.DynamoDbUserRepository
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

// This is the REAL DI module for your production application.
val appModule = module {
    single { DynamoDbClient { region = "us-west-2" } } // Use your actual region
    single<UserRepository> { DynamoDbUserRepository(get()) }
    single { LeaderboardService(get()) }
    single { SessionService(get()) }
}

fun Application.module() {
    // It installs Koin with the REAL database module...
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    configurePlugins()
}

fun Application.configurePlugins() {
    install(ContentNegotiation) {
        json()
    }
    configureSecurity()
    routing {
        leaderboardRoutes()
        sessionRoutes()
    }
}