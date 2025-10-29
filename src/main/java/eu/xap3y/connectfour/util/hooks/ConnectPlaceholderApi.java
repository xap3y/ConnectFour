package eu.xap3y.connectfour.util.hooks;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.model.PlayerStatModel;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

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
        return "1.3.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String key = String.valueOf(player != null ? player.getUniqueId() : null);
        Map<String, PlayerStatModel> data = ConnectFour.getConfigLoader().data;

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
