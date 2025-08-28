package kz.mm.rest

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kz.mm.service.RecommendationService
import org.slf4j.LoggerFactory

class RecommendationAPI(
    private val recService: RecommendationService
) {

    private val log = LoggerFactory.getLogger(RecommendationAPI::class.java)

    fun Route.recommendationRoutes() {
        // TODO get user id from auth
        route("/recommend") {
            get("/{userId}") {
                val userId = call.parameters["userId"] ?: return@get call.respondText(
                    "Missing userId", status = HttpStatusCode.BadRequest
                )
                val detailed = call.request.queryParameters["detailed"]?.toBooleanStrictOrNull() ?: false
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5
                val seedCount = call.request.queryParameters["seedCount"]?.toIntOrNull() ?: 5

                log.info("API: GET /recommend/$userId?detailed=$detailed&limit=$limit&seedCount=$seedCount")
                val recommendations = recService.recommendForUser(userId, limit, seedCount, detailed)
                log.info("API: Recommendations generated for userId=$userId, count=${recommendations.recommended.size}")
                call.respond(recommendations)
            }


        }
    }

}