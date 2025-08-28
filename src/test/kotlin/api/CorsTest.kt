package api

import authenticateAsTestUser
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kz.mm.module
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CorsTest {


    @Test
    fun `should allow CORS request from allowed origin`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.allowedHosts" to "www.example.com"
            )
        }

        application {
            module() // installs CORS + routing
        }

        val response = client.options("/") {
            header(HttpHeaders.Origin, "https://www.example.com")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
            authenticateAsTestUser()
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("https://www.example.com", response.headers[HttpHeaders.AccessControlAllowOrigin])
        assertEquals("true", response.headers[HttpHeaders.AccessControlAllowCredentials])
    }


    @Test
    fun `should deny CORS request from disallowed origin`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.allowedHosts" to "www.example.com"
            )
        }

        application {
            module()
        }

        val response = client.options("/") {
            header(HttpHeaders.Origin, "https://evil.com")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
            authenticateAsTestUser()
        }

        // ✅ Ktor CORS plugin returns 403 if origin is not allowed
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }


}
