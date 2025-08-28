package kz.mm.client.activityService

/**
 * Abstracts user activity queries (which movies a user recently interacted with).
 */
interface ActivityService {
    /**
     * Returns recent activities of a user, optionally filtered by action type(s).
     * @param userId - ID of the user.
     * @param types - Filter by these action types (null = all).
     */
    fun getRecentActivities(
        userId: String,
        types: Set<ActivityType>? = null,
        limit: Int = 20
    ): List<Activity>

    /**
     * Returns only activities of type RATED for a user.
     */
    fun getRatedActivities(userId: String): List<Activity> =
        getRecentActivities(userId, setOf(ActivityType.RATED))

    /**
     * Returns only activities of type WATCHLISTED for a user.
     */
    fun getWatchlistedActivities(userId: String): List<Activity> =
        getRecentActivities(userId, setOf(ActivityType.WATCHLISTED))
}