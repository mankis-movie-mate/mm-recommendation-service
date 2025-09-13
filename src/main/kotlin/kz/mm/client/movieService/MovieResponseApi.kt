package kz.mm.client.movieService

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieApiResponse(
    val elements: List<MovieApiModel>
)

@Serializable
data class MovieApiModel(
    val _id: String? = null,  // Make optional!
    val id: String? = null,
    val title: String,
    val genres: List<String>,
    val director: PersonApiModel? = null,
    val casts: List<PersonApiModel> = emptyList(),
    val synopsis: String? = null,
    val releaseDate: String? = null,
    val language: String? = null,
    val rating: RatingApiModel? = null,
    val reviews: List<ReviewApiModel> = emptyList(),
    val posterUrl: String = "",
)

@Serializable
data class PersonApiModel(
    val firstName: String,
    val lastName: String
)

@Serializable
data class RatingApiModel(
    val average: Double = 0.0,
    val count: Int = 0
)

@Serializable
data class ReviewApiModel(
    val author: String? = null,
    val text: String? = null,
    val createdAt: String? = null
)
