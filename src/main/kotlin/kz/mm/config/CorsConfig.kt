package kz.mm.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    val allowedHosts = environment.config
        .propertyOrNull("ktor.allowedHosts")
        ?.getString()
        ?.split(",")
        ?.toSet()
        ?: emptySet()

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        allowCredentials = true
        allowHeaders { true }

        allowedHosts.forEach { host ->
            allowHost(host, schemes = listOf("https", "http"))
        }
    }
}
