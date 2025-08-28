import io.ktor.client.request.*

fun HttpRequestBuilder.authenticateAsTestUser(
    userId: String = "test-user-123",
    email: String = "test@themanki.net",
    username: String = "themanki",
    roles: String = "USER"
) {
    header(RequestHeaders.USER_ID, userId)
    header(RequestHeaders.USER_EMAIL, email)
    header(RequestHeaders.USER_USERNAME, username)
    header(RequestHeaders.USER_ROLES, roles)
}
