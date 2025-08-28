package kz.mm.client.userServcie

data class User(
    val id: String,
    val username: String,
    val email: String,
    val roles: List<String>
)