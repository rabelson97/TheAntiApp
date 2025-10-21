package com.ra.antiapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: String,
    val displayName: String,
    var currentSessionStartTime: Long? = null,
    var highestScore: Long = 0
)