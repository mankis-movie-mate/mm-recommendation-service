package kz.mm.service

import kz.mm.model.MovieSimilarityExplanation

/**
 * Defines a strategy for scoring candidate recommended movies.
 */
fun interface SimilarityAggregationStrategy {
    /**
     * Computes the ranking score for a set of explanations (per candidate movie).
     */
    fun aggregate(explanations: List<MovieSimilarityExplanation>): Double

}