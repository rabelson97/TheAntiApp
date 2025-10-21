package com.ra.antiapp.leaderboard

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val displayName: String,
    val score: Long
)