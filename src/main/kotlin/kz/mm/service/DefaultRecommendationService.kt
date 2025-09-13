package kz.mm.service

import kz.mm.client.movieService.MovieService
import kz.mm.model.Recommendation
import kz.mm.model.RecommendedMovie
import org.slf4j.LoggerFactory

class DefaultRecommendationService(
    private val core: RecommendationService,
    private val movieService: MovieService   // must provide top/popular movies
) : RecommendationService {

    private val log = LoggerFactory.getLogger(DefaultRecommendationService::class.java)


    override fun recommendForUser(
        userId: String,
        limit: Int,
        seedCount: Int,
        detailed: Boolean,
        authToken: String
    ): Recommendation {
        log.info("Generating recommendations for userId=$userId, limit=$limit, seedCount=$seedCount, detailed=$detailed")
        val rec = core.recommendForUser(userId, limit, seedCount, detailed, authToken)
        log.info("Core recommender produced ${rec.recommended.size} recommendations")

        val alreadyRecommended = rec.recommended.map { it.movie.id }.toSet()
        val missing = limit - rec.recommended.size

        val paddingMovies = if (missing > 0) {
            log.info("Padding with $missing popular movies")
            movieService.getTop5Movies(authToken)
                .filter { it.id !in alreadyRecommended }
                .take(missing)
                .onEach { movie ->
                    log.info("Adding fallback movie '${movie.title}' (id=${movie.id})")
                }
                .map { movie ->
                    RecommendedMovie(movie = movie, score = 0.0, explanations = null)
                }
        } else emptyList()

        val all = (rec.recommended + paddingMovies).take(limit)
        log.info("Final list for userId=$userId has ${all.size} recommendations")
        return Recommendation(userId, all)
    }
}
