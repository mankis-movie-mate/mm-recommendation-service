package kz.mm.client.movieService


/**
 * Abstracts movie info retrieval
 */
interface MovieService {
    /**
     * Lookup info for a single movie by ID.
     */
    fun getMovie(movieId: String): Movie?

    /**
     * Batch lookup for multiple movies by ID.
     */
    fun getMovies(movieIds: Collection<String>): List<Movie>

    /**
     * Returns the top movies by rating.
     * Used for fallback recommendations.
     */
    fun getTopMovies(limit: Int): List<Movie>
}