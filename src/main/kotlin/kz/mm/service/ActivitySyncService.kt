package kz.mm.service

import kz.mm.client.activityService.Activity
import kz.mm.repository.ActivityGraphRepository
import org.slf4j.LoggerFactory

class ActivitySyncService(
    private val repo: ActivityGraphRepository
) {
    private val log = LoggerFactory.getLogger(ActivitySyncService::class.java)

    /**
     * Main business logic for syncing activity event to Neo4j graph.
     */
    fun syncActivityEvent(event: Activity) {
        log.info("SERVICE: Received activity event $event")
        repo.upsertActivity(event)
        log.info("SERVICE: Synced activity event to Neo4j and ensured GDS projection")
    }
}