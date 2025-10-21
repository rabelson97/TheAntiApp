package com.ra.antiapp.leaderboard

import com.ra.antiapp.data.UserRepository

// This is like a UseCase or ViewModel. It contains business logic.
// It depends on the UserRepository to get its data.
class LeaderboardService(private val userRepository: UserRepository) {

    suspend fun getLeaderboard(limit: Int = 20): List<LeaderboardEntry> {
        val topUsers = userRepository.getTopUsers(limit)

        // The "business logic" here is transforming the raw User data
        // into a ranked LeaderboardEntry.
        return topUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                rank = index + 1,
                displayName = user.displayName,
                score = user.highestScore
            )
        }
    }
}