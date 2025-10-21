package com.ra.antiapp.session

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.sessionRoutes() {
    val service: SessionService by inject()

    // We'll hardcode the userId for now. Later, this will come from the auth token.
    val hardcodedUserId = "user123"

    post("/session/start") {
        if (service.startSession(hardcodedUserId)) {
            call.respond(HttpStatusCode.OK, "Session started for ${hardcodedUserId}.")
        } else {
            call.respond(HttpStatusCode.NotFound, "User not found.")
        }
    }

    post("/session/end") {
        val score = service.endSession(hardcodedUserId)
        if (score != null) {
            call.respond(HttpStatusCode.OK, "Session ended. Your score was $score.")
        } else {
            call.respond(HttpStatusCode.BadRequest, "No active session found.")
        }
    }
}