package eu.xap3y.connectfour.utils.hooks

import eu.xap3y.connectfour.ConnectFour
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.expansion
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class ConnectMiniPlaceholders(private val plugin: ConnectFour) {

    fun register() {
        val expansion = expansion("connectfour") {
            audiencePlaceholder("version") { p, _, _ ->
                return@audiencePlaceholder Component.text(plugin.version).asInsertingTag()
            }
            audiencePlaceholder("wins") { p, _, _ ->
                val player = p as Player
                return@audiencePlaceholder Component.text((plugin.configLoader.data[player.uniqueId.toString()]?.wins ?: 0).toString()).asInsertingTag()
            }
            audiencePlaceholder("losses") { p, _, _ ->
                val player = p as Player
                return@audiencePlaceholder Component.text((plugin.configLoader.data[player.uniqueId.toString()]?.losses ?: 0).toString()).asInsertingTag()
            }
            audiencePlaceholder("draws") { p, _, _ ->
                val player = p as Player
                return@audiencePlaceholder Component.text((plugin.configLoader.data[player.uniqueId.toString()]?.draws ?: 0).toString()).asInsertingTag()
            }
            audiencePlaceholder("played") { p, _, _ ->
                val player = p as Player
                return@audiencePlaceholder Component.text((plugin.configLoader.data[player.uniqueId.toString()]?.gamesPlayed ?: 0).toString()).asInsertingTag()
            }
        }

        expansion.register()
    }
}