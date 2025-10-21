package com.ra.antiapp

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ra.antiapp.data.InMemoryUserRepository
import com.ra.antiapp.data.UserRepository
import com.ra.antiapp.leaderboard.LeaderboardService
import com.ra.antiapp.leaderboard.leaderboardRoutes
import com.ra.antiapp.plugins.configureSecurity
import com.ra.antiapp.session.SessionService
import com.ra.antiapp.session.sessionRoutes
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private val testSecret = "test-secret"
    private val testAlgorithm = Algorithm.HMAC256(testSecret)
    private val testAudience = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com"
    private val testIssuer = "https://accounts.google.com"

    private fun generateTestToken(userId: String, name: String): String {
        return JWT.create()
            .withAudience(testAudience)
            .withIssuer(testIssuer)
            .withSubject(userId)
            .withClaim("name", name)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600_000))
            .sign(testAlgorithm)
    }

    @Test
    fun `test GET leaderboard endpoint is public`() = testApplication {
        environment { config = MapApplicationConfig() }
        application {
            // VERBOSE SETUP: All plugins are installed explicitly here.
            this.install(Koin) {
                modules(module {
                    single<UserRepository> { InMemoryUserRepository() }
                    single { LeaderboardService(get()) }
                    single { SessionService(get()) }
                })
            }
            this.install(Authentication) {
                // A dummy authenticator is needed because sessionRoutes requires it.
                jwt("google-jwt") { verifier(JWT.require(Algorithm.HMAC256("dummy-secret")).build()) }
            }
            this.install(ContentNegotiation) { json() }
            this.routing {
                leaderboardRoutes()
                sessionRoutes()
            }
        }

        val response = client.get("/leaderboard")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `test POST session-start fails without any token`() = testApplication {
        environment { config = MapApplicationConfig() }
        application {
            // VERBOSE SETUP: All plugins are installed explicitly here.
            this.install(Koin) {
                modules(module {
                    single<UserRepository> { InMemoryUserRepository() }
                    single { LeaderboardService(get()) }
                    single { SessionService(get()) }
                })
            }
            // Use the REAL security configuration from your main source code
            this.configureSecurity()
            this.install(ContentNegotiation) { json() }
            this.routing {
                leaderboardRoutes()
                sessionRoutes()
            }
        }

        val response = client.post("/session/start")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test POST session-start with new user succeeds`() = testApplication {
        val inMemoryRepo = InMemoryUserRepository()

        environment { config = MapApplicationConfig() }
        application {
            // VERBOSE SETUP: All plugins are installed explicitly here.
            this.install(Koin) {
                modules(module {
                    single<UserRepository> { inMemoryRepo }
                    single { LeaderboardService(get()) }
                    single { SessionService(get()) }
                })
            }
            this.install(Authentication) {
                jwt("google-jwt") {
                    realm = "Test Realm"
                    verifier(JWT.require(testAlgorithm).withAudience(testAudience).withIssuer(testIssuer).build())
                    validate { credential -> JWTPrincipal(credential.payload) }
                }
            }
            this.install(ContentNegotiation) { json() }
            this.routing {
                leaderboardRoutes()
                sessionRoutes()
            }
        }

        val newUserToken = generateTestToken(userId = "new-user-999", name = "Test User")
        val response = client.post("/session/start") {
            header(HttpHeaders.Authorization, "Bearer $newUserToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val createdUser = inMemoryRepo.findUserById("new-user-999")
        assertEquals("Test User", createdUser?.displayName)
    }
}