import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.openapi.OpenAPIYamlGenerator

fun Application.configureOpenApi() {
    routing {
        openAPI(path = "/docs") { codegen = OpenAPIYamlGenerator() }
        swaggerUI(path = "/docs/swagger") {}
    }
}


