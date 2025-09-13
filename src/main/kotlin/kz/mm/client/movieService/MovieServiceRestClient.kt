package kz.mm.client.movieService

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

object MovieServiceEnv {
    val baseUrl: String = System.getenv("MOVIE_MATE_MOVIE_SERVICE_BASE_URL")
        ?: "http://mm-movie-service:8080"
}

class MovieServiceRestClient(
    private val httpClient: HttpClient = defaultHttpClient,
    private val baseUrl: String = MovieServiceEnv.baseUrl
) : MovieService {

    companion object {
        val log = LoggerFactory.getLogger(MovieServiceRestClient::class.java)

        val defaultHttpClient = HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) { requestTimeoutMillis = 5000 }
        }
    }

    override fun getMovie(movieId: String, authToken: String): Movie? = runBlocking {
        val url = "$baseUrl/api/$movieId"
        try {
            log.info("Calling MovieService GET $url")
            val apiModel: MovieApiModel = httpClient.get(url) {
                if (authToken.isNotBlank()) header("Authorization", authToken)
            }.body()
            log.info("MovieService returned movie with id=${apiModel.id}")
            apiModel.toInternalMovie()
        } catch (ex: Exception) {
            log.warn("Failed to fetch movie $movieId from MovieService: ${ex.message}")
            null
        }
    }

    override fun getMovies(movieIds: Collection<String>, authToken: String): List<Movie> = runBlocking {
        if (movieIds.isEmpty()) {
            log.info("getMovies called with empty movieIds; returning empty list")
            return@runBlocking emptyList<Movie>()
        }
        val url = "$baseUrl/api/movies/all-by-ids"
        try {
            log.info("Calling MovieService POST $url for movieIds=${movieIds.joinToString(",")}")
            val requestBody = MoviesByIdsRequest(movieIds.toList())
            val response: MovieApiResponse = httpClient.post(url) {
                setBody(requestBody)
                if (authToken.isNotBlank()) header("Authorization", authToken)
            }.body()
            log.info("MovieService returned ${response.elements.size} movies")
            response.elements.map { it.toInternalMovie() }
        } catch (ex: Exception) {
            log.warn("Failed to batch fetch movies from MovieService: ${ex.message} (falling back to getMovie per id)")
            movieIds.mapNotNull { getMovie(it, authToken) }
        }
    }

    override fun getTop5Movies(authToken: String): List<Movie> = runBlocking {
        val type = "third-party"
        val url = "$baseUrl/api/top5/$type"
        try {
            log.info("Calling MovieService GET $url (top 5, type=$type)")
            val response: List<MovieApiModel> = httpClient.get(url) {
                if (authToken.isNotBlank()) header("Authorization", authToken)
            }.body()
            log.info("MovieService returned top5 size=${response.size}")
            response.map { it.toInternalMovie() }
        } catch (ex: Exception) {
            log.warn("Failed to fetch top5 movies from MovieService: ${ex.message}")
            emptyList()
        }
    }

}

@Serializable
data class MoviesByIdsRequest(val ids: List<String>)
private fun MovieApiModel.toInternalMovie(): Movie = Movie(
    id = _id ?: id ?: title, // fallback to title
    title = title,
    genres = genres,
    releaseYear = releaseDate?.substring(0, 4)?.toIntOrNull() ?: 0,
    rating = rating?.average,
    posterUrl = posterUrl
)
