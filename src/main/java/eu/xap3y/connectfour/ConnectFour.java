package eu.xap3y.connectfour;

import eu.xap3y.connectfour.api.model.ConfigModel;
import eu.xap3y.connectfour.command.RootCommand;
import eu.xap3y.connectfour.listener.PlayerQuitListener;
import eu.xap3y.connectfour.manager.*;
import eu.xap3y.connectfour.service.Texter;
import eu.xap3y.connectfour.util.RequestHttp;
import eu.xap3y.connectfour.util.hooks.ConnectPlaceholderApi;
import eu.xap3y.xagui.XaGui;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ConnectFour extends JavaPlugin {

    @Getter
    private static ConnectFour instance;

    @Getter
    @Setter
    private static Texter texter;

    public static boolean useOld = false;
    public static boolean useNew = false;
    public static boolean useTextComponents = true;
    public static boolean isPaper = false;
    public static final String VERSION_UPSTREAM_URL = "https://raw.githubusercontent.com/xap3y/ConnectFour/main/VER";
    public static int totalGames = 0;
    public static int totalDraws = 0;
    public static final String VERSION = "1.5.0";
    public static String language = "en";

    @Getter
    @Setter
    private static XaGui xagui;

    @Setter
    @Getter
    private static GameManager gameManager;

    @Getter
    @Setter
    private static InviteManager inviteManager;

    @Setter
    @Getter
    private static ConfigModel configModel;

    @Getter
    private static ConfigLoader configLoader;

    @Getter
    private static boolean isFolia = false;

    @Getter
    private final Set<Player> openedGuis = Collections.synchronizedSet(new HashSet<>());

    @Getter
    private Economy economy = null;

    @Override
    public void onEnable() {
        instance = this;
        xagui = new XaGui(this);
        gameManager = new GameManager();
        inviteManager = new InviteManager();
        configLoader = new ConfigLoader();

        //  Creating parser & Parsing command classes below  \\
        CommandManager cmdManager = new CommandManager();
        cmdManager.parse(new RootCommand());

        ConfigManager.reloadConfig();

        configLoader.reload();
        configLoader.loadData();

        nms();

        if (configModel != null && configModel.isMetrics()) {
            Metrics metrics = new Metrics(this, 22557);
            metrics.addCustomChart(new SingleLineChart("total_games", () -> totalGames));
            metrics.addCustomChart(new SingleLineChart("total_draws", () -> totalDraws));
            metrics.addCustomChart(new SimplePie("used_language", () -> getConfig().getString("lang", "en")));
        }

        if (configModel.isUpdates()) {
            RequestHttp.isNewest().whenComplete((result, ex) -> {
                if ((ex != null || result.latestVersion() == null) && !result.isUpToDate()) {
                    texter.console("Could not check for updates!");
                    return;
                } else if (result.isUpToDate()){
                    texter.console("&aYou are running the latest version of Connect Four! &7(&2" + VERSION + "&7)");
                    return;
                }
                if (!result.isUpToDate()) {
                    texter.console("&eThere is a new version available! &7(&4" + VERSION + " &9-> &2" + result.latestVersion() + "&7)");
                    texter.console("&fDownload it at: &bhttps://www.spigotmc.org/resources/connect-four-1v1-gui-minigame-1-8-8-1-21.117864/");
                }
            });
        }

        LangManager.reload();
        LangManager.checkDefaults();
        //  Registering PlaceholderAPI  \\
        if (configModel.isHookPapi()) {
            registerPapi();
        }

        if (configModel.isHookMiniPlaceholders()) {
            registerMini();
        }

        BukkitRunnable economySetup = new BukkitRunnable() {
            @Override
            public void run() {
                if (economy == null) {
                    setupEconomy();
                } else {
                    this.cancel();
                }
            }
        };

        try {
            economySetup.runTaskTimerAsynchronously(this, 60L, 20L * 60L * 5L);
        } catch (UnsupportedOperationException ex) {
            // folia
            //economySetup.runTaskTimer(this, 60L, 20L * 60L * 5L);
        }


        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public void onDisable() {
        synchronized (openedGuis) {
            for (Player p : openedGuis) {
                try { p.closeInventory(); } catch (Throwable ignored) {}
            }
            openedGuis.clear();
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            texter.console("&cVault not found!");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            texter.console("&cVault rsp error (economy)! &8No economy plugin found?");
            return;
        }
        economy = rsp.getProvider();
    }

    private void registerPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ConnectPlaceholderApi().register();
        }
    }

    private void registerMini() {
        if (Bukkit.getPluginManager().getPlugin("MiniPlaceholders") != null) {
            //new ConnectMiniPlaceholders().register();
        }
    }

    private void nms() {
        String nmsver = Bukkit.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

        if (nmsver.startsWith("v1_8_") || nmsver.startsWith("v1_7_") || nmsver.startsWith("v1_9_") || nmsver.startsWith("v1_10_") || nmsver.startsWith("v1_11_")) {
            useOld = true;
        } else if (nmsver.startsWith("v1_21_") || nmsver.startsWith("v1_22_")) {
            if (nmsver.startsWith("v1_20_6")) isPaper = true;
            try {
                Class.forName("net.md_5.bungee.api.chat.BaseComponent");
                useNew = true;
            } catch (ClassNotFoundException ex) {
                useNew = false;
            }
        }

        String version;
        try {
            String[] split = Bukkit.getServer().getVersion().split(" ");
            version = split[0].split("-")[0];
        } catch (Exception e) {
            version = Bukkit.getServer().getVersion().split(" ")[0];
        }

        if (version.startsWith("1.21")) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                isPaper = true;
            } catch (ClassNotFoundException ex) {
                isPaper = false;
            }
        }

        try {
            Class.forName("net.md_5.bungee.api.chat.TextComponent");
        } catch (ClassNotFoundException ex) {
            useTextComponents = false;
        }
    }

}
