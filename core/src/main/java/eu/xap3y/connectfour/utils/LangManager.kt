package eu.xap3y.connectfour.utils

import eu.xap3y.connectfour.ConnectFour
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStream

class LangManager {

    companion object {

        private lateinit var lang: YamlConfiguration

        private val defaults = setOf("cz", "en")

        var prefix: String = "";

        fun reload() {

            defaults.forEach {
                checkFile(it)
            }

            val langCode = ConnectFour.language

            val file = File("${ConnectFour.instance.dataFolder}/lang/messages_${ConnectFour.language}.yml")

            if (!file.exists() && !defaults.contains(langCode)) {
                ConnectFour.instance.texter.console("&cLanguage &e$langCode &cnot found! Falling back to &een &clanguage")
                lang = YamlConfiguration.loadConfiguration(File("${ConnectFour.instance.dataFolder}/lang/messages_en.yml"))
                return
            } else if (file.exists()) {
                lang = YamlConfiguration.loadConfiguration(File("${ConnectFour.instance.dataFolder}/lang/messages_$langCode.yml"))
            }
        }

        private fun checkFile(lang: String) {
            val file = File("${ConnectFour.instance.dataFolder}/lang/messages_$lang.yml")
            if (file.exists().not()) {
                file.parentFile.mkdirs()
                file.createNewFile()
                ConnectFour.instance.getResource("messages_$lang.yml")?.copyTo(file.outputStream())
                //ConnectFour.instance.saveResource("lang/messages_$lang.yml", false)
            }
        }

        fun getBool(path: String): Boolean {
            return lang.getBoolean(path, false)
        }

        fun getStringPrefixed(path: String, map: HashMap<String, String>? = null): String {
            var value = lang.getString(path) ?: return "&cMessage &e$path &cis not set in messages_en.yml"
            value = value.replace("{prefix}", prefix)

            map?.forEach { (key, v0) ->
                value = value.replace("{$key}", v0)
            }

            return value
        }

        fun getString(path: String): String? {
            return lang.getString(path)
        }

        fun getInt(path: String, def: Int = 0): Int {
            return lang.getInt(path, def)
        }

        fun getListPrefixed(path: String, map: HashMap<String, String>? = null): List<String> {
            var value = lang.getStringList(path)
            if (value.isEmpty()) return listOf("&cList &e$path &cis not set in messages_en.yml")

            value = value.map {
                it.replace("{prefix}", prefix)
            }

            map?.forEach { (key, v0) ->
                value = value.map {
                    it.replace("{$key}", v0)
                }
            }

            return value
        }
    }
}