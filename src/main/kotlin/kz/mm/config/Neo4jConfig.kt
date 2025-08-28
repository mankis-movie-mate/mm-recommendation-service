package kz.mm.config

import io.ktor.server.config.*
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.exceptions.ServiceUnavailableException

class Neo4jConfig(config: ApplicationConfig) {
    val uri: String = config.property("neo4j.uri").getString()
    val user: String = config.property("neo4j.user").getString()
    val password: String = config.property("neo4j.password").getString()

    val driver: Driver by lazy { connectAndLog() }

    private fun connectAndLog(): Driver {
        println("[Neo4j] Attempting to connect to $uri as $user.")
        val driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))
        return try {
            driver.verifyConnectivity()
            println("[Neo4j] Connected successfully!")
            driver
        } catch (ex: Exception) {
            println("[Neo4j] Connection FAILED: ${ex.message}")
            driver.close()
            throw ServiceUnavailableException("Neo4j connection failed: ${ex.message}", ex)
        }
    }
}
