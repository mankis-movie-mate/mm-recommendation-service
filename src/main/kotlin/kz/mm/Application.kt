package kz.mm

import configureOpenApi
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kz.mm.client.activityService.MockActivityService
import kz.mm.client.movieService.MockMovieService
import kz.mm.config.Neo4jConfig
import kz.mm.config.configureCORS
import kz.mm.config.configureRouting
import kz.mm.config.configureSecurity
import kz.mm.init.Neo4jTestDataUtil
import kz.mm.init.consul.registerWithConsul
import kz.mm.init.opeanapi.patchAndOverwriteOpenApiResource
import kz.mm.repository.ActivityGraphRepository
import kz.mm.repository.RecommendationRepository
import kz.mm.rest.DaprAPI
import kz.mm.rest.InternalAPI
import kz.mm.rest.RecommendationAPI
import kz.mm.service.ActivitySyncService
import kz.mm.service.DefaultRecommendationService
import kz.mm.service.Neo4jJAlgorithmRecommendationService


fun main(args: Array<String>) {
    io.ktor.server.tomcat.jakarta.EngineMain.main(args)
//   embeddedServer()
}


fun Application.module() {
    install(ContentNegotiation) { json() }

    // Dependency setup
    val neo4jConfig = Neo4jConfig(environment.config)
    val repo = RecommendationRepository(neo4jConfig.driver)
    val movieService = MockMovieService()
    val activityService = MockActivityService()
    val recService = DefaultRecommendationService(
        Neo4jJAlgorithmRecommendationService(repo, activityService, movieService),
        movieService
    )
    val activityGraphRepo = ActivityGraphRepository(neo4jConfig.driver)
    val activitySyncService = ActivitySyncService(activityGraphRepo)
    val recAPI = RecommendationAPI(recService);
    val intAPI = InternalAPI();
    val daprAPI = DaprAPI(activitySyncService);

    // Data init before API is available!
    Neo4jTestDataUtil.initSampleData(neo4jConfig.driver)

    configureRouting(recAPI, intAPI, daprAPI)
    configureCORS()
    configureSecurity()
    registerWithConsul()
    patchAndOverwriteOpenApiResource()
    configureOpenApi()

}
