package com.ra.antiapp

import com.ra.antiapp.data.InMemoryUserRepository
import com.ra.antiapp.data.UserRepository
import com.ra.antiapp.leaderboard.LeaderboardService
import com.ra.antiapp.session.SessionService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.install
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.Test
import kotlin.test.assertEquals

// The DI module for our tests. It provides the fake in-memory repository.
val testModule = module {
    single<UserRepository> { InMemoryUserRepository() }
    single { LeaderboardService(get()) }
    single { SessionService(get()) }
}

class ApplicationTest {

    @Test
    fun `test GET leaderboard endpoint`() = testApplication {
        // This block explicitly configures the test environment.
        environment {
            // We tell the test runner to use an empty configuration,
            // completely ignoring any .conf files. This is the key.
            config = MapApplicationConfig()
        }

        // This block configures the application itself.
        application {
            // Now we have a truly blank slate and can set up everything manually.
            // 1. Install Koin with our TEST module.
            this.install(Koin) {
                modules(testModule)
            }
            // 2. Install all the other application plugins.
            configurePlugins()
        }

        // The test now runs against a perfectly configured, isolated server.
        val response = client.get("/leaderboard")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers["Content-Type"])
        assert(response.bodyAsText().contains("Alice"))
    }

    @Test
    fun `test POST session-start fails without token`() = testApplication {
        // Do the same explicit setup for this test.
        environment {
            config = MapApplicationConfig()
        }
        application {
            install(Koin) {
                modules(testModule)
            }
            configurePlugins()
        }

        val response = client.post("/session/start")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}