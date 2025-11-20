package eu.xap3y.connectfour.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.model.PlayerStatModel;
import eu.xap3y.connectfour.api.model.StorageModel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StorageMigrator {

    private StorageMigrator() {}

    /**
     * Run migration inside the plugin's data folder.
     *
     * @param plugin    plugin instance
     * @param backupOld if true, creates storage.json.bak before writing data.yml
     * @param overwrite if false and data.yml exists, aborts migration
     * @return true if migrated or already migrated successfully; false on failure
     */
    public static boolean migrate(ConnectFour plugin, boolean backupOld, boolean overwrite) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().severe("Failed to create plugin data folder.");
                return false;
            }
        }

        File jsonFile = new File(dataFolder, "storage.json");
        File yamlFile = new File(dataFolder, "data.yml");

        if (!jsonFile.exists()) {
            plugin.getLogger().info("No storage.json found. Nothing to migrate.");
            return true; // nothing to do
        }

        if (yamlFile.exists() && !overwrite) {
            plugin.getLogger().warning("data.yml already exists. Pass overwrite=true to replace it.");
            return false;
        }

        try (FileReader reader = new FileReader(jsonFile, StandardCharsets.UTF_8)) {
            // Parse StorageModel { players: List<PlayerStatModel> }
            Gson gson = new Gson();
            Type type = new TypeToken<StorageModel>(){}.getType();
            StorageModel storage = gson.fromJson(reader, type);

            // Build aggregated map (in case duplicates exist by UUID)
            Map<String, PlayerStatModel> byUuid = new LinkedHashMap<>();
            if (storage != null && storage.getPlayers() != null) {
                for (PlayerStatModel p : storage.getPlayers()) {
                    if (p == null || p.getUuid() == null) continue;
                    byUuid.merge(
                            p.getUuid(),
                            new PlayerStatModel(p.getUuid(), p.getName(), p.getGamesPlayed(), p.getWins(), p.getLosses(), p.getDraws(), 0, 0, 0),
                            (a, b) -> {
                                // aggregate counts; prefer latest non-null name
                                a.setName(b.getName() != null ? b.getName() : a.getName());
                                a.setWins(a.getWins() + b.getWins());
                                a.setLosses(a.getLosses() + b.getLosses());
                                a.setDraws(a.getDraws() + b.getDraws());
                                a.setGamesPlayed(a.getGamesPlayed() + b.getGamesPlayed());
                                return a;
                            }
                    );
                }
            }

            // Write YAML using Bukkit's YamlConfiguration
            YamlConfiguration yaml = new YamlConfiguration();
            ConfigurationSection playersSec = yaml.createSection("players");

            for (Map.Entry<String, PlayerStatModel> e : byUuid.entrySet()) {
                String uuid = e.getKey();
                PlayerStatModel s = e.getValue();
                ConfigurationSection sect = playersSec.createSection(uuid);
                sect.set("name", s.getName());
                sect.set("gamesPlayed", s.getGamesPlayed());
                sect.set("wins", s.getWins());
                sect.set("losses", s.getLosses());
                sect.set("draws", s.getDraws());
            }

            if (backupOld) {
                File bak = new File(dataFolder, "storage.json.bak");
                if (bak.exists()) bak.delete();
                if (!jsonFile.renameTo(bak)) {
                    try (FileReader fr = new FileReader(jsonFile, StandardCharsets.UTF_8);
                         FileWriter fw = new FileWriter(bak, StandardCharsets.UTF_8)) {
                        fr.transferTo(fw);
                    }
                }
            }

            yaml.save(yamlFile);
            plugin.getLogger().info("Migration completed: storage.json -> data.yml (" + byUuid.size() + " players).");
            return true;
        } catch (Exception ex) {
            plugin.getLogger().severe("Migration failed: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience method to migrate from raw JSON string (for tests or external usage).
     */
    public static String toYamlString(String storageJson) throws Exception {
        Gson gson = new Gson();
        StorageModel storage = gson.fromJson(storageJson, StorageModel.class);

        Map<String, PlayerStatModel> byUuid = new LinkedHashMap<>();
        if (storage != null && storage.getPlayers() != null) {
            for (PlayerStatModel p : storage.getPlayers()) {
                if (p == null || p.getUuid() == null) continue;
                byUuid.merge(
                        p.getUuid(),
                        new PlayerStatModel(p.getUuid(), p.getName(), p.getWins(), p.getLosses(), p.getDraws(), p.getGamesPlayed(), 0, 0, 0),
                        (a, b) -> {
                            a.setName(b.getName() != null ? b.getName() : a.getName());
                            a.setWins(a.getWins() + b.getWins());
                            a.setLosses(a.getLosses() + b.getLosses());
                            a.setDraws(a.getDraws() + b.getDraws());
                            a.setGamesPlayed(a.getGamesPlayed() + b.getGamesPlayed());
                            return a;
                        }
                );
            }
        }

        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection playersSec = yaml.createSection("players");
        for (Map.Entry<String, PlayerStatModel> e : byUuid.entrySet()) {
            String uuid = e.getKey();
            PlayerStatModel s = e.getValue();
            ConfigurationSection sect = playersSec.createSection(uuid);
            sect.set("name", s.getName());
            sect.set("gamesPlayed", s.getGamesPlayed());
            sect.set("wins", s.getWins());
            sect.set("losses", s.getLosses());
            sect.set("draws", s.getDraws());
        }
        return yaml.saveToString();
    }
}
