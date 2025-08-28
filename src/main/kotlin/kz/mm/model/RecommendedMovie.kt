package kz.mm.model

import kotlinx.serialization.Serializable
import kz.mm.client.movieService.Movie

@Serializable
data class RecommendedMovie(
    val movie: Movie,
    val score: Double,
    val explanations: List<MovieSimilarityExplanation>? = emptyList()
)