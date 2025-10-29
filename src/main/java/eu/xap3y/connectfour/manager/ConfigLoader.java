package eu.xap3y.connectfour.manager;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.dto.TexterObjDto;
import eu.xap3y.connectfour.api.model.ConfigModel;
import eu.xap3y.connectfour.api.model.PlayerStatModel;
import eu.xap3y.connectfour.service.Texter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    public final Map<String, PlayerStatModel> data = new HashMap<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    public void reload() {
        ConnectFour.getInstance().reloadConfig();
        FileConfiguration cfg = ConnectFour.getInstance().getConfig();

        LangManager.prefix = cfg.getString("prefix", "&8[&6ConnectFour&8] &7");
        ConnectFour.language = cfg.getString("lang", "en");

        ConnectFour.setTexter(new Texter(new TexterObjDto(
                cfg.getString("prefix", "&8[&6ConnectFour&8] &7"),
                false,
                null
        )));

        ConnectFour.setConfigModel(new ConfigModel(
                cfg.getInt("inviteTimeout", 60),
                cfg.getBoolean("tokenFallAnimation", true),
                cfg.getLong("tokenFallSpeed", 120L),
                cfg.getBoolean("inviterStarts", false),
                cfg.getBoolean("winRewards.enabled", false),
                cfg.getStringList("winRewards.commands"),
                cfg.getBoolean("hooks.placeholderAPI", true),
                cfg.getBoolean("hooks.miniPlaceholders", false),
                cfg.getBoolean("metrics", true),
                cfg.getBoolean("updateCheck", true),
                cfg.getBoolean("doubleEscape", true),
                cfg.getInt("moveTimeout", 0)
        ));

        dataFile = new File(ConnectFour.getInstance().getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            File oldData = new File(ConnectFour.getInstance().getDataFolder(), "storage.json");
            if (oldData.exists()) {
                ConnectFour.getTexter().console("&eOld storage.json file detected, attempting migration to data.yml...");
                boolean migrated = eu.xap3y.connectfour.util.StorageMigrator.migrate(ConnectFour.getInstance(), true, false);
                if (migrated) {
                    ConnectFour.getTexter().console("&aMigration completed successfully!");
                } else {
                    ConnectFour.getTexter().console("&cMigration failed! Please check the console for details.");
                }
            } else {
                ConnectFour.getInstance().saveResource("data.yml", false);
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadData() {
        if (!ConnectFour.getInstance().getDataFolder().exists()) return;

        if (dataConfig.getConfigurationSection("players") == null) {
            return;
        }
        for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
            String path = "players." + key + ".";
            PlayerStatModel psm = new PlayerStatModel(
                    key,
                    dataConfig.getString(path + "name", "Unknown"),
                    dataConfig.getInt(path + "gamesPlayed", 0),
                    dataConfig.getInt(path + "wins", 0),
                    dataConfig.getInt(path + "losses", 0),
                    dataConfig.getInt(path + "draws", 0)
            );
            data.put(key, psm);
        }
    }

    /*public void saveData() {
        File dataFile = new File(ConnectFour.getInstance().getDataFolder(), "storage.json");

        try (FileWriter writer = new FileWriter(dataFile, false)) {
            StorageModel storage = new StorageModel(new ArrayList<>(data.values()));
            prettyGson.toJson(storage, writer);
        } catch (Exception ignored) {
        }
    }*/

    public void savePlayerData(Player p0) {
        PlayerStatModel psm = data.get(p0.getUniqueId().toString());
        String path = "players." + p0.getUniqueId() + ".";
        dataConfig.set(path + "name", p0.getName());
        dataConfig.set(path + "gamesPlayed", psm.getGamesPlayed());
        dataConfig.set(path + "wins", psm.getWins());
        dataConfig.set(path + "losses", psm.getLosses());
        dataConfig.set(path + "draws", psm.getDraws());
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            ConnectFour.getTexter().console("&cCould not save player data for " + p0.getName() + "!");
        }

    }

    public void checkPlayer(Player player) {
        String key = player.getUniqueId().toString();
        if (!data.containsKey(key)) {
            data.put(key, new PlayerStatModel(
                    player.getUniqueId().toString(),
                    player.getName(),
                    0,
                    0,
                    0,
                    0
            ));
        }
    }

    public PlayerStatModel getPlayerStats(OfflinePlayer player) {
        return data.get(player.getUniqueId().toString());
    }

    public List<PlayerStatModel> getLeaderboard() {
        int limit = LangManager.getInt("leaderboard.entries", 10);
        return data.values()
                .stream()
                .sorted(Comparator.comparingInt(PlayerStatModel::getWins).reversed())
                .limit(limit)
                .toList();
    }
}