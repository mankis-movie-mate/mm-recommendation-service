package kz.mm.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kz.mm.client.userServcie.User
import kz.mm.security.AuthCheckManager

val AuthenticatedUserKey = AttributeKey<User>("AuthenticatedUser")

/**
 * Loads excluded paths from application config or uses sensible defaults.
 * - Looks for 'cloud.health-path'
 * - Optionally, looks for 'security.excluded-paths' (comma-separated)
 */
private fun Application.resolveExcludedPaths(): Set<String> {
    // Get context path/rootPath, default to empty string if not set
    val contextPath = environment.config.propertyOrNull("ktor.deployment.rootPath")?.getString() ?: ""
    // Normalize to always have a leading slash and no trailing slash (except for root)
    val normalizedContext = when {
        contextPath.isBlank() -> ""
        contextPath == "/" -> ""
        contextPath.startsWith("/") -> contextPath.trimEnd('/')
        else -> "/$contextPath"
    }

    // Read paths to exclude
    val healthPath = environment.config.propertyOrNull("cloud.health-path")?.getString() ?: "/healthz"
    val extraExcluded = environment.config.propertyOrNull("security.excluded-paths")
        ?.getString()
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    // Prepend context path to each excluded path if not already present
    fun withContextPath(path: String): String =
        if (normalizedContext.isBlank() || path.startsWith(normalizedContext)) path
        else (normalizedContext + (if (path.startsWith("/")) path else "/$path"))

    return (extraExcluded + healthPath)
        .map { withContextPath(it) }
        .toSet()
}

/**
 * Configures security/authentication, automatically resolving excluded paths from config.
 */
fun Application.configureSecurity() {
    val excludedPaths = resolveExcludedPaths()

    intercept(ApplicationCallPipeline.Plugins) {
        val currentPath = call.request.path()
        if (excludedPaths.any { currentPath.startsWith(it) }) {
            return@intercept
        }

        val user = try {
            AuthCheckManager.authenticate(call)
        } catch (e: BadRequestException) {
            call.respond(HttpStatusCode.Unauthorized, "Authentication failed: ${e.message}")
            finish()
            return@intercept
        }
        call.attributes.put(AuthenticatedUserKey, user)
    }
}

/**
 * Access the authenticated user from the call.
 */
fun ApplicationCall.authenticatedUser(): User = attributes[AuthenticatedUserKey]
