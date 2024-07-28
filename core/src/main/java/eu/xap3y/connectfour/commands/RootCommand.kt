package eu.xap3y.connectfour.commands

import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.utils.LangManager
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TextModifier
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class RootCommand(private val plugin: ConnectFour = ConnectFour.instance) {

    //private val onlyPlayer: String = "&cYou must be a player to use this command!"

    @Command("connectfour|cf")
    @CommandDescription("Connect Four main command")
    fun root(sender: CommandSender) {
        //plugin.texter.response(sender, "&fRunning &bConnect4 &fversion &7${plugin.version}")
        val header = LangManager.getString("help.header")
        val footer = LangManager.getString("help.footer")

        val list: MutableList<String> = mutableListOf()

        if (header != null) {
            list.add(header)
        }

        val entry = LangManager.getString("help.entry") ?: " &9&l➢ &7{command} &8- &f{description}"

        val commandMap = hashMapOf(
            "/cf stats [player]" to (LangManager.getString("help.descriptions.stats") ?: "View stats"),
            "/cf leaderboard" to (LangManager.getString("help.descriptions.leaderboard") ?: "View leaderboard"),
            "/cf invite <player>" to (LangManager.getString("help.descriptions.invite") ?: "Invite a player"),
            "/cf accept <player>" to (LangManager.getString("help.descriptions.accept") ?: "Accept an invite"),
            "/cf reject <player>" to (LangManager.getString("help.descriptions.reject") ?: "Reject an invite"),
        )

        val adminCommandMap = hashMapOf(
            "/cf reload" to (LangManager.getString("help.descriptions.reload") ?: "Reload the config")
        )

        commandMap.forEach { (key, value) ->
            list.add(entry.replace("{command}", key).replace("{description}", value))
        }

        if (sender.isOp || sender.hasPermission("connectfour.*") || sender.hasPermission("connectfour.admin")) {
            adminCommandMap.forEach { (key, value) ->
                list.add(entry.replace("{command}", key).replace("{description}", value))
            }
        }

        if (footer != null) list.add(footer)

        list.forEach { plugin.texter.response(sender, it, TextModifier(false)) }
    }

    @Command("connectfour|cf stats [player]")
    @CommandDescription("Connect Four stats command")
    //@Permission(value = ["connectfour.*", "connectfour.stats"], mode = Permission.Mode.ANY_OF)
    fun stats(
        sender: CommandSender,
        @Argument("player") player: OfflinePlayer?
    ) {

        if (player == null && sender !is Player)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("wrong_usage", hashMapOf("usage" to "/cf stats <player>")), TextModifier(false))

        val stats = plugin.configLoader.getPlayerStats(player ?: sender as Player)

        if (stats == null)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("no_stats_found_player"), TextModifier(false))

        val winRate = (if (stats.gamesPlayed == 0) 0.0 else stats.wins.toDouble() / stats.gamesPlayed.toDouble() * 100).toInt()

        val header = LangManager.getString("stats.header")
        val footer = LangManager.getString("stats.footer")

        val list = LangManager.getListPrefixed("stats.list", hashMapOf(
            "wins" to stats.wins.toString(),
            "losses" to stats.losses.toString(),
            "draws" to stats.draws.toString(),
            "winrate" to winRate.toString(),
            "gamesPlayed" to stats.gamesPlayed.toString()
        )).toMutableList()

        val centered = LangManager.getBool("stats.centered")

        val temp: MutableList<String> = mutableListOf()

        val playerName = if (player?.name != null) player.name ?: "" else sender.name

        if (centered && sender is Player) {
            if (header != null) temp.add(Texter.centered(header))
            temp.add(Texter.centered(LangManager.getStringPrefixed("stats.text", hashMapOf("player" to playerName))))
            list.forEach {
                temp.add(Texter.centered(it))
            }
            if (footer != null) temp.add(Texter.centered(footer))
        } else {
            if (header != null) temp.add(header)
            temp.add(LangManager.getStringPrefixed("stats.text", hashMapOf("player" to playerName)))
            temp.addAll(list)
            if (footer != null) temp.add(footer)
        }

        temp.forEach { plugin.texter.response(sender, it, TextModifier(false)) }
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
            return plugin.texter.response(sender, LangManager.getStringPrefixed("only_player"), TextModifier(false))

        else if (player == null)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("wrong_usage", hashMapOf("usage" to "/cf invite <player>")), TextModifier(false))

        else if (player == sender)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("invite_self_err"), TextModifier(false))

        else if (plugin.inviteManager.inviter(sender))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("someone_already_invited"), TextModifier(false))

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("player_already_playing"), TextModifier(false))

        else if (plugin.gameManager.isPlaying(sender))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("self_already_playing"), TextModifier(false))

        if (plugin.inviteManager.isInvitedBy(player, sender))
            plugin.inviteManager.accept(sender, player)

        else if (plugin.inviteManager.isInvitedBy(sender, player)) {
            return plugin.texter.response(sender, LangManager.getStringPrefixed("player_already_invited"), TextModifier(false))
        }

        else {
            plugin.inviteManager.invite(sender, player)
            plugin.texter.response(sender, LangManager.getStringPrefixed("invite_sent", hashMapOf("player" to player.name)), TextModifier(false))
        }
    }

    @Command("connectfour|cf accept [player]")
    @CommandDescription("Connect Four accept invite command")
    //@Permission(value = ["connectfour.*", "connectfour.accept"], mode = Permission.Mode.ANY_OF)
    fun accept(
        sender: CommandSender,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("only_player"), TextModifier(false))

        else if (player == null)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("wrong_usage", hashMapOf("usage" to "/cf accept <player>")), TextModifier(false))

        else if (player.uniqueId == sender.uniqueId)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("self_accept_err"), TextModifier(false))

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("player_already_playing"), TextModifier(false))

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("no_invites_from_player"), TextModifier(false))

        plugin.inviteManager.accept(sender, player)
    }

    @Command("connectfour|cf reject [player]")
    @CommandDescription("Connect Four reject invite command")
    //@Permission(value = ["connectfour.*", "connectfour.reject"], mode = Permission.Mode.ANY_OF)
    fun reject(
        sender: CommandSender,
        @Argument("player") player: Player?
    ) {

        if (sender !is Player)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("only_player"), TextModifier(false))

        else if (player == null)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("wrong_usage", hashMapOf("usage" to "/cf reject <player>")), TextModifier(false))

        else if (player.uniqueId == sender.uniqueId)
            return plugin.texter.response(sender, LangManager.getStringPrefixed("self_reject_err"), TextModifier(false))

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("no_invites"), TextModifier(false))

        else if (plugin.gameManager.isPlaying(player))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("player_already_playing"), TextModifier(false))

        else if (!plugin.inviteManager.isInvitedBy(player, sender))
            return plugin.texter.response(sender, LangManager.getStringPrefixed("no_invites_from_player"), TextModifier(false))

        plugin.inviteManager.reject(sender, player)
    }

    @Command("connectfour|cf leaderboard")
    @CommandDescription("Connect Four leaderboard command")
    //@Permission(value = ["connectfour.*", "connectfour.leaderboard"], mode = Permission.Mode.ANY_OF)
    fun leaderBoard(
        sender: CommandSender
    ) {
        val list = plugin.configLoader.getLeaderboard()

        if (list.isEmpty())
            return plugin.texter.response(sender, LangManager.getStringPrefixed("no_leaderboard_entries"), TextModifier(false))

        var setOfSymbols = LangManager.getListPrefixed("leaderboard.symbols").toSet()

        if (list.size > setOfSymbols.size)
            setOfSymbols = setOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩")

        /*val setOfSymbols = setOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩")*/

        val header = LangManager.getString("leaderboard.header")
        val footer = LangManager.getString("leaderboard.footer")

        if (header != null) plugin.texter.response(sender, header, TextModifier(false))

        val entry = LangManager.getStringPrefixed("leaderboard.entry")

        list.onEachIndexed { index, it ->
            val symbol = setOfSymbols.elementAt(index)
            val winRate = (if (it.gamesPlayed == 0) 0.0 else it.wins.toDouble() / it.gamesPlayed.toDouble() * 100).toInt()
            val winRateTextColored = when {
                winRate >= 70 -> "&a$winRate%"
                winRate >= 50 -> "&e$winRate%"
                winRate <= 10 -> "&4$winRate%"
                else -> "&c$winRate%"
            }
            //plugin.texter.response(sender, "     &e$symbol &7${it.name} &f- &9${it.wins} wins  &7(&fWinRate: $winRateTextColored&7)", TextModifier(false))
            plugin.texter.response(sender, entry.replace("{symbol}", symbol).replace("{player}", it.name).replace("{wins}", it.wins.toString()).replace("{winrate}", winRateTextColored), TextModifier(false))
        }
        if (footer != null) plugin.texter.response(sender, footer, TextModifier(false))
    }

    @Command("connectfour|cf reload")
    @CommandDescription("Connect Four reload config command")
    @Permission(value = ["connectfour.*", "connectfour.reload"], mode = Permission.Mode.ANY_OF)
    fun reload(sender: CommandSender) {
        plugin.configLoader.reload()
        LangManager.reload()

        plugin.texter.response(sender, LangManager.getStringPrefixed("config_reload"), TextModifier(false))
    }
}