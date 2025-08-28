package kz.mm.init.consul.model

import kotlinx.serialization.Serializable

@Serializable
data class Check(
    val HTTP: String,
    val Interval: String,
    val DeregisterCriticalServiceAfter: String = "1m"
)