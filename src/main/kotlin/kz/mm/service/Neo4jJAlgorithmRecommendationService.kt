package kz.mm.service

import kz.mm.client.activityService.Activity
import kz.mm.client.movieService.MovieService
import kz.mm.model.MovieSimilarityExplanation
import kz.mm.model.Recommendation
import kz.mm.model.RecommendedMovie
import kz.mm.repository.ActivityGraphRepository
import kz.mm.repository.RecommendationRepository
import org.slf4j.LoggerFactory

/**
 * Recommends movies for a user using Neo4j GDS node similarity.
 * - Non-detailed: fast, returns recommended movies with scores.
 * - Detailed: includes for each movie a list of explanations, one per seed movie.
 */
class Neo4jJAlgorithmRecommendationService(
    private val repo: RecommendationRepository,
    private val movieService: MovieService,
    private val activityGraphRepository: ActivityGraphRepository,
    private val scoreStrategy: SimilarityAggregationStrategy = AverageSimilarityStrategy
) : RecommendationService {

    private val log = LoggerFactory.getLogger(Neo4jJAlgorithmRecommendationService::class.java)


    override fun recommendForUser(
        userId: String,
        limit: Int,
        seedCount: Int,
        detailed: Boolean,
        authToken: String
    ): Recommendation {
        log.info("SERVICE: Starting recommendation for user=$userId, limit=$limit, seeds=$seedCount, detailed=$detailed")
        val seeds = getSeedActivities(userId, seedCount)
        log.info("SERVICE: Got ${seeds.size} seed activities for user=$userId")
        if (seeds.isEmpty()) {
            log.info("SERVICE: No seeds, returning empty recommendations for user=$userId")
            return Recommendation(userId, emptyList())
        }
        val seen = seeds.map { it.movieId }.toSet()

        val recommended = if (!detailed) {
            log.info("SERVICE: Building SIMPLE recommendations for user=$userId")
            buildSimpleRecommendations(seeds, seen, limit, authToken)
        } else {
            log.info("SERVICE: Building DETAILED recommendations for user=$userId")
            buildDetailedRecommendations(seeds, seen, limit, authToken)
        }
        log.info("SERVICE: Built ${recommended.size} recommendations for user=$userId")
        return Recommendation(userId, recommended)
    }

    // Used for both simple & detailed: collects a list of MovieSimilarityExplanation per candidate
    private fun collectCandidates(
        seeds: List<Activity>,
        seen: Set<String>,
        limit: Int,
        authToken: String
    ): Map<String, MutableList<MovieSimilarityExplanation>> {
        val candidates = mutableMapOf<String, MutableList<MovieSimilarityExplanation>>()
        for (seed in seeds) {
            repo.getSimilarMovies(seed.movieId, limit).forEach { (movieId, similarity) ->
                if (movieId !in seen) {
                    candidates.getOrPut(movieId) { mutableListOf() }.add(
                        MovieSimilarityExplanation(
                            seedMovieId = seed.movieId,
                            seedMovieTitle = movieService.getMovie(seed.movieId, authToken)?.title,
                            similarity = similarity,
                            activityType = seed.action
                        )
                    )
                }
            }
        }
        log.info("SERVICE: Collected ${candidates.size} candidate movies (not seen by user)")
        return candidates
    }

    private fun buildSimpleRecommendations(
        seeds: List<Activity>,
        seen: Set<String>,
        limit: Int,
        authToken: String
    ): List<RecommendedMovie> {
        val candidates = collectCandidates(seeds, seen, limit, authToken)
        val scored = candidates.entries
            .map { (movieId, explanations) -> movieId to scoreStrategy.aggregate(explanations) }
            .sortedByDescending { it.second }
            .take(limit)
        val movies = movieService.getMovies(scored.map { it.first }, authToken).associateBy { it.id }
        return scored.mapNotNull { (movieId, score) ->
            movies[movieId]?.let { movie ->
                RecommendedMovie(movie = movie, score = score, explanations = null)
            }
        }
    }

    private fun buildDetailedRecommendations(
        seeds: List<Activity>,
        seen: Set<String>,
        limit: Int,
        authToken: String
    ): List<RecommendedMovie> {
        val candidates = collectCandidates(seeds, seen, limit, authToken)
        val scored = candidates.entries
            .map { (movieId, explanations) ->
                val avgSim = if (explanations.isNotEmpty())
                    explanations.sumOf { it.similarity } / explanations.size else 0.0
                movieId to avgSim
            }
            .sortedByDescending { it.second }
            .take(limit)
        val movies = movieService.getMovies(scored.map { it.first }, authToken).associateBy { it.id }
        return scored.mapNotNull { (movieId, score) ->
            movies[movieId]?.let { movie ->
                RecommendedMovie(
                    movie = movie,
                    score = score,
                    explanations = candidates[movieId]
                )
            }
        }
    }


    private fun getSeedActivities(userId: String, count: Int): List<Activity> =
        activityGraphRepository.getRecentActivities(userId, count)

    companion object {
        val AverageSimilarityStrategy = SimilarityAggregationStrategy { exps ->
            if (exps.isEmpty()) 0.0 else exps.sumOf { it.similarity } / exps.size
        }
    }
}