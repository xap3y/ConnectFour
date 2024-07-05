package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XSound
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.ps
import eu.xap3y.xalib.managers.Texter
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

class InviteManager(private val plugin: ConnectFour = ConnectFour.instance) {

    private val inviteMapper: MutableMap<Player, Pair<Int, Player>> = mutableMapOf()

    // NMS is painful
    fun invite(player: Player, target: Player) {
        inviteMapper[player] = Pair(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, Runnable {
            inviteMapper.remove(player)
            plugin.texter.response(player, "&cThe invitation has expired!")
        }, 20L * 60), target)

        // send clickable accept message
        // net.kyori:adventure-api for 1.16+
        /*if (!ConnectFour.useOld) {
            val map: LinkedHashMap<String, FormatterModifiers?> = linkedMapOf(
                prefix to null,
                "&7You have been invited to a game by &6${player.name}&7. " to null,
                "&7[&aACCEPT&7]" to FormatterModifiers(
                    hoverText = "&aClick to accept",
                    clickAction = ClickEvent.runCommand("/cf accept")
                ),
                " " to null,
                "&7[&cREJECT&7]" to FormatterModifiers(
                    hoverText = "&cClick to reject",
                    clickAction = ClickEvent.runCommand("/cf reject")
                )
            )
            val text = Texter.formatOneLine(map)
            target.sendMessage(text)
        }*/
        val prefix: String = plugin.prefix
        if (ConnectFour.useTextComponents.not()) {
            val text = "&aYou have been invited to a game by &e${player.name}&7. Type &a/cf accept &7to accept or &c/cf reject &7to reject"
            plugin.texter.response(target, text)
        } else if (ConnectFour.useTextComponents && ConnectFour.useNew) {
            val comp = ComponentBuilder(Texter.colored(prefix))
                .append(ComponentBuilder(Texter.colored("&fYou have been invited to a game by &e${player.name}&7. ")).create())
                .append(ComponentBuilder(Texter.colored("&7[&aACCEPT&7]"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&aClick to accept")))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept"))
                    .create()
                )

                .append(ComponentBuilder(" ").create())

                .append(ComponentBuilder(Texter.colored("&7[&cREJECT&7]"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&cClick to reject")))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject"))
                    .create()
                )
                .create()
            try {
                target.spigot().sendMessage(*comp)
            } catch (e: Exception) {
                plugin.texter.response(target, "&cAn error occurred while sending the invitation")
            }
            /*target.sendMessage(*comp)*/
        } else {
            val set: Set<TextComponent> = setOf(
                TextComponent(Texter.colored(prefix)),
                TextComponent(Texter.colored("&fYou have been invited to a game by &e${player.name}&7. ")),
                TextComponent(Texter.colored("&7[&aACCEPT&7]")).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&aClick to accept"))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept")
                },
                TextComponent(" "),
                TextComponent(Texter.colored("&7[&cREJECT&7]")).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&cClick to reject"))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject")
                }
            )
            target.spigot().sendMessage(*set.toTypedArray())
        }

        target.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)
        player.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)
    }

    fun isInvited(player: Player): Boolean {
        return inviteMapper.values.any { it.second == player } || inviteMapper.keys.contains(player)
    }

    fun accept(player: Player) {
        val data: MutableMap.MutableEntry<Player, Pair<Int, Player>> = inviteMapper.entries.firstOrNull { it.value.second == player } ?: return

        Bukkit.getScheduler().cancelTask(data.value.first)
        inviteMapper.remove(data.key)
        // Start game
        plugin.gameManager.startGame(data.key, player)
        plugin.texter.response(data.key, "&e${player.name} &ahas accepted the invitation")
        plugin.texter.response(player, "&aYou have accepted the invitation!")
    }

    fun reject(player: Player) {
        val data: MutableMap.MutableEntry<Player, Pair<Int, Player>> = inviteMapper.entries.firstOrNull { it.value.second == player } ?: return

        Bukkit.getScheduler().cancelTask(data.value.first)
        inviteMapper.remove(data.key)
        data.key.ps(XSound.ENTITY_VILLAGER_NO)
        plugin.texter.response(data.key, "&e${player.name} &chas rejected the invitation!")
        plugin.texter.response(player, "&cYou have rejected the invitation!")
        // Send rejection message
    }
}