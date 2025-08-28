package kz.mm.client.activityService

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val userId: String,
    val movieId: String,
    val action: ActivityType,
    val timestamp: Long
)