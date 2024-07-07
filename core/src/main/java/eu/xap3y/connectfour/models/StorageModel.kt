package eu.xap3y.connectfour.models

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class StorageModel(
    val players: List<PlayerStatModel>
)