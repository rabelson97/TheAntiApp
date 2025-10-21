package com.ra.antiapp.leaderboard

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

// This is an "extension function" that adds our routes to the Ktor application.
// Think of it as a self-contained router config.
fun Route.leaderboardRoutes() {
    // Koin injects an instance of LeaderboardService for us.
    val service: LeaderboardService by inject()

    get("/leaderboard") {
        val leaderboard = service.getLeaderboard()
        call.respond(leaderboard)
    }
}