package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XSound
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.ps
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TextModifier
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
                plugin.texter.response(player, LangManager.getStringPrefixed("invite_expired"), TextModifier(false))
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
        val lineHeader = LangManager.getString("invite.header")
        val line = LangManager.getString("invite.footer")
        val line2 = LangManager.getStringPrefixed("invite.text", hashMapOf(
            "player" to player.name
        ))

        val button1 = LangManager.getString("invite.accept.text") ?: "&7[&aACCEPT&7]"
        val button2 = LangManager.getString("invite.reject.text") ?: "&7[&cREJECT&7]"
        val button1Hover = LangManager.getString("invite.accept.hover") ?: "&aClick to accept"
        val button2Hover = LangManager.getString("invite.reject.hover") ?: "&cClick to reject"

        val textButtons = LangManager.getString("invite.buttons")?.replace("{button1}", button1)?.replace("{button2}", button2) ?: "$button1    $button2"
        val recreateMid = Texter.centered(textButtons)

        val spaces = recreateMid.indexOfFirst { it == '&' }
        val spacesText = recreateMid.substring(0, spaces)

        if (ConnectFour.useTextComponents.not()) {
            val text = LangManager.getStringPrefixed("invite.bukkit_invite", hashMapOf(
                "player" to player.name
            ))
            plugin.texter.response(target, text, TextModifier(false))
        } else if (ConnectFour.useTextComponents && ConnectFour.useNew) {
            plugin.texter.console(recreateMid)
            // the created spaces before the first text so like "           &7[&aACCEPT&7]   &7[&cREJECT&7]"
            // get the first spaces
            // get how many spaces before first text
            val comp = ComponentBuilder(spacesText)
                .append(ComponentBuilder(Texter.colored(button1))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored(button1Hover)))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept ${player.name}"))
                    .create()
                )

                .append(ComponentBuilder("    ").create())

                .append(ComponentBuilder(Texter.colored(button2))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored(button2Hover)))))
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject ${player.name}"))
                    .create()
                )
                .create()
            try {
                if (lineHeader != null) target.sendMessage(Texter.colored(lineHeader))
                target.sendMessage(Texter.colored(Texter.centered(line2)))
                target.spigot().sendMessage(*comp)
                if (line != null) target.sendMessage(Texter.colored(line))
            } catch (e: Exception) {
                plugin.texter.response(target, LangManager.getStringPrefixed("invite_err"), TextModifier(false))
            }
            /*target.sendMessage(*comp)*/
        } else {
            val set: Set<TextComponent> = setOf(
                TextComponent(spacesText),
                TextComponent(Texter.colored(button1)).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored(button1Hover))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf accept ${player.name}")
                },
                TextComponent("    "),
                TextComponent(Texter.colored(button2)).apply {
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(Texter.colored(button2Hover))))
                    clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cf reject ${player.name}")
                }
            )
            if (lineHeader != null) target.sendMessage(Texter.colored(lineHeader))
            target.sendMessage(Texter.colored(Texter.centered(line2)))
            target.spigot().sendMessage(*set.toTypedArray())
            if (line != null) target.sendMessage(Texter.colored(line))
        }

        target.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)
        player.ps(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)

        System.gc()
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
        plugin.texter.response(data.inviter, LangManager.getStringPrefixed("invite_accept_other", hashMapOf(
            "player" to player.name
        )), TextModifier(false))
        plugin.texter.response(player, LangManager.getStringPrefixed("invite_accept_self"), TextModifier(false))
    }

    fun reject(player: Player, inviter: Player) {
        val data = inviteMapper.firstOrNull { it.invited.uniqueId == player.uniqueId && it.inviter.uniqueId == inviter.uniqueId } ?: return

        Bukkit.getScheduler().cancelTask(data.taskId)
        inviteMapper.remove(data)
        data.inviter.ps(XSound.ENTITY_VILLAGER_NO)
        plugin.texter.response(data.inviter, LangManager.getStringPrefixed("invite_reject_other", hashMapOf(
            "player" to player.name
        )), TextModifier(false))
        plugin.texter.response(player, LangManager.getStringPrefixed("invite_reject_self"), TextModifier(false))
        // Send rejection message
    }
}