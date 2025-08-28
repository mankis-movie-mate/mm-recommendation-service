package kz.mm.init

import org.neo4j.driver.Driver

object Neo4jTestDataUtil {
    fun initSampleData(driver: Driver) {
        driver.session().use { session ->
            val userCount = session.run("MATCH (u:User) RETURN count(u) AS cnt")
                .single()["cnt"].asInt()
            if (userCount > 0) {
                println("[Neo4jTestDataUtil] Sample data already exists.")
                return
            }
            println("[Neo4jTestDataUtil] Initializing sample data...")

            val queries = listOf(
                // Users
                "CREATE (:User {id: 'u1', name: 'Alice'})",
                "CREATE (:User {id: 'u2', name: 'Bob'})",
                "CREATE (:User {id: 'u3', name: 'Carol'})",

                // Movies
                "CREATE (:Movie {id: 'm1', title: 'Inception'})",
                "CREATE (:Movie {id: 'm2', title: 'The Matrix'})",
                "CREATE (:Movie {id: 'm3', title: 'Interstellar'})",
                "CREATE (:Movie {id: 'm4', title: 'The Godfather'})",
                "CREATE (:Movie {id: 'm5', title: 'Blade Runner'})",

                // --- Ratings/Watchlists ---
                // Alice rates m1, m2 (sci-fi), watchlists m3 (drama), hasn't touched m4 or m5
                "MATCH (u:User {id:'u1'}), (m:Movie {id:'m1'}) CREATE (u)-[:RATED {score: 5}]->(m)",
                "MATCH (u:User {id:'u1'}), (m:Movie {id:'m2'}) CREATE (u)-[:RATED {score: 4}]->(m)",
                "MATCH (u:User {id:'u1'}), (m:Movie {id:'m3'}) CREATE (u)-[:WATCHLISTED]->(m)",

                // Bob rates m2, m3 (so overlaps with Alice on m2, but also covers m3)
                // Bob also rates m4 (unique to Bob)
                "MATCH (u:User {id:'u2'}), (m:Movie {id:'m2'}) CREATE (u)-[:RATED {score: 4}]->(m)",
                "MATCH (u:User {id:'u2'}), (m:Movie {id:'m3'}) CREATE (u)-[:RATED {score: 5}]->(m)",
                "MATCH (u:User {id:'u2'}), (m:Movie {id:'m4'}) CREATE (u)-[:RATED {score: 2}]->(m)",

                // Carol rates m3, watchlists m5
                "MATCH (u:User {id:'u3'}), (m:Movie {id:'m3'}) CREATE (u)-[:RATED {score: 3}]->(m)",
                "MATCH (u:User {id:'u3'}), (m:Movie {id:'m5'}) CREATE (u)-[:WATCHLISTED]->(m)"
            )

            queries.forEach { q -> session.run(q) }
            println("[Neo4jTestDataUtil] Sample data loaded.")
        }
    }

}
