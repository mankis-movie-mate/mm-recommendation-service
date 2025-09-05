import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kz.mm.module
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World v2!", response.bodyAsText())
    }
}