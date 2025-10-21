package com.ra.antiapp // Make sure this package name matches your file's location

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    // Let's rename the test to be more descriptive
    @Test
    fun `test GET leaderboard endpoint`() = testApplication {
        val response = client.get("/leaderboard")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("application/json; charset=UTF-8", response.headers["Content-Type"])
        assert(response.bodyAsText().contains("Alice"))
    }
}