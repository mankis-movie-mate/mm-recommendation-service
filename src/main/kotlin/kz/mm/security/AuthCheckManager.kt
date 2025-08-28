package kz.mm.security


import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import kz.mm.client.userServcie.User

object AuthCheckManager {
    fun authenticate(call: ApplicationCall): User {
        val userId = call.request.header("X-User-Id")
        val username = call.request.header("X-User-Username")
        val email = call.request.header("X-User-Email")
        val rolesHeader = call.request.header("X-User-Roles") ?: ""
        val roles = rolesHeader.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (userId.isNullOrBlank() || username.isNullOrBlank() || email.isNullOrBlank()) {
            throw BadRequestException("Missing authentication headers")
        }

        return User(
            id = userId,
            username = username,
            email = email,
            roles = roles
        )
    }
}
