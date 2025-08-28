package kz.mm.client.activityService

class MockActivityService : ActivityService {
    private val activities = listOf(
        Activity("u1", "m1", ActivityType.RATED, 1714400000000),         // Alice: RATED Inception
        Activity("u1", "m2", ActivityType.RATED, 1714410000000),         // Alice: RATED The Matrix
        Activity("u1", "m3", ActivityType.WATCHLISTED, 1714420000000),   // Alice: WATCHLISTED Interstellar

        Activity("u2", "m2", ActivityType.RATED, 1714430000000),         // Bob: RATED The Matrix
        Activity("u2", "m3", ActivityType.RATED, 1714440000000),         // Bob: RATED Interstellar
        Activity("u2", "m4", ActivityType.RATED, 1714450000000),         // Bob: RATED The Godfather

        Activity("u3", "m3", ActivityType.RATED, 1714460000000),         // Carol: RATED Interstellar
        Activity("u3", "m5", ActivityType.WATCHLISTED, 1714470000000)    // Carol: WATCHLISTED Blade Runner
    )

    override fun getRecentActivities(
        userId: String,
        types: Set<ActivityType>?,
        limit: Int
    ): List<Activity> =
        activities.filter { it.userId == userId && (types == null || it.action in types) }
            .sortedByDescending { it.timestamp }
            .take(limit)
}