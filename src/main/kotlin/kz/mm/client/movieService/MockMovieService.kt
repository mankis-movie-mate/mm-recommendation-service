package kz.mm.client.movieService

class MockMovieService : MovieService {
//    private val movies = listOf(
//        Movie(
//            id = "m1",
//            title = "Inception",
//            genres = listOf("Sci-Fi", "Thriller"),
//            releaseYear = 2010,
//            rating = 8.8
//        ),
//        Movie(
//            id = "m2",
//            title = "The Matrix",
//            genres = listOf("Sci-Fi", "Action"),
//            releaseYear = 1999,
//            rating = 8.7
//        ),
//        Movie(
//            id = "m3",
//            title = "Interstellar",
//            genres = listOf("Sci-Fi", "Drama"),
//            releaseYear = 2014,
//            rating = 8.6
//        ),
//        Movie(
//            id = "m4",
//            title = "The Godfather",
//            genres = listOf("Crime", "Drama"),
//            releaseYear = 1972,
//            rating = 9.2
//        ),
//        Movie(
//            id = "m5",
//            title = "Blade Runner",
//            genres = listOf("Sci-Fi", "Thriller"),
//            releaseYear = 1982,
//            rating = 8.1
//        )
//    )

//    fun getMovie(movieId: String): Movie? =
//        movies.find { it.id == movieId }
//
//    fun getMovies(movieIds: Collection<String>): List<Movie> =
//        movies.filter { it.id in movieIds }
//
//    fun getTop5Movies(): List<Movie> =
//        movies.sortedByDescending { it.rating }

    override fun getMovie(movieId: String, authToken: String): Movie? {
        TODO("Not yet implemented")
    }

    override fun getMovies(movieIds: Collection<String>, authToken: String): List<Movie> {
        TODO("Not yet implemented")
    }

    override fun getTop5Movies(authToken: String): List<Movie> {
        TODO("Not yet implemented")
    }
}