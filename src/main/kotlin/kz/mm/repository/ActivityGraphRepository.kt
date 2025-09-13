package kz.mm.repository

import kz.mm.client.activityService.Activity
import kz.mm.client.activityService.ActivityType
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
            log.info("REPO: Upserted $relType relationship: ${event.userId} --${relType}--> ${event.movieId}")
        }
    }


    fun getRecentActivities(userId: String, count: Int): List<Activity> {
        val log = LoggerFactory.getLogger(ActivityGraphRepository::class.java)
        driver.session().use { session ->
            val query = (
                    "MATCH (u:User {id: \$userId})-[r]->(m:Movie) " +
                            "RETURN type(r) AS action, m.id AS movieId, r.timestamp AS timestamp " +
                            "ORDER BY r.timestamp DESC " +
                            "LIMIT \$count"
                    )
            val params = mapOf("userId" to userId, "count" to count)
            log.info("Running getRecentActivities for userId=$userId, count=$count")
            val result = session.run(query, params)
            val activities = result.list { record ->
                val timestamp =
                    if (!record["timestamp"].isNull) record["timestamp"].asLong()
                    else {
                        log.warn("Activity relationship for user=$userId and movie=${record["movieId"]} is missing timestamp. Using 0L.")
                        0L
                    }
                Activity(
                    userId = userId,
                    movieId = record["movieId"].asString(),
                    action = ActivityType.valueOf(record["action"].asString()),
                    timestamp = timestamp
                )
            }
            log.info("Found ${activities.size} activities for userId=$userId")
            return activities
        }
    }

}
