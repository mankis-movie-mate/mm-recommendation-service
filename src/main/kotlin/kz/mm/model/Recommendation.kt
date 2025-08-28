package kz.mm.model

import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
    val userId: String,
    val recommended: List<RecommendedMovie>
)
