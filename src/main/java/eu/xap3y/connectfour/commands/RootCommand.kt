package eu.xap3y.connectfour.commands

import eu.xap3y.connectfour.ConnectFour
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class RootCommand(private val plugin: ConnectFour = ConnectFour.instance) {

    private val onlyPlayer: String = "&cYou must be a player to use this command!"

    @Command("connectfour|cf")
    @CommandDescription("Connect Four main command")
    fun root(sender: CommandSender) {
        plugin.texter.response(sender, "&fRunning &bConnect4 &fversion &7${plugin.version}")
    }

    @Command("connectfour|cf stats [player]")
    @CommandDescription("Connect Four stats command")
    @Permission(value = ["connectfour.*", "connectfour.stats"], mode = Permission.Mode.ANY_OF)
    fun stats(
        sender: CommandSender,
        @Argument("player") player: OfflinePlayer?
    ) {

        if (player == null && sender !is Player)
            return plugin.texter.response(sender, "&cWrong usage! &8(&7/cf stats <player>&8)")
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

        plugin.inviteManager.invite(sender, player)
        plugin.texter.response(sender, "&aInvite sent to &7${player.name}!")
    }

    @Command("connectfour|cf accept")
    @CommandDescription("Connect Four accept invite command")
    //@Permission(value = ["connectfour.*", "connectfour.accept"], mode = Permission.Mode.ANY_OF)
    fun accept(sender: CommandSender) {

        if (sender !is Player)
            return plugin.texter.response(sender, onlyPlayer)

        else if (!plugin.inviteManager.isInvited(sender))
            return plugin.texter.response(sender, "&cYou don't have any invites!")

        plugin.inviteManager.accept(sender)

        // Implement accept logic
    }

    @Command("connectfour|cf reject")
    @CommandDescription("Connect Four reject invite command")
    //@Permission(value = ["connectfour.*", "connectfour.reject"], mode = Permission.Mode.ANY_OF)
    fun reject(sender: CommandSender) {

        if (sender !is Player)
            return plugin.texter.response(sender, onlyPlayer)

        else if (!plugin.inviteManager.isInvited(sender))
            return plugin.texter.response(sender, "&cYou don't have any invites!")

        plugin.inviteManager.reject(sender)
    }
}