package eu.xap3y.connectfour.manager;

import eu.xap3y.connectfour.ConnectFour;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

public class LangManager {

    private static YamlConfiguration lang;
    private static final Set<String> defaults = new HashSet<>(Arrays.asList("cz", "en"));

    private static final Map<String, String> checkStrings = new HashMap<>() {{
        put("not_enough_money", "{prefix}&cYou don't have enough money to start the game!");
        put("invite_accept_no_money_target", "{prefix}&c{player} doesn't have enough money to start the game!");
        put("invite_accept_bet_withdraw", "{prefix}&a{bet}$ &fhas been withdrawn from your balance for the bet!");
        put("gui.pot.name", "&6&lPot: &a{pot}$");
        put("game_refund_other_cancel", "{prefix}&fThe opponent has cancelled the game. You got the pot of &a{bet}$&f!");
        put("game_refund_cancel", "{prefix}&fGame has been cancelled. &a{bet}$ &fhas been refunded to your balance!");
        put("game_lost_due_cancel", "{prefix}&cYou lost your &a{bet}$ &cbet because you cancelled the game!");
        put("won_bet", "{prefix}&fYou won the bet! You received &a{pot}$ &ffrom the pot!");
        put("draw_pot", "{prefix}&fThe game ended in a draw! You received &a{bet}$ &ffrom the pot!");
        put("lost_bet", "{prefix}&cYou lost the bet! You lost your &a{bet}$ &cbet!");
        put("max_bet", "{prefix}&cYou can't bet more than &a{max}$&c!");
        put("min_bet", "{prefix}&cYou can't bet less than &a{min}$&c!");
    }};

    private static final Map<String, List<String>> checkStringsList = new HashMap<>() {{
        put("gui.pot.lore", Arrays.asList(
                " ",
                "&7&l| &fEach player bet: &a{bet}$"
        ));
    }};

    public static String prefix = "";

    public static void checkDefaults() {
        for (String d : defaults) checkFile(d);

        for (String d : defaults) {
            File file = new File(ConnectFour.getInstance().getDataFolder(), "lang/messages_" + d + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            boolean changed = false;
            for (Map.Entry<String, String> entry : checkStrings.entrySet()) {
                if (config.getString(entry.getKey()) == null) {
                    config.set(entry.getKey(), entry.getValue());
                    changed = true;
                }
            }
            if (changed) {
                try {
                    config.save(file);
                } catch (IOException ignored) { }
            }
        }

        checkDefaultsLists();
    }

    public static void checkDefaultsLists() {
        for (String d : defaults) checkFile(d);

        for (String d : defaults) {
            File file = new File(ConnectFour.getInstance().getDataFolder(), "lang/messages_" + d + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            boolean changed = false;
            for (Map.Entry<String, List<String>> entry : checkStringsList.entrySet()) {
                if (config.getStringList(entry.getKey()).isEmpty()) {
                    config.set(entry.getKey(), entry.getValue());
                    changed = true;
                }
            }
            if (changed) {
                try {
                    config.save(file);
                } catch (IOException ignored) { }
            }
        }

        reload();
    }

    public static void reload() {
        for (String d : defaults) checkFile(d);

        String langCode = ConnectFour.language;
        File file = new File(ConnectFour.getInstance().getDataFolder(), "lang/messages_" + langCode + ".yml");

        if (!file.exists() && !defaults.contains(langCode)) {
            ConnectFour.getTexter().console("&cLanguage &e" + langCode + " &cnot found! Falling back to &een &clanguage");
            lang = YamlConfiguration.loadConfiguration(new File(ConnectFour.getInstance().getDataFolder(), "lang/messages_en.yml"));
            return;
        } else if (file.exists()) {
            lang = YamlConfiguration.loadConfiguration(file);
        }
    }

    private static void checkFile(String code) {
        try {
            File file = new File(ConnectFour.getInstance().getDataFolder(), "lang/messages_" + code + ".yml");
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                file.createNewFile();
                try (InputStream in = ConnectFour.getInstance().getResource("messages_" + code + ".yml")) {
                    if (in != null) {
                        try (OutputStream out = new FileOutputStream(file)) {
                            in.transferTo(out);
                        }
                    }
                }
            }
        } catch (IOException ignored) { }
    }

    public static boolean getBool(String path) {
        return lang != null && lang.getBoolean(path, false);
    }

    public static String getStringPrefixed(String path) {
        String v = (lang != null) ? lang.getString(path) : null;
        if (v == null) return "&cMessage &e" + path + " &cis not set in messages_en.yml";
        return v.replace("{prefix}", prefix);
    }

    public static String getStringPrefixed(String path, Map<String, String> map) {
        String v = (lang != null) ? lang.getString(path) : null;
        if (v == null) return "&cMessage &e" + path + " &cis not set in messages_en.yml";
        v = v.replace("{prefix}", prefix);
        if (map != null) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                v = v.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return v;
    }

    public static String getString(String path) {
        return lang != null ? lang.getString(path) : null;
    }

    public static int getInt(String path, int def) {
        return lang != null ? lang.getInt(path, def) : def;
    }

    public static List<String> getListPrefixed(String path) {
        return getListPrefixed(path, null);
    }

    public static List<String> getListPrefixed(String path, Map<String, String> map) {
        List<String> value = (lang != null) ? lang.getStringList(path) : Collections.emptyList();
        if (value.isEmpty()) return Collections.singletonList("&cList &e" + path + " &cis not set in messages_en.yml");

        List<String> mapped = new ArrayList<>(value.size());
        for (String line : value) {
            mapped.add(line.replace("{prefix}", prefix));
        }

        if (map != null && !map.isEmpty()) {
            List<String> replaced = new ArrayList<>(mapped.size());
            for (String s : mapped) {
                String t = s;
                for (Map.Entry<String, String> e : map.entrySet()) {
                    t = t.replace("{" + e.getKey() + "}", e.getValue());
                }
                replaced.add(t);
            }
            return replaced;
        }
        return mapped;
    }
}