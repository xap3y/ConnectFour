package eu.xap3y.connectfour.models

data class ConfigModel(
    val inviteTimeout: Int,
    val tokenFallAnimation: Boolean,
    val tokenFallSpeed: Long,
    val inviterStart: Boolean,
    val winRewardsEnable: Boolean,
    val winRewards: List<String>? = null,
    val hookPapi: Boolean,
    val hookMiniPlaceholders: Boolean
)