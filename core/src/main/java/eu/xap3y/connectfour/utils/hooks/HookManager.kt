package eu.xap3y.connectfour.utils.hooks

import eu.xap3y.connectfour.ConnectFour
import org.bukkit.Bukkit

class HookManager(private val plugin: ConnectFour) {

    fun hook() {
        if (plugin.configModel.hookPapi)
            hookPapi()

        if (plugin.configModel.hookMiniPlaceholders)
            hookMiniPlaceholders()
    }

    private fun hookPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            ConnectPlaceholderApi(plugin).register()
            plugin.texter.console("&aPlaceholderAPI found, placeholders are registered!")
        } else {
            plugin.texter.console("&cPlaceholderAPI not found, not registering placeholders")
        }
    }

    private fun hookMiniPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("MiniPlaceholders") != null) {
            ConnectMiniPlaceholders(plugin).register()
            plugin.texter.console("&aMiniPlaceholders found, placeholders are registered !")
        } else {
            plugin.texter.console("&cMiniPlaceholders not found, not registering placeholders")
        }
    }
}