package eu.xap3y.connectfour.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerStatModel(
    val uuid: String,
    val name: String,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val gamesPlayed: Int
)
