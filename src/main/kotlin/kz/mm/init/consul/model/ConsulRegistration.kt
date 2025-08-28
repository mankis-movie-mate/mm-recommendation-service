package kz.mm.init.consul.model

import kotlinx.serialization.Serializable

@Serializable
data class ConsulRegistration(
    val ID: String,
    val Name: String,
    val Address: String,
    val Port: Int,
    val Tags: List<String> = emptyList(),
    val Check: Check? = null
)