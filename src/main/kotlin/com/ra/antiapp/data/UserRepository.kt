package com.ra.antiapp.data

import com.ra.antiapp.core.model.User

// Using an interface is a best practice for testability and swapping implementations later.
interface UserRepository {
    fun findUserById(userId: String): User?
    fun getTopUsers(limit: Int): List<User>
    fun save(user: User)
}

// This is the concrete implementation of our repository.
class InMemoryUserRepository : UserRepository {
    // Our fake database now lives inside the repository.
    private val userDatabase = mutableMapOf(
        "user123" to User(userId = "user123", displayName = "Alice", highestScore = 5000),
        "user456" to User(userId = "user456", displayName = "Bob", highestScore = 9000),
        "user789" to User(userId = "user789", displayName = "Charlie", highestScore = 2500)
    )

    override fun findUserById(userId: String): User? {
        return userDatabase[userId]
    }

    override fun getTopUsers(limit: Int): List<User> {
        return userDatabase.values
            .sortedByDescending { it.highestScore }
            .take(limit)
    }

    override fun save(user: User) {
        // In a real database, this would be an UPDATE or INSERT operation.
        userDatabase[user.userId] = user
    }
}