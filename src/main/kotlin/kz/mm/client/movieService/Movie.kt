package kz.mm.client.movieService

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String,
    val title: String,
    val genres: List<String>,
    val releaseYear: Int,
    val rating: Double? = null,
    val posterUrl: String,
)