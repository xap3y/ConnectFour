package eu.xap3y.connectfour.command;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.manager.LangManager;
import eu.xap3y.connectfour.service.Texter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.*;

public class RootCommand {

    @Command("connectfour|cf")
    @CommandDescription("Connect Four main command")
    public void root(CommandSender sender) {
        String header = LangManager.getString("help.header");
        String footer = LangManager.getString("help.footer");

        List<String> list = new ArrayList<>();

        if (header != null) list.add(header);

        String entry = LangManager.getString("help.entry");
        if (entry == null) entry = " &9&l➢ &7{command} &8- &f{description}";

        Map<String, String> commandMap = new LinkedHashMap<>();
        commandMap.put("/cf stats [player]", Optional.ofNullable(LangManager.getString("help.descriptions.stats")).orElse("View stats"));
        commandMap.put("/cf leaderboard", Optional.ofNullable(LangManager.getString("help.descriptions.leaderboard")).orElse("View leaderboard"));
        commandMap.put("/cf invite <player>", Optional.ofNullable(LangManager.getString("help.descriptions.invite")).orElse("Invite a player"));
        commandMap.put("/cf accept <player>", Optional.ofNullable(LangManager.getString("help.descriptions.accept")).orElse("Accept an invite"));
        commandMap.put("/cf reject <player>", Optional.ofNullable(LangManager.getString("help.descriptions.reject")).orElse("Reject an invite"));

        Map<String, String> adminCommandMap = new LinkedHashMap<>();
        adminCommandMap.put("/cf reload", Optional.ofNullable(LangManager.getString("help.descriptions.reload")).orElse("Reload the config"));

        for (Map.Entry<String, String> e : commandMap.entrySet()) {
            list.add(entry.replace("{command}", e.getKey()).replace("{description}", e.getValue()));
        }

        if (sender.isOp() || sender.hasPermission("connectfour.*") || sender.hasPermission("connectfour.admin")) {
            for (Map.Entry<String, String> e : adminCommandMap.entrySet()) {
                list.add(entry.replace("{command}", e.getKey()).replace("{description}", e.getValue()));
            }
        }

        if (footer != null) list.add(footer);

        for (String line : list) ConnectFour.getTexter().response(sender, line, true, false);
    }

    @Command("connectfour|cf stats [player]")
    @CommandDescription("Connect Four stats command")
    public void stats(CommandSender sender, @Argument("player") OfflinePlayer player) {
        if (player == null && !(sender instanceof Player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("wrong_usage", mapOf("usage", "/cf stats <player>")), true, false);
            return;
        }

        Object stats = ConnectFour.getConfigLoader().getPlayerStats(player != null ? player : (Player) sender);
        if (stats == null) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("no_stats_found_player"), true, false);
            return;
        }

        // Assuming a stats model with getters: getWins(), getLosses(), getDraws(), getGamesPlayed(), getName()
        int wins = getInt(stats, "wins");
        int losses = getInt(stats, "losses");
        int draws = getInt(stats, "draws");
        int gamesPlayed = getInt(stats, "gamesPlayed");

        int winRate = (gamesPlayed == 0) ? 0 : (int) (wins * 100.0 / gamesPlayed);

        String header = LangManager.getString("stats.header");
        String footer = LangManager.getString("stats.footer");

        List<String> list = new ArrayList<>(LangManager.getListPrefixed("stats.list", mapOf(
                "wins", Integer.toString(wins),
                "losses", Integer.toString(losses),
                "draws", Integer.toString(draws),
                "winrate", Integer.toString(winRate),
                "gamesPlayed", Integer.toString(gamesPlayed)
        )));

        boolean centered = LangManager.getBool("stats.centered");

        List<String> temp = new ArrayList<>();

        String playerName = (player != null && player.getName() != null) ? player.getName() : sender.getName();

        if (centered && sender instanceof Player) {
            if (header != null) temp.add(Texter.centered(header));
            temp.add(Texter.centered(LangManager.getStringPrefixed("stats.text", mapOf("player", playerName))));
            for (String s : list) temp.add(Texter.centered(s));
            if (footer != null) temp.add(Texter.centered(footer));
        } else {
            if (header != null) temp.add(header);
            temp.add(LangManager.getStringPrefixed("stats.text", mapOf("player", playerName)));
            temp.addAll(list);
            if (footer != null) temp.add(footer);
        }

        for (String s : temp) ConnectFour.getTexter().response(sender, s, true, false);
    }

    @Command("connectfour|cf invite [player]")
    @CommandDescription("Connect Four invite player command")
    public void invite(CommandSender sender, @Argument("player") Player player) {
        if (!(sender instanceof Player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("only_player"), true, false);
            return;
        } else if (player == null) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("wrong_usage", mapOf("usage", "/cf invite <player>")), true, false);
            return;
        } else if (player.equals(sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("invite_self_err"), true, false);
            return;
        } else if (ConnectFour.getInviteManager().inviter((Player) sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("someone_already_invited"), true, false);
            return;
        } else if (ConnectFour.getGameManager().isPlaying(player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("player_already_playing"), true, false);
            return;
        } else if (ConnectFour.getGameManager().isPlaying((Player) sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("self_already_playing"), true, false);
            return;
        }

        if (ConnectFour.getInviteManager().isInvitedBy(player, (Player) sender)) {
            ConnectFour.getInviteManager().accept((Player) sender, player);
        } else if (ConnectFour.getInviteManager().isInvitedBy((Player) sender, player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("player_already_invited"), true, false);
        } else {
            ConnectFour.getInviteManager().invite((Player) sender, player);
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("invite_sent", mapOf("player", player.getName())), true, false);
        }
    }

    @Command("connectfour|cf accept [player]")
    @CommandDescription("Connect Four accept invite command")
    public void accept(CommandSender sender, @Argument("player") Player player) {
        if (!(sender instanceof Player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("only_player"), true, false);
            return;
        } else if (player == null) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("wrong_usage", mapOf("usage", "/cf accept <player>")), true, false);
            return;
        } else if (player.getUniqueId().equals(((Player) sender).getUniqueId())) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("self_accept_err"), true, false);
            return;
        } else if (ConnectFour.getGameManager().isPlaying(player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("player_already_playing"), true, false);
            return;
        } else if (!ConnectFour.getInviteManager().isInvitedBy(player, (Player) sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("no_invites_from_player"), true, false);
            return;
        }

        ConnectFour.getInviteManager().accept((Player) sender, player);
    }

    @Command("connectfour|cf reject [player]")
    @CommandDescription("Connect Four reject invite command")
    public void reject(CommandSender sender, @Argument("player") Player player) {
        if (!(sender instanceof Player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("only_player"), true, false);
            return;
        } else if (player == null) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("wrong_usage", mapOf("usage", "/cf reject <player>")), true, false);
            return;
        } else if (player.getUniqueId().equals(((Player) sender).getUniqueId())) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("self_reject_err"), true, false);
            return;
        } else if (!ConnectFour.getInviteManager().isInvitedBy(player, (Player) sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("no_invites"), true, false);
            return;
        } else if (ConnectFour.getGameManager().isPlaying(player)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("player_already_playing"), true, false);
            return;
        } else if (!ConnectFour.getInviteManager().isInvitedBy(player, (Player) sender)) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("no_invites_from_player"), true, false);
            return;
        }

        ConnectFour.getInviteManager().reject((Player) sender, player);
    }

    @Command("connectfour|cf leaderboard")
    @CommandDescription("Connect Four leaderboard command")
    public void leaderBoard(CommandSender sender) {
        // Assuming this returns a list of objects with getName(), getWins(), getGamesPlayed()
        java.util.List<?> list = ConnectFour.getConfigLoader().getLeaderboard();

        if (list == null || list.isEmpty()) {
            ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("no_leaderboard_entries"), true, false);
            return;
        }

        List<String> symbols = new ArrayList<>(LangManager.getListPrefixed("leaderboard.symbols"));
        if (list.size() > symbols.size()) {
            symbols = Arrays.asList("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩");
        }

        String header = LangManager.getString("leaderboard.header");
        String footer = LangManager.getString("leaderboard.footer");

        if (header != null) ConnectFour.getTexter().response(sender, header, true, false);

        String entry = LangManager.getStringPrefixed("leaderboard.entry");

        for (int i = 0; i < list.size(); i++) {
            Object it = list.get(i);
            String symbol = symbols.get(i);
            int wins = getInt(it, "wins");
            int gamesPlayed = getInt(it, "gamesPlayed");
            String name = getString(it, "name");
            int winRate = (gamesPlayed == 0) ? 0 : (int) (wins * 100.0 / gamesPlayed);
            if (winRate > 100) winRate = 100; // cap at 100%
            String winRateTextColored;
            if (winRate >= 70) winRateTextColored = "&a" + winRate + "%";
            else if (winRate >= 50) winRateTextColored = "&e" + winRate + "%";
            else if (winRate <= 10) winRateTextColored = "&4" + winRate + "%";
            else winRateTextColored = "&c" + winRate + "%";

            ConnectFour.getTexter().response(
                    sender,
                    entry
                            .replace("{symbol}", symbol)
                            .replace("{player}", name)
                            .replace("{wins}", Integer.toString(wins))
                            .replace("{winrate}", winRateTextColored),
                    true,
                    false
            );
        }

        if (footer != null) ConnectFour.getTexter().response(sender, footer, true, false);
    }

    @Command("connectfour|cf reload")
    @CommandDescription("Connect Four reload config command")
    @Permission(value = {"connectfour.*", "connectfour.reload"}, mode = Permission.Mode.ANY_OF)
    public void reload(CommandSender sender) {
        ConnectFour.getConfigLoader().reload();
        LangManager.reload();
        ConnectFour.getTexter().response(sender, LangManager.getStringPrefixed("config_reload"), true, false);
    }

    private static Map<String, String> mapOf(String k1, String v1) {
        Map<String, String> m = new HashMap<>();
        m.put(k1, v1);
        return m;
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4, String k5, String v5) {
        Map<String, String> m = new HashMap<>();
        m.put(k1, v1); m.put(k2, v2); m.put(k3, v3); m.put(k4, v4); m.put(k5, v5);
        return m;
    }

    // Helpers to access fields via reflection to avoid depending on concrete models here
    private static int getInt(Object o, String field) {
        try {
            return (int) o.getClass().getField(field).get(o);
        } catch (Throwable ignore) {
            try {
                return (int) o.getClass().getMethod("get" + cap(field)).invoke(o);
            } catch (Throwable e) {
                return 0;
            }
        }
    }

    private static String getString(Object o, String field) {
        try {
            Object v = o.getClass().getField(field).get(o);
            return v == null ? "" : v.toString();
        } catch (Throwable ignore) {
            try {
                Object v = o.getClass().getMethod("get" + cap(field)).invoke(o);
                return v == null ? "" : v.toString();
            } catch (Throwable e) {
                return "";
            }
        }
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
