package kz.mm.rest

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kz.mm.config.ApiPaths
import kz.mm.config.authenticatedUser
import org.slf4j.LoggerFactory

class InternalAPI(

) {
    // Logger instance inside the class
    private val log = LoggerFactory.getLogger(InternalAPI::class.java)

    // The main route extension
    fun Route.internalApiRoutes(apiPaths: ApiPaths) {

        /**
         * Health check endpoint.
         */
        get(apiPaths.health) {
            val accepts = call.request.acceptItems()
            log.info("Health check requested, accepts: $accepts")
            call.respondText("OK", ContentType.Text.Plain)
        }

        /**
         * Service root endpoint.
         */
        get(apiPaths.root) {
            val accepts = call.request.acceptItems()
            log.info("Root requested, accepts: $accepts")
            call.respondText("Recommendation Service!", ContentType.Text.Plain)
        }

        /**
         * Authenticated user info endpoint.
         */
        get(apiPaths.me) {
            val user = call.authenticatedUser() // You must define this somewhere
            call.respondText(
                "Hello ${user.username}! 👤 Your email is ${user.email}, id is ${user.id}, roles: ${user.roles}",
                ContentType.Text.Plain
            )
        }


    }
}