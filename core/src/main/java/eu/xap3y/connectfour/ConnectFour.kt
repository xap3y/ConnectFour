package eu.xap3y.connectfour

import com.cryptomorin.xseries.XSound
import eu.xap3y.connectfour.commands.PaperRootCommand
import eu.xap3y.connectfour.commands.RootCommand
import eu.xap3y.connectfour.utils.CommandLoader
import eu.xap3y.connectfour.utils.Connect4GameManager
import eu.xap3y.connectfour.utils.InviteManager
import eu.xap3y.xagui.XaGui
import eu.xap3y.xalib.managers.ConfigManager
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TexterObj
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ConnectFour : JavaPlugin() {

    val guiManager by lazy { XaGui(this) }

    lateinit var texter: Texter
    val version: String = "1.0.0"
    /*val gameManager by lazy { MenuManager(this) }*/
    val gameManager by lazy { Connect4GameManager(this) }
    val inviteManager by lazy { InviteManager(this) }
    var prefix = "&8[&6ConnectFour&8] &7"

    override fun onEnable() {
        instance = this
        ConfigManager(this).reloadConfig()
        prefix = config.getString("prefix", "&8[&6ConnectFour&8] &7") ?: ""
        texter = Texter(TexterObj(
            prefix,
            false,
            null
        ))

        var nmsver = Bukkit.getServer().javaClass.getPackage().name;
        //texter.console("NMS Version: $nmsver")
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
        if (nmsver.startsWith("v1_8_") || nmsver.startsWith("v1_7_")) { // Not sure if 1_7 works for the protocol hack?
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
                isNewestPaper = true
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
        if (isPaper) {
            if (isNewestPaper) {
                val parser = eu.xap3y.connectfour.V1_20_R1.CommandLoader(this)
                parser.getParser().parse(PaperRootCommand(instance))
            } else {
                texter.console("&cThis plugin is currently only supported up to 1.20.6. To use this with version 1.21+ you need to choose other bukkit platform other then PaperSpigot")
                server.pluginManager.disablePlugin(this)
                this.isEnabled = false
                return
            }
        } else {
            eu.xap3y.connectfour.utils.CommandLoader(this).getParser().parse(RootCommand(this))
        }
        //this.server.pluginManager.registerEvents(PlayerJoinListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var instance: ConnectFour
        var useOld: Boolean = false
        var useNew: Boolean = false
        var useTextComponents: Boolean = true
        var isPaper: Boolean = false
        var isNewestPaper: Boolean = false
    }
}

fun Player.ps(sound: XSound) {
    sound.play(this)
}
