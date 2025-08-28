package kz.mm.config

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import kz.mm.rest.DaprAPI
import kz.mm.rest.InternalAPI
import kz.mm.rest.RecommendationAPI

data class ApiPaths(
    val health: String = "/health",
    val root: String = "/",
    val me: String = "/me",
    val daprEvent: String = "/dapr/activity/sync",
    val daprSub: String = "/dapr/subscribe"
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): ApiPaths = ApiPaths(
            health = config.propertyOrNull("cloud.health-path")?.getString() ?: "/health",
            root = config.propertyOrNull("cloud.root-path")?.getString() ?: "/",
            me = config.propertyOrNull("cloud.me-path")?.getString() ?: "/me",
        )
    }
}

fun Application.configureRouting(
    recommendationApi: RecommendationAPI,
    internalAPI: InternalAPI,
    daprApi: DaprAPI,
) {
    routing {
        // Load all API paths from config (centralized definition)
        val apiPaths = ApiPaths.fromConfig(environment.config)

        with(recommendationApi) { recommendationRoutes() }
        with(internalAPI) { internalApiRoutes(apiPaths) }
        with(daprApi) { daprSubscriptionRoute(apiPaths) }

    }
}
