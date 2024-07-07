package eu.xap3y.connectfour.utils

import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.models.ConfigModel
import eu.xap3y.connectfour.models.PlayerStatModel
import eu.xap3y.connectfour.models.StorageModel
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TexterObj
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.io.File
import kotlin.collections.HashMap

class ConfigLoader(private val plugin: ConnectFour) {

    val data: HashMap<String, PlayerStatModel> = hashMapOf()

    fun reload(){
        plugin.configManager.reloadConfig()
        plugin.prefix = plugin.config.getString("prefix", "&8[&6ConnectFour&8] &7") ?: "&8[&6ConnectFour&8] &7"
        plugin.texter = Texter(
            TexterObj(
                plugin.prefix,
                false,
                null
            )
        )

        plugin.configModel = ConfigModel(
            plugin.config.getInt("inviteTimeout", 60),
            plugin.config.getBoolean("tokenFallAnimation", true),
            plugin.config.getLong("tokenFallSpeed", 120L),
            plugin.config.getBoolean("inviterStarts", false),
            plugin.config.getBoolean("winRewards.enabled", false),
            plugin.config.getStringList("winRewards.commands"),
            plugin.config.getBoolean("hooks.placeholderAPI", true),
            plugin.config.getBoolean("hooks.miniPlaceholders", false),
            plugin.config.getBoolean("metrics", true),
            updates = plugin.config.getBoolean("updateCheck", true)
        )
    }

    fun loadData() {
        if (!plugin.dataFolder.exists()) return
        val dataFile = File(plugin.dataFolder, "storage.json")
        if (!dataFile.exists())
            plugin.saveResource("storage.json", false)

        val json = dataFile.readText()
        try {
            val parsed = Json.decodeFromString<StorageModel>(json)
            parsed.players.forEach {
                data[it.uuid] = it
            }
        } catch (e: Exception) {
            saveData()
        }

    }

    @OptIn(ExperimentalSerializationApi::class)
    fun saveData() {
        val dataFile = File(plugin.dataFolder, "storage.json")

        val prettyJson = Json {
            prettyPrint = true
            prettyPrintIndent = " "
        }

        val json: String = prettyJson.encodeToString(StorageModel(data.values.toList()))
        dataFile.writeText(json)
    }

    fun checkPlayer(player: Player) {
        if (!data.containsKey(player.uniqueId.toString())) {
            data[player.uniqueId.toString()] = PlayerStatModel(player.uniqueId.toString(), player.name, 0, 0, 0, 0)
        }
    }

    fun getPlayerStats(player: OfflinePlayer): PlayerStatModel? {
        //checkPlayer(player)
        return data[player.uniqueId.toString()]
    }

    fun getLeaderboard(): List<PlayerStatModel> {
        return data.values.sortedByDescending { it.wins }.take(10)

    }
}