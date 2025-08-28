package kz.mm.init.opeanapi

import io.ktor.server.application.*
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Top-level function: Call this to patch the OpenAPI YAML.
 * Only use in dev/build mode, before packaging the app!
 */
fun Application.patchAndOverwriteOpenApiResource() {
    val file = findOpenApiYamlFile() ?: return
    val yaml = Yaml()
    val openApi = readYamlAsMutableMap(yaml, file) ?: return

    patchInfo(openApi)
    patchServers(openApi)
    patchJwtSecurity(openApi)

    writeYamlToFile(yaml, openApi, file)
    println("✅ Patched info, servers, and Authorization. File: ${file.absolutePath}")
}

/**
 * Finds the openapi/documentation.yaml file from classloader resource (dev/build mode).
 */
private fun findOpenApiYamlFile(): File? {
    val resourceUrl = object {}.javaClass.classLoader.getResource("openapi/documentation.yaml")
    if (resourceUrl == null) {
        println("❌ Could not find openapi/documentation.yaml in resources.")
        return null
    }
    return File(resourceUrl.toURI())
}

/**
 * Reads YAML file into a MutableMap for patching.
 */
private fun readYamlAsMutableMap(yaml: Yaml, file: File): MutableMap<String, Any>? {
    return file.inputStream().use {
        val map = yaml.load<Any>(it)
        if (map is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            map as MutableMap<String, Any>
        } else if (map is Map<*, *>) {
            // Create a mutable copy if loaded as immutable
            @Suppress("UNCHECKED_CAST")
            map.toMutableMap() as MutableMap<String, Any>
        } else {
            println("❌ Failed to parse YAML as map.")
            null
        }
    }
}

/**
 * Patches the 'info' block.
 */
private fun patchInfo(openApi: MutableMap<String, Any>) {
    val customInfo = mapOf(
        "title" to "Recommendation Service API",
        "description" to "HTTP APIs for recommendations",
        "version" to "1.0.0",
        "contact" to mapOf("name" to "manki1337"),
        "license" to mapOf("name" to "MIT")
    )
    openApi["info"] = customInfo
}

/**
 * Patches the 'servers' block using ENV or fallback.
 */
private fun patchServers(openApi: MutableMap<String, Any>) {
    val serverUrl = System.getenv("MOVIE_MATE_RECOMMENDATION_SERVICE_OPENAPI_SERVER_URL")
        ?: "https://themanki.net"
    openApi["servers"] = listOf(mapOf("url" to serverUrl))
}

/**
 * Patches JWT/bearer authorization (securitySchemes and global security requirement).
 */
private fun patchJwtSecurity(openApi: MutableMap<String, Any>) {
    val securitySchemes = mapOf(
        "BearerAuth" to mapOf(
            "type" to "http",
            "scheme" to "bearer",
            "bearerFormat" to "JWT"
        )
    )
    val components = openApi.getOrPut("components") { mutableMapOf<String, Any>() } as MutableMap<String, Any>
    components["securitySchemes"] = securitySchemes
    openApi["components"] = components

    openApi["security"] = listOf(mapOf("BearerAuth" to emptyList<String>()))
}

/**
 * Writes the patched map back to the YAML file.
 */
private fun writeYamlToFile(yaml: Yaml, openApi: MutableMap<String, Any>, file: File) {
    file.writer().use {
        yaml.dump(openApi, it)
    }
}
