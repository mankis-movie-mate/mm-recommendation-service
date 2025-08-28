package kz.mm.repository

import kz.mm.client.activityService.Activity
import org.neo4j.driver.Driver
import org.neo4j.driver.Values
import org.slf4j.LoggerFactory

class ActivityGraphRepository(
    private val driver: Driver
) {
    private val log = LoggerFactory.getLogger(ActivityGraphRepository::class.java)

    fun upsertActivity(event: Activity) {
        driver.session().use { session ->
            // Ensure user exists
            session.run(
                "MERGE (u:User {id: \$userId})",
                Values.parameters("userId", event.userId)
            )
            // Ensure movie exists
            session.run(
                "MERGE (m:Movie {id: \$movieId})",
                Values.parameters("movieId", event.movieId)
            )
            // Create or update relationship
            val relType = event.action.name
            session.run(
                "MATCH (u:User {id: \$userId}), (m:Movie {id: \$movieId}) " +
                        "MERGE (u)-[r:$relType]->(m) " +
                        "ON CREATE SET r.timestamp = \$timestamp " +
                        "ON MATCH SET r.timestamp = \$timestamp"
                            .trimIndent(),
                Values.parameters(
                    "userId", event.userId,
                    "movieId", event.movieId,
                    "timestamp", event.timestamp
                )
            )
            log.info("REPO: Upserted $relType relationship: ${event.userId} -> ${event.movieId}")
        }
    }

}
