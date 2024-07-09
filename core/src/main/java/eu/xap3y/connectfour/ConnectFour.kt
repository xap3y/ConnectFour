package eu.xap3y.connectfour

import com.cryptomorin.xseries.XSound
import eu.xap3y.connectfour.ConnectFour.Companion.isPaper
import eu.xap3y.connectfour.ConnectFour.Companion.useNew
import eu.xap3y.connectfour.ConnectFour.Companion.useOld
import eu.xap3y.connectfour.ConnectFour.Companion.useTextComponents
import eu.xap3y.connectfour.commands.RootCommand
import eu.xap3y.connectfour.models.ConfigModel
import eu.xap3y.connectfour.utils.ConfigLoader
import eu.xap3y.connectfour.utils.Connect4GameManager
import eu.xap3y.connectfour.utils.InviteManager
import eu.xap3y.connectfour.utils.RequestHttp
import eu.xap3y.connectfour.utils.hooks.HookManager
import eu.xap3y.xagui.XaGui
import eu.xap3y.xalib.managers.ConfigManager
import eu.xap3y.xalib.managers.Texter
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.net.InetAddress

class ConnectFour : JavaPlugin() {

    val guiManager by lazy { XaGui(this) }

    lateinit var texter: Texter
    /*val gameManager by lazy { MenuManager(this) }*/
    val gameManager by lazy { Connect4GameManager(this) }
    val inviteManager by lazy { InviteManager(this) }
    var prefix = "&8[&6ConnectFour&8] &7"
    lateinit var configModel: ConfigModel
    val configManager by lazy { ConfigManager(this) }
    val configLoader by lazy { ConfigLoader(this) }
    var openedGuis: MutableSet<Player> = mutableSetOf()

    override fun onEnable() {
        instance = this
        configLoader.reload()
        configLoader.loadData()

        // NMS So the plugin is compatible with 1.8.8 - 1.21
        nms()

        // This command parser is horrible, it needs to be rewritten. OR maybe I shouldn't use org.incendo.cloud framework but native Bukkit API
        // Because 1.20.6+ API uses CommandSourceStack instead of CommandSender
        /*if (isPaper) {
            eu.xap3y.connectfour.V1_20_R1.CommandLoader(this).getParser().parse(PaperRootCommand(instance))
        } else {
            eu.xap3y.connectfour.utils.CommandLoader(this).getParser().parse(RootCommand(this))
        }*/
        eu.xap3y.connectfour.utils.CommandLoader(this).getParser().parse(RootCommand(this))
        //this.server.pluginManager.registerEvents(PlayerJoinListener(), this)

        HookManager(this).hook()

        if (configModel.metrics) {
            val metrics = Metrics(this, 22557)
            metrics.addCustomChart(SingleLineChart("totalGames") { totalGames })
        }

        if (configModel.updates) {
            RequestHttp.isNewest().whenComplete { it, e ->
                if ((it.second == null && it.first.not()) || e != null) {
                    texter.console("&cFailed to check for updates")
                    return@whenComplete
                }
                else if (it.first.not() && it.second != null) {
                    texter.console("&eThere is a new version available! &7(&4$VERSION &9-> &2${it.second}&7)")
                    texter.console("&fPlease download newest version from &bhttps://github.com/xap3y/ConnectFour/releases")
                }
            }
        }
    }

    override fun onDisable() {
        openedGuis.forEach {
            it.closeInventory()
            //it.sendMessage("The plugin has been disabled, closing all open GUIs")
        }
    }

    companion object {
        lateinit var instance: ConnectFour
        var useOld: Boolean = false
        var useNew: Boolean = false
        var useTextComponents: Boolean = true
        var isPaper: Boolean = false
        const val VERSION_UPSTREAM_URL = "https://raw.githubusercontent.com/xap3y/ConnectFour/main/VER"
        var totalGames = 0
        const val VERSION: String = "1.0.0"
    }
}

fun Player.ps(sound: XSound, vol: Float = 1.0f, pitch: Float = 1.0f) {
    sound.play(this, vol, pitch)
    // Using Bukkit method:
    //this.playSound(this.location, sound.parseSound() ?: return, vol, pitch)
}

fun nms() {
    var nmsver = Bukkit.getServer().javaClass.getPackage().name;
    //ConnectFour.instance.texter.console("NMS Version: $nmsver")
    nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
    if (nmsver.startsWith("v1_8_") || nmsver.startsWith("v1_7_") || nmsver.startsWith("v1_9_") || nmsver.startsWith("v1_10_") || nmsver.startsWith("v1_11_")) { // Not sure if 1_7 works for the protocol hack?
        useOld = true
    } else if (nmsver.startsWith("v1_21_") || nmsver.startsWith("v1_22_")) {
        if (nmsver.startsWith("v1_20_6")) isPaper = true
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent")
            useNew = true
        } catch (e: ClassNotFoundException) {
            useNew = false
        }
    }
    val version = try {
        Bukkit.getServer().version.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0]
    } catch (e: Exception) {
        Bukkit.getServer().version.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    if (version.startsWith("1.21")) {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            isPaper = true
        } catch (e: ClassNotFoundException) {
            isPaper = false
        }
    }

    try {
        Class.forName("net.md_5.bungee.api.chat.TextComponent")
    } catch (e: ClassNotFoundException) {
        useTextComponents = false
    }

    //texter.console("Using old: $useOld, Using new: $useNew, Using text components: $useTextComponents, isPaper: $isPaper")
}