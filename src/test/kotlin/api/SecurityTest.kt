package api

import authenticateAsTestUser
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kz.mm.module
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SecurityTest {
    @Test
    fun testRootEndpoint_authenticatedUser_shouldSucceed() = testApplication {
        application {
            module()
        }

        val response = client.get("/") {
            authenticateAsTestUser() // inject test user headers
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Recommendation Service!", response.bodyAsText())
    }

    @Test
    fun testRootEndpoint_withoutAuthHeaders_shouldBeUnauthorized() = testApplication {
        application {
            module()
        }

        val response = client.get("/") // no auth headers

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}