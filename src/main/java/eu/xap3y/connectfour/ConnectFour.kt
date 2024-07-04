package eu.xap3y.connectfour

import eu.xap3y.connectfour.utils.CommandLoader
import eu.xap3y.connectfour.utils.Connect4GameManager
import eu.xap3y.xagui.XaGui
import eu.xap3y.xalib.managers.ConfigManager
import eu.xap3y.xalib.managers.Texter
import eu.xap3y.xalib.objects.TexterObj
import org.bukkit.plugin.java.JavaPlugin

class ConnectFour : JavaPlugin() {

    val guiManager by lazy { XaGui(this) }

    lateinit var texter: Texter
    val version: String = "1.0.0"
    /*val gameManager by lazy { MenuManager(this) }*/
    val newGameManager by lazy { Connect4GameManager(this) }

    override fun onEnable() {
        instance = this
        CommandLoader(this).register()
        ConfigManager(this).reloadConfig()
        texter = Texter(TexterObj(
            config.getString("prefix", "&8[&6ConnectFour&8] &7") ?: "",
            false,
            null
        ))
        //this.server.pluginManager.registerEvents(PlayerJoinListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var instance: ConnectFour
    }
}
