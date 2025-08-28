package kz.mm.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kz.mm.client.activityService.Activity
import kz.mm.config.ApiPaths
import kz.mm.service.ActivitySyncService
import org.slf4j.LoggerFactory


@Serializable
data class DaprSubscription(
    val pubsubname: String,
    val topic: String,
    val routes: DaprRoutes
)

@Serializable
data class DaprRoutes(
    val default: String
)


class DaprAPI(
    private val activityService: ActivitySyncService
) {
    private val log = LoggerFactory.getLogger(DaprAPI::class.java)

    fun Route.daprSubscriptionRoute(apiPaths: ApiPaths) {

        get(apiPaths.daprSub) {
            val pubsubName = System.getenv("MOVIE_MATE_KAFKA_PUBSUB") ?: "kafka-pubsub"
            val topic = System.getenv("MOVIE_MATE_KAFKA_ACTIVITY_TOPIC") ?: "activity-events"
            val route = apiPaths.daprEvent
            val subscription = DaprSubscription(
                pubsubname = pubsubName,
                topic = topic,
                routes = DaprRoutes(default = route)
            )

            // Logging: show env, request info, and what is being returned
            application.log.info(
                """
            [DAPR] /dapr/subscribe requested
            - pubsubname: $pubsubName
            - topic: $topic
            - route: $route
            - From: ${call.request.origin.remoteHost}
            - Responding: ${listOf(subscription)}
            """.trimIndent()
            )

            call.respond(listOf(subscription))
        }

        post(apiPaths.daprEvent) {
            val payload = call.receiveText()
            log.info("API: Received POST for sync activities with payload: $payload")
            val event = try {
                Json.decodeFromString(Activity.serializer(), payload)
            } catch (e: Exception) {
                log.error("API: Failed to parse activity event", e)
                call.respond(HttpStatusCode.BadRequest, "Invalid payload")
                return@post
            }
            activityService.syncActivityEvent(event)
            call.respondText("OK")
        }

    }
}