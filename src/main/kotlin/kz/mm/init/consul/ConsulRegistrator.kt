package kz.mm.init.consul

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kz.mm.init.consul.model.Check
import kz.mm.init.consul.model.ConsulRegistration
import org.slf4j.Logger
import java.net.NetworkInterface

/**
 * Utility to find the first non-loopback IPv4 address on the machine.
 */
fun findNonLoopbackIp(): String? =
    NetworkInterface.getNetworkInterfaces()?.toList()
        ?.flatMap { it.inetAddresses.toList() }
        ?.firstOrNull { !it.isLoopbackAddress && it.hostAddress.indexOf(':') < 0 }
        ?.hostAddress

/**
 * Loads all configuration values required for Consul registration.
 * Throws if any required config is missing.
 */
data class ConsulConfig(
    val appPort: Int,
    val consulHost: String,
    val consulPort: Int,
    val shouldRegister: Boolean,
    val healthCheckPath: String,
    val healthCheckInterval: String,
    val registrationPort: Int,
    val serviceName: String,
    val scheme: String
) {
    companion object {
        fun from(app: Application): ConsulConfig {
            val config = app.environment.config
            val contextPath = config.property("ktor.deployment.rootPath").getString()
            return ConsulConfig(
                appPort = config.property("ktor.deployment.port").getString().toInt(),
                consulHost = config.property("consul.host").getString(),
                consulPort = config.property("consul.port").getString().toInt(),
                shouldRegister = config.propertyOrNull("consul.register")?.getString()?.toBooleanStrictOrNull() ?: true,
                healthCheckPath = contextPath + config.property("cloud.health-path").getString(),
                healthCheckInterval = config.property("consul.health-check-interval").getString(),
                registrationPort = config.property("consul.registrationPort").getString().toInt(),
                serviceName = config.property("app.name").getString(),
                scheme = config.propertyOrNull("consul.scheme")?.getString() ?: "http"
            )
        }
    }
}

/**
 * Try to register with Consul up to [maxAttempts] times, waiting [intervalMs] between attempts.
 */
suspend fun tryRegisterWithConsul(
    client: HttpClient,
    url: String,
    registration: ConsulRegistration,
    log: Logger,
    maxAttempts: Int = 10,
    intervalMs: Long = 10000
): Boolean {
    repeat(maxAttempts) { attempt ->
        try {
            log.info("🔁 Consul registration attempt ${attempt + 1}/$maxAttempts ...")
            val resp = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(registration)
            }
            if (resp.status.isSuccess()) {
                log.info("✅ Registered service in Consul (status: ${resp.status})")
                return true
            } else {
                log.error("❌ Consul registration failed with status: ${resp.status} (attempt ${attempt + 1})")
            }
        } catch (e: Exception) {
            log.error("❌ Exception during Consul registration (attempt ${attempt + 1}): ${e.message}", e)
        }
        if (attempt < maxAttempts - 1) delay(intervalMs)
    }
    log.error("❌ Gave up after $maxAttempts attempts to register with Consul!")
    return false
}

/**
 * Builds tags for Consul registration, including Traefik tags and metadata.
 */
fun buildServiceTags(serviceName: String, registrationPort: Int): List<String> = listOf(
    // Protected router: GET + POST + PUT + DELETE + PATCH
    "traefik.enable=true",
    "traefik.http.routers.$serviceName.rule=PathPrefix(`/$serviceName`) && (Method(`GET`) || Method(`POST`) || Method(`PUT`) || Method(`DELETE`) || Method(`PATCH`))",
    "traefik.http.routers.$serviceName.middlewares=dapr-rewrite@file,jwt-forwardauth@file",
    "traefik.http.services.$serviceName.loadbalancer.server.port=$registrationPort",

    // OPTIONS router (unprotected)
    "traefik.http.routers.${serviceName}-options.rule=PathPrefix(`/$serviceName`) && Method(`OPTIONS`)",
    "traefik.http.routers.${serviceName}-options.service=$serviceName",
    "traefik.http.routers.${serviceName}-options.priority=100",
    "traefik.http.routers.${serviceName}-options.middlewares=dapr-rewrite@file"
)
/**
 * Determines the service IP to register with Consul.
 * Prefers POD_IP env, falls back to first non-loopback IPv4 address, then 127.0.0.1.
 */
fun resolveServiceIp(): String =
    System.getenv("POD_IP") ?: findNonLoopbackIp() ?: "127.0.0.1"

/**
 * Registers the service with Consul.
 * Reads all config values, builds request, and logs all key steps and errors.
 */
fun Application.registerWithConsul() {
    val log = environment.log
    val consulConfig = ConsulConfig.from(this)

    if (!consulConfig.shouldRegister) {
        log.info("🛑 Consul registration is disabled via config.")
        return
    }

    val serviceId = "${consulConfig.serviceName}-${System.currentTimeMillis()}"
    val serviceAddress = resolveServiceIp()
    val tags = environment.config.property("consul.traefik.tags").getString()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val healthUrl = "${consulConfig.scheme}://$serviceAddress:${consulConfig.appPort}${consulConfig.healthCheckPath}"

    val registration = ConsulRegistration(
        ID = serviceId,
        Name = consulConfig.serviceName,
        Address = serviceAddress,
        Port = consulConfig.appPort,
        Tags = tags,
        Check = Check(
            HTTP = healthUrl,
            Interval = consulConfig.healthCheckInterval
        )
    )

    val prettyJson = Json { prettyPrint = true; encodeDefaults = true }
    log.info(
        """
        🚀 Consul registration payload:
        ${prettyJson.encodeToString(registration)}
        ---
        Will be sent to: ${consulConfig.scheme}://${consulConfig.consulHost}:${consulConfig.consulPort}/v1/agent/service/register
        """.trimIndent()
    )

    val client = createConsulHttpClient()
    val url = "${consulConfig.scheme}://${consulConfig.consulHost}:${consulConfig.consulPort}/v1/agent/service/register"

    launch {
        try {
            val success = tryRegisterWithConsul(client, url, registration, log)
            if (!success) {
                log.error("❌ Service was NOT registered with Consul after multiple attempts.")
            }
        } finally {
            client.close()
        }
    }
}

/**
 * Creates and configures the HttpClient for Consul requests.
 */
fun createConsulHttpClient() = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = false })
    }
}
