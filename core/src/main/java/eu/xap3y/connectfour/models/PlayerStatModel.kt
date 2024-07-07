package eu.xap3y.connectfour.models

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class PlayerStatModel(
    val uuid: String,
    val name: String,
    var wins: Int,
    var losses: Int,
    var draws: Int,
    var gamesPlayed: Int
)
