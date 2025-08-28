package kz.mm.model

import kotlinx.serialization.Serializable
import kz.mm.client.activityService.ActivityType

@Serializable
data class MovieSimilarityExplanation(
    val seedMovieId: String,
    val seedMovieTitle: String?,
    val similarity: Double,
    val activityType: ActivityType
)