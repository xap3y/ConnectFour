package eu.xap3y.connectfour.util.hooks;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.model.PlayerStatModel;
import eu.xap3y.connectfour.manager.LangManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ConnectPlaceholderApi extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "connectfour";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XAP3Y";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.5.2";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String key = String.valueOf(player != null ? player.getUniqueId() : null);
        Map<String, PlayerStatModel> data = ConnectFour.getConfigLoader().data;

        if (params.toLowerCase(Locale.ROOT).startsWith("leaderboard_") && params.toLowerCase(Locale.ROOT).endsWith("_wins")) {
            // leaderboard_<position>_wins
            String posStr = params.substring("leaderboard_".length(), params.length() - "_wins".length());
            int position;
            try {
                position = Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                return null;
            }

            java.util.List<PlayerStatModel> list = ConnectFour.getConfigLoader().getLeaderboard();
            if (position < 1 || position > list.size()) {
                return "0";
            }

            PlayerStatModel stat = list.get(position - 1);

            return Integer.toString(stat.getWins());
        } else if (params.toLowerCase(Locale.ROOT).startsWith("leaderboard_") && params.toLowerCase(Locale.ROOT).endsWith("played")) {
            // leaderboard_<position>_played
            String posStr = params.substring("leaderboard_".length(), params.length() - "played".length());
            int position;
            try {
                position = Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                return null;
            }

            java.util.List<PlayerStatModel> list = ConnectFour.getConfigLoader().getLeaderboard();
            if (position < 1 || position > list.size()) {
                return "0";
            }

            PlayerStatModel stat = list.get(position - 1);

            return Integer.toString(stat.getGamesPlayed());
        } else if (params.toLowerCase(Locale.ROOT).startsWith("leaderboard_") && params.toLowerCase(Locale.ROOT).endsWith("lost")) {
            // leaderboard_<position>_lost
            String posStr = params.substring("leaderboard_".length(), params.length() - "lost".length());
            int position;
            try {
                position = Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                return null;
            }

            java.util.List<PlayerStatModel> list = ConnectFour.getConfigLoader().getLeaderboard();
            if (position < 1 || position > list.size()) {
                return "0";
            }

            PlayerStatModel stat = list.get(position - 1);

            return Integer.toString(stat.getLosses());
        } else if (params.toLowerCase(Locale.ROOT).startsWith("leaderboard_")) {
            // leaderboard_<position>
            String posStr = params.substring("leaderboard_".length());
            int position;
            try {
                position = Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                return null;
            }

            java.util.List<PlayerStatModel> list = ConnectFour.getConfigLoader().getLeaderboard();
            if (position < 1 || position > list.size()) {
                return Objects.requireNonNullElse(LangManager.getString("stats.no-entry"), "&cN/A");
            }

            PlayerStatModel stat = list.get(position - 1);

            return stat.getName();
        }

        switch (params.toLowerCase(Locale.ROOT)) {
            case "version":
                return ConnectFour.VERSION;

            case "wins": {
                PlayerStatModel s = data.get(key);
                return Integer.toString(s != null ? s.getWins() : 0);
            }

            case "losses": {
                PlayerStatModel s = data.get(key);
                return Integer.toString(s != null ? s.getLosses() : 0);
            }

            case "draws": {
                PlayerStatModel s = data.get(key);
                return Integer.toString(s != null ? s.getDraws() : 0);
            }

            case "played": {
                PlayerStatModel s = data.get(key);
                return Integer.toString(s != null ? s.getGamesPlayed() : 0);
            }


            case "totalbet": {
                PlayerStatModel s = data.get(key);
                return Integer.toString(s != null ? s.getTotalBet() : 0);
            }

            case "winrate": {
                if (player == null) return "0";
                PlayerStatModel stats = ConnectFour.getConfigLoader().getPlayerStats(player);
                if (stats == null) return "0";
                double winRate = stats.getGamesPlayed() == 0
                        ? 0.0
                        : (stats.getWins() * 100.0) / stats.getGamesPlayed();
                return Integer.toString((int) winRate);
            }

            default:
                return null;
        }
    }
}
