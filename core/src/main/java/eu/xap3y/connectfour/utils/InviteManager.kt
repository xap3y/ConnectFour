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

data class Invite(
    val inviter: Player,
    val invited: Player,
    var taskId: Int
)

class InviteManager(private val plugin: ConnectFour = ConnectFour.instance) {

    //private val inviteMapper: MutableMap<Player, Pair<Int, Player>> = mutableMapOf()

    private val inviteMapper: MutableList<Invite> = mutableListOf()

    // NMS is painful
    fun invite(player: Player, target: Player) {
        inviteMapper.add(Invite(
            player,
            target,
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                inviteMapper.removeIf { it.invited.uniqueId == target.uniqueId && it.inviter.uniqueId == player.uniqueId }
                plugin.texter.response(player, "&cThe invitation has expired!")
            }, 20L * plugin.configModel.inviteTimeout).taskId
        ))

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
        if (ConnectFour.useTextComponents.not()) {
            val text = "&aYou have been invited to a game by &e${player.name}&7. Type &a/cf accept ${player.name} &7to accept or &c/cf reject ${player.name} &7to reject"
            plugin.texter.response(target, text)
        } else if (ConnectFour.useTextComponents && ConnectFour.useNew) {
            val lineHeader = "&a&m-+-----------------&r&a[&bConnectFour&a]&a&m-----------------+-"
            val line = "&a&m-+----------------------------------------------+-"
            val line2 = "&fYou have been invited to a game by &e&l${player.name}"

            val recreateMid = Texter.centered("&7[&aACCEPT&7]    &7[&cREJECT&7]")
            plugin.texter.console(recreateMid)
            // the created spaces before the first text so like "           &7[&aACCEPT&7]   &7[&cREJECT&7]"
            // get the first spaces
            val spaces = recreateMid.indexOfFirst { it == '&' }
            val spacesText = recreateMid.substring(0, spaces)
            // get how many spaces before first text
            val comp = ComponentBuilder(spacesText)
                .append(ComponentBuilder(Texter.colored("&7[&aACCEPT&7]"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&aClick to accept")))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept ${player.name}"))
                    .create()
                )

                .append(ComponentBuilder("    ").create())

                .append(ComponentBuilder(Texter.colored("&7[&cREJECT&7]"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&cClick to reject")))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject ${player.name}"))
                    .create()
                )
                .create()
            try {
                target.sendMessage(Texter.colored(lineHeader))
                target.sendMessage(Texter.colored(Texter.centered(line2)))
                target.spigot().sendMessage(*comp)
                target.sendMessage(Texter.colored(line))
            } catch (e: Exception) {
                plugin.texter.response(target, "&cAn error occurred while sending the invitation")
            }
            /*target.sendMessage(*comp)*/
        } else {
            val recreateMid = Texter.centered("&7[&aACCEPT&7]    &7[&cREJECT&7]")
            val spaces = recreateMid.indexOfFirst { it == '&' }
            val spacesText = recreateMid.substring(0, spaces)

            val lineHeader = "&a&m-+-----------------&r&a[&bConnectFour&a]&a&m-----------------+-"
            val line = "&a&m-+----------------------------------------------+-"
            val line2 = "&fYou have been invited to a game by &e&l${player.name}"
            val set: Set<TextComponent> = setOf(
                TextComponent(spacesText),
                TextComponent(Texter.colored("&7[&aACCEPT&7]")).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&aClick to accept"))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept ${player.name}")
                },
                TextComponent("    "),
                TextComponent(Texter.colored("&7[&cREJECT&7]")).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored("&cClick to reject"))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject ${player.name}")
                }
            )
            target.sendMessage(Texter.colored(lineHeader))
            target.sendMessage(Texter.colored(Texter.centered(line2)))
            target.spigot().sendMessage(*set.toTypedArray())
            target.sendMessage(Texter.colored(line))
        }

        target.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)
        player.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)
    }

    /*fun isInvited(player: Player): Boolean {
        return inviteMapper.values.any { it.second == player } || inviteMapper.keys.contains(player)
    }*/

    fun inviter(player: Player): Boolean {
        return inviteMapper.any { it.inviter.uniqueId == player.uniqueId }
    }

    fun isInvitedBy(inviter: Player, player: Player): Boolean {
        //return inviteMapper[inviter]?.second?.uniqueId == player.uniqueId
        return inviteMapper.any { it.inviter.uniqueId == inviter.uniqueId && it.invited.uniqueId == player.uniqueId }
    }

    fun accept(player: Player, inviter: Player) {
        //val data: MutableMap.MutableEntry<Player, Pair<Int, Player>> = inviteMapper.entries.firstOrNull { it.value.second == player } ?: return

       /* val data = inviteMapper.entries.firstOrNull { it.value.second.uniqueId == player.uniqueId && it.key.uniqueId == inviter.uniqueId } ?: return*/
        val data = inviteMapper.firstOrNull { it.invited.uniqueId == player.uniqueId && it.inviter.uniqueId == inviter.uniqueId } ?: return

        Bukkit.getScheduler().cancelTask(data.taskId)
        inviteMapper.remove(data)
        // Start game
        plugin.gameManager.startGame(data.inviter, player)
        plugin.texter.response(data.inviter, "&e${player.name} &ahas accepted the invitation")
        plugin.texter.response(player, "&aYou have accepted the invitation!")
    }

    fun reject(player: Player, inviter: Player) {
        val data = inviteMapper.firstOrNull { it.invited.uniqueId == player.uniqueId && it.inviter.uniqueId == inviter.uniqueId } ?: return

        Bukkit.getScheduler().cancelTask(data.taskId)
        inviteMapper.remove(data)
        data.inviter.ps(XSound.ENTITY_VILLAGER_NO)
        plugin.texter.response(data.inviter, "&e${player.name} &chas rejected the invitation!")
        plugin.texter.response(player, "&cYou have rejected the invitation!")
        // Send rejection message
    }
}