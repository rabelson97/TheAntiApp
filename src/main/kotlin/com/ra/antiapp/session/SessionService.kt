package com.ra.antiapp.session

import com.ra.antiapp.data.UserRepository
import kotlin.math.max

class SessionService(private val userRepository: UserRepository) {

    fun startSession(userId: String): Boolean {
        val user = userRepository.findUserById(userId) ?: return false
        user.currentSessionStartTime = System.currentTimeMillis()
        userRepository.save(user)
        return true
    }

    fun endSession(userId: String): Long? {
        val user = userRepository.findUserById(userId) ?: return null
        val startTime = user.currentSessionStartTime ?: return null // No active session

        val score = (System.currentTimeMillis() - startTime) / 1000 // Score in seconds
        user.highestScore = max(user.highestScore, score)
        user.currentSessionStartTime = null // End the session
        userRepository.save(user)

        return score
    }
}