package kz.mm.repository

import org.neo4j.driver.Driver
import org.neo4j.driver.Values
import org.slf4j.LoggerFactory

/**
 * Repository responsible for movie recommendation operations via Neo4j GDS.
 * Handles all data access and GDS projection logic.
 */
class RecommendationRepository(
    private val driver: Driver
) {

    private val log = LoggerFactory.getLogger(RecommendationRepository::class.java)


    /**
     * For a given movie, find similar movies (id + similarity score only).
     */
    fun getSimilarMovies(movieId: String, limit: Int): List<Pair<String, Double>> =
        driver.session().use { session ->
            log.info("REPO: Querying similar movies for movieId=$movieId, limit=$limit")
            ensureGraphProjection(session)
            val movieNodeId = getInternalMovieNodeId(session, movieId) ?: run {
                log.info("REPO: Movie $movieId not found in Neo4j, returning empty list")
                return emptyList()
            }
            val nodeSimilarity = queryNodeSimilarity(session, movieNodeId, limit)
            log.info("REPO: Found ${nodeSimilarity.size} similar nodes for internalMovieNodeId=$movieNodeId")
            val mapped = mapInternalIdsToMovieIds(session, nodeSimilarity)
            log.info("REPO: Mapped ${mapped.size} similar movies for movieId=$movieId")
            mapped
        }


    /** Ensures Cypher graph projection exists, projects only movies and relevant relationships. */
    private fun ensureGraphProjection(session: org.neo4j.driver.Session) {
        val graphName = GDS_GRAPH_NAME
        val exists = session.run(
            "CALL gds.graph.exists(\$graphName) YIELD exists RETURN exists",
            Values.parameters("graphName", graphName)
        ).single()["exists"].asBoolean()
        if (!exists) {
            session.run(
                """
                CALL gds.graph.project.cypher(
                    '$graphName',
                    'MATCH (m:Movie) RETURN id(m) AS id',
                    'MATCH (m1:Movie)<-[:RATED|WATCHLISTED]-(u:User)-[:RATED|WATCHLISTED]->(m2:Movie)
                     WHERE m1 <> m2
                     RETURN id(m1) AS source, id(m2) AS target'
                )
                """.trimIndent()
            )
        }
    }

    /** Maps an external movieId to Neo4j internal nodeId, null if not found. */
    private fun getInternalMovieNodeId(session: org.neo4j.driver.Session, movieId: String): Long? {
        return session.run(
            "MATCH (m:Movie {id: \$movieId}) RETURN id(m) AS nodeId",
            Values.parameters("movieId", movieId)
        ).list().singleOrNull()?.get("nodeId")?.asLong()
    }

    /** Runs the node similarity query for a given internal nodeId, returns list of (internalNodeId, similarity). */
    private fun queryNodeSimilarity(
        session: org.neo4j.driver.Session,
        movieNodeId: Long,
        limit: Int
    ): List<Pair<Long, Double>> {
        val cypher = """
            CALL gds.nodeSimilarity.stream('$GDS_GRAPH_NAME')
            YIELD node1, node2, similarity
            WHERE node1 = $movieNodeId
            RETURN node2 AS targetNodeId, similarity
            ORDER BY similarity DESC
            LIMIT $limit
        """.trimIndent()
        return session.run(cypher).list { record ->
            record["targetNodeId"].asLong() to record["similarity"].asDouble()
        }
    }

    /** Maps internal Neo4j node IDs to movieIds and returns (movieId, similarity). */
    private fun mapInternalIdsToMovieIds(
        session: org.neo4j.driver.Session,
        simList: List<Pair<Long, Double>>
    ): List<Pair<String, Double>> {
        if (simList.isEmpty()) return emptyList()
        val nodeIds = simList.map { it.first }
        val movies = session.run(
            "MATCH (m:Movie) WHERE id(m) IN \$ids RETURN id(m) AS nodeId, m.id AS movieId",
            Values.parameters("ids", nodeIds)
        ).list { rec -> rec["nodeId"].asLong() to rec["movieId"].asString() }.toMap()
        return simList.mapNotNull { (nodeId, sim) -> movies[nodeId]?.let { it to sim } }
    }

    companion object {
        private const val GDS_GRAPH_NAME = "movie-reco-graph"
    }
}
