package com.ra.antiapp.data

import com.ra.antiapp.core.model.User

// Using an interface is a best practice for testability and swapping implementations later.
interface UserRepository {
    suspend fun findUserById(userId: String): User?
    suspend fun getTopUsers(limit: Int): List<User>
    suspend fun save(user: User)
}