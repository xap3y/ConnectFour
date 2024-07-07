package eu.xap3y.connectfour.utils.hooks

import eu.xap3y.connectfour.ConnectFour
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class ConnectPlaceholderApi(private val plugin: ConnectFour): PlaceholderExpansion() {

    override fun getIdentifier(): String = "connectfour"

    override fun getAuthor(): String = "XAP3Y"

    override fun getVersion(): String = "1.0.0"

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        return when (params) {
            "version" -> "1.0.0"
            "wins" -> (plugin.configLoader.data[player?.uniqueId.toString()]?.wins ?: 0).toString()
            "losses" -> (plugin.configLoader.data[player?.uniqueId.toString()]?.losses ?: 0).toString()
            "draws" -> (plugin.configLoader.data[player?.uniqueId.toString()]?.draws ?: 0).toString()
            "played" -> (plugin.configLoader.data[player?.uniqueId.toString()]?.gamesPlayed ?: 0).toString()
            "winrate" -> run {
                val stats = plugin.configLoader.getPlayerStats(player ?: return "0") ?: return "0"
                val winRate = if (stats.gamesPlayed == 0) 0.0 else (stats.wins.toDouble() / stats.gamesPlayed.toDouble()) * 100
                return winRate.toInt().toString()
            }
            else -> null
        }
    }
}