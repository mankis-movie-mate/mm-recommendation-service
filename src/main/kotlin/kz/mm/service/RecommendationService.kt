package kz.mm.service

import kz.mm.model.Recommendation

/**
 * Defines contract for movie recommendation services.
 */
interface RecommendationService {
    /**
     * Recommends movies for a user, with explanation and ranking.
     * @param userId the user to recommend for
     * @param limit max number of recommendations to return
     * @param seedCount how many recent user activities to use as seeds
     * @param detailed return detailed info
     */
    fun recommendForUser(
        userId: String,
        limit: Int = 5,
        seedCount: Int = 5,
        detailed: Boolean = false
    ): Recommendation
}