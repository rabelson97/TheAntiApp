package com.ra.antiapp.data

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import aws.sdk.kotlin.services.dynamodb.model.ScanRequest
import com.ra.antiapp.core.model.User
import kotlinx.coroutines.runBlocking

// The name of our table in the cloud.
private const val TABLE_NAME = "anti-app-users"

// This is the implementation of our repository that talks to DynamoDB.
// It takes a DynamoDbClient as a dependency.
class DynamoDbUserRepository(private val dynamoDbClient: DynamoDbClient) : UserRepository {

    // Note: runBlocking is used here for simplicity to fit the interface.
    // In a more advanced setup, you'd make the UserRepository functions suspendable.
    override suspend fun findUserById(userId: String): User? {
        val request = GetItemRequest {
            tableName = TABLE_NAME
            key = mapOf("userId" to AttributeValue.S(userId))
        }
        val response = dynamoDbClient.getItem(request)
        return response.item?.toUser()
    }

    override suspend fun save(user: User) {
        // Create a mutable map with a non-nullable value type.
        val userItem = mutableMapOf(
            "userId" to AttributeValue.S(user.userId),
            "displayName" to AttributeValue.S(user.displayName),
            "highestScore" to AttributeValue.N(user.highestScore.toString())
        )

        user.currentSessionStartTime?.let { startTime ->
            userItem["currentSessionStartTime"] = AttributeValue.N(startTime.toString())
        }

        val request = PutItemRequest {
            tableName = TABLE_NAME
            item = userItem
        }
        dynamoDbClient.putItem(request)
    }

    // This is a more advanced query. We can't sort the whole table,
    // so we'd need to create a Global Secondary Index (GSI) on 'highestScore' in DynamoDB.
    // For now, we'll implement it with a Scan, which is inefficient but works for small amounts of data.
    override suspend fun getTopUsers(limit: Int): List<User> {
        val request = ScanRequest {
            tableName = TABLE_NAME
            this.limit = limit * 2 // Scan is unordered, so we get more and sort in memory
        }
        val response = dynamoDbClient.scan(request)
        return response.items
            ?.map { it.toUser() }
            ?.sortedByDescending { it.highestScore }
            ?.take(limit)
            ?: emptyList()
    }

    // A helper function to convert a DynamoDB item map back into our User data class.
    private fun Map<String, AttributeValue>.toUser(): User {
        return User(
            userId = this["userId"]!!.asS(),
            displayName = this["displayName"]!!.asS(),
            highestScore = this["highestScore"]!!.asN().toLong(),
            currentSessionStartTime = this["currentSessionStartTime"]?.asN()?.toLong()
        )
    }
}