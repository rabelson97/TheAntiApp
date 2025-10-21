package com.ra.antiapp.session

import com.ra.antiapp.core.model.User
import com.ra.antiapp.data.UserRepository
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlin.math.max

class SessionService(private val userRepository: UserRepository) {

    suspend fun startSession(principal: JWTPrincipal): Boolean {
        val userId = principal.payload.subject ?: return false // A token without a subject is invalid
        val user = userRepository.findUserById(userId)

        if (user != null) {
            // --- EXISTING USER LOGIC (Unchanged) ---
            user.currentSessionStartTime = System.currentTimeMillis()
            userRepository.save(user)
        } else {
            // --- NEW USER LOGIC ---
            // The user doesn't exist. Let's create them!
            val displayName = principal.payload.getClaim("name")?.asString() ?: "New User"
            val newUser = User(
                userId = userId,
                displayName = displayName,
                currentSessionStartTime = System.currentTimeMillis(), // Start their first session immediately
                highestScore = 0
            )
            userRepository.save(newUser)
        }
        return true
    }

    suspend fun endSession(userId: String): Long? {
        val user = userRepository.findUserById(userId) ?: return null
        val startTime = user.currentSessionStartTime ?: return null // No active session

        val score = (System.currentTimeMillis() - startTime) / 1000 // Score in seconds
        user.highestScore = max(user.highestScore, score)
        user.currentSessionStartTime = null // End the session
        userRepository.save(user)

        return score
    }
}