package com.ra.antiapp.session

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.* // <-- ADD THIS IMPORT
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.sessionRoutes() {
    val service: SessionService by inject()

    // This block tells Ktor that any route inside it requires
    // a valid token validated by our "google-jwt" configuration.
    authenticate("google-jwt") {

        post("/session/start") {
            val principal = call.principal<JWTPrincipal>()!!
            // Pass the entire principal to the service
            if (service.startSession(principal)) {
                call.respond(HttpStatusCode.OK, "Session started for ${principal.payload.subject}.")
            } else {
                // This now represents a more serious, unexpected error
                call.respond(HttpStatusCode.InternalServerError, "Could not start session.")
            }
        }

        post("/session/end") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.subject

            val score = service.endSession(userId)
            if (score != null) {
                call.respond(HttpStatusCode.OK, "Session ended. Your score was $score.")
            } else {
                call.respond(HttpStatusCode.BadRequest, "No active session found for $userId.")
            }
        }
    }
}