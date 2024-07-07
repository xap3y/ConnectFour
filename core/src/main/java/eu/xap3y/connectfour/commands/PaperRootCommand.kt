package eu.xap3y.connectfour.commands

import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TextModifier
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

// 1.20.6+ Replica or RootCommand but with CommandSourceStack instead of CommandSender
// This is because org.incendo.cloud framework command parser
class PaperRootCommand(private val plugin: ConnectFour = ConnectFour.instance) {

    private val onlyPlayer: String = "&cYou must be a player to use this command!"

    @Command("connectfour|cf")
    @CommandDescription("Connect Four main command")
    fun root(sender: CommandSourceStack) {
        //plugin.texter.response(sender, "&fRunning &bConnect4 &fversion &7${plugin.version}")
        val list: MutableList<String> = mutableListOf(
            "&a&m-+-----------------&r&a[&bConnectFour&a]&a&m-----------------+-",
            " &9&l➢ &7/cf stats [player] &8- &fView stats",
            " &9&l➢ &7/cf invite <player> &8- &fInvite a player",
            " &9&l➢ &7/cf accept <player> &8- &fAccept an invite",
            " &9&l➢ &7/cf reject <player> &8- &fReject an invite",
        )
        if (sender.sender.isOp || sender.sender.hasPermission("connectfour.*") || sender.sender.hasPermission("connectfour.admin"))
            list.add(" &9&l➢ &7/cf reload&8- &fReload the config")

        list.add("&a&m-+----------------------------------------------+-")

        list.forEach { plugin.texter.response(sender.sender, it, TextModifier(false)) }
    }

    @Command("connectfour|cf stats [player]")
    @CommandDescription("Connect Four stats command")
    //@Permission(value = ["connectfour.*", "connectfour.stats"], mode = Permission.Mode.ANY_OF)
    fun stats(
        sender: CommandSourceStack,
        @Argument("player") player: OfflinePlayer?
    ) {

        if (player == null && sender !is Player)
            return plugin.texter.response(sender.sender, "&cWrong usage! &8(&7/cf stats <player>&8)")

        val stats = plugin.configLoader.getPlayerStats(player ?: sender as Player)

        if (stats == null)
            return plugin.texter.response(sender.sender, "&cNo stats found for this player!")

        val winRate = (if (stats.gamesPlayed == 0) 0.0 else (stats.wins.toDouble() / stats.gamesPlayed.toDouble()) * 100).toInt()

        val list = if (sender !is Player) listOf(
            "&a&m-+-----------------&r&a[&bConnectFour&a]&a&m-----------------+-",
            "&e&l${player?.name ?: sender.sender.name} &fstatistics",
            " &9&l➢ &7Wins: &9${stats.wins}",
            " &9&l➢ &7Losses: &9${stats.losses}",
            " &9&l➢ &7Draws: &9${stats.draws}",
            " &9&l➢ &7Win rate: &9$winRate%",
            " &9&l➢ &7Games played: &9${stats.gamesPlayed}",
            "&a&m-+----------------------------------------------+-"
        ) else listOf(
            Texter.centered("&a&m-+----------------&r&a[&bConnectFour&a]&a&m----------------+-"),
            Texter.centered("&e&l${player?.name ?: sender.name} &fstatistics"),
            Texter.centered("&7Wins: &9${stats.wins}"),
            Texter.centered("&7Losses: &9${stats.losses}"),
            Texter.centered("&7Draws: &9${stats.draws}"),
            Texter.centered("&7Win rate: &9$winRate%"),
            Texter.centered("&7Games played: &9${stats.gamesPlayed}"),
            Texter.centered("&a&m-+-------------------------------------------+-")
        )

        list.forEach { plugin.texter.response(sender.sender, it, TextModifier(false)) }
    }

    /*@Command("connectfour|cf inviteOld [player]")
    @CommandDescription("Connect Four invite player command")
    //@Permission(value = ["connectfour.*", "connectfour.invite"], mode = Permission.Mode.ANY_OF)
    fun invite(
        sender: CommandSender,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender, onlyPlayer)

        else if (player == null)
            return plugin.texter.response(sender, "&cWrong usage! &8(&7/cf invite <player>&8)")

        else if (player == sender)
            return plugin.texter.response(sender, "&cYou can't invite yourself!")

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, "&cThis player is already playing a game!")

        else if (plugin.gameManager.isPlaying(sender))
            return plugin.texter.response(sender, "&cYou are already playing a game!")

        plugin.gameManager.openGui(sender, player)
        // Implement invite logic
    }*/

    @Command("connectfour|cf invite [player]")
    @CommandDescription("Connect Four invite player command")
    //@Permission(value = ["connectfour.*", "connectfour.invite"], mode = Permission.Mode.ANY_OF)
    fun invite(
        sender: CommandSourceStack,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender.sender, onlyPlayer)

        else if (player == null)
            return plugin.texter.response(sender, "&cWrong usage! &8(&7/cf invite <player>&8)")

        else if (player == sender)
            return plugin.texter.response(sender, "&cYou can't invite yourself!")

        else if (plugin.inviteManager.inviter(sender))
            return plugin.texter.response(sender, "&cYou already invited someone!")

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, "&cThis player is already playing a game!")

        else if (plugin.gameManager.isPlaying(sender))
            return plugin.texter.response(sender, "&cYou are already playing a game!")

        if (plugin.inviteManager.isInvitedBy(player, sender))
            plugin.inviteManager.accept(sender, player)

        else if (plugin.inviteManager.isInvitedBy(sender, player)) {
            return plugin.texter.response(sender, "&cYou already invited this player!")
        }

        else {
            plugin.inviteManager.invite(sender, player)
            plugin.texter.response(sender, "&aInvite sent to &e&l${player.name}")
        }
    }

    @Command("connectfour|cf accept [player]")
    @CommandDescription("Connect Four accept invite command")
    //@Permission(value = ["connectfour.*", "connectfour.accept"], mode = Permission.Mode.ANY_OF)
    fun accept(
        sender: CommandSourceStack,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender.sender, onlyPlayer)

        else if (player == null)
            return plugin.texter.response(sender, "&cWrong usage! &8(&7/cf accept <player>&8)")

        else if (player.uniqueId == sender.uniqueId)
            return plugin.texter.response(sender, "&cYou can't accept your self!")

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, "&cThis player is already playing a game!")

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, "&cYou don't have any invites from this player!")

        plugin.inviteManager.accept(sender, player)
    }

    @Command("connectfour|cf reject [player]")
    @CommandDescription("Connect Four reject invite command")
    //@Permission(value = ["connectfour.*", "connectfour.reject"], mode = Permission.Mode.ANY_OF)
    fun reject(
        sender: CommandSourceStack,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender.sender, onlyPlayer)

        else if (player == null)
            return plugin.texter.response(sender, "&cWrong usage! &8(&7/cf reject <player>&8)")

        else if (player.uniqueId == sender.uniqueId)
            return plugin.texter.response(sender, "&cYou can't reject your self!")

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, "&cYou don't have any invites!")

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, "&cThis player is already playing a game!")

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, "&cYou don't have any invites from this player!")

        plugin.inviteManager.reject(sender, player)
    }

    @Command("connectfour|cf reload")
    @CommandDescription("Connect Four reload config command")
    @Permission(value = ["connectfour.*", "connectfour.reload"], mode = Permission.Mode.ANY_OF)
    fun reload(sender: CommandSourceStack) {
        plugin.configLoader.reload()

        plugin.texter.response(sender.sender, "&aConfig reloaded!")
    }
}