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
            // The 'authenticate' block guarantees that the principal is not null.
            // If the token was invalid, Ktor would have already sent a 401 Unauthorized response.
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.subject

            if (service.startSession(userId)) {
                call.respond(HttpStatusCode.OK, "Session started for $userId.")
            } else {
                // This might happen if a user has a valid token but isn't in our DB yet.
                // We'll handle user creation later.
                call.respond(HttpStatusCode.NotFound, "User not found.")
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