package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.xagui.GuiMenu
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.concurrent.ConcurrentHashMap

@Deprecated("Use Connect4GameManager instead")
class MenuManager(private val plugin: ConnectFour = ConnectFour.instance) {

    private val playingPlayers = ConcurrentHashMap<Player, Player>()
    private val playerColor = hashMapOf<Player, Boolean>()

    private val borderPane = (XMaterial.BLACK_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1))

    private val redPane = XMaterial.RED_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.RED_STAINED_GLASS_PANE, 1)
    private val yellowPane = XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1)
    //private val bluePane = XMaterial.BLUE_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)
    private val greenPane = XMaterial.LIME_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.LIME_STAINED_GLASS_PANE, 1)

    fun isPlaying(player: Player): Boolean {
        return playingPlayers.containsKey(player) || playingPlayers.containsValue(player)
    }

    fun openGui(player: Player, opponent: Player) {

        var onMove: Int
        var end = false

        playingPlayers[player] = opponent

        // Open GUI
        val gui: GuiMenu = plugin.guiManager.createMenu("&bConnect4 &7| &e${player.name} &c- &e${opponent.name}", 6)

        val grid: Array<IntArray> = Array(6) { IntArray(7) { 0 } }

        // Add items to GUI
        val border: Set<Int> = setOf(16, 25, 34, 43)

        gui.setSlot(26, GuiButton(borderPane).setName(" "))
        gui.setSlot(35, GuiButton(borderPane).setName(" "))
        gui.setSlot(7, GuiButton(borderPane).setName(" "))
        gui.setSlot(52, GuiButton(borderPane).setName(" "))

        val skullPlayer: ItemStack = XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.PLAYER_HEAD,1)
        val skullPlayerMeta: SkullMeta = skullPlayer.itemMeta as SkullMeta
        // Using deprecated .setOwner() cuz 1.8.8 doesn't have .setOwningPlayer()
        skullPlayerMeta.setOwner(player.name)
        skullPlayer.itemMeta = skullPlayerMeta

        val skullOpponent: ItemStack = XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.PLAYER_HEAD,1)
        val skullOpponentMeta: SkullMeta = skullOpponent.itemMeta as SkullMeta
        skullOpponentMeta.setOwner(opponent.name)
        skullOpponent.itemMeta = skullOpponentMeta

        // Randomize player color
        val randomBoolean: Boolean = (0..1).random() == 1
        playerColor[player] = randomBoolean
        playerColor[opponent] = !randomBoolean

        gui.fillSlots(gui.getCurrentPageIndex(), border, GuiButton(if (randomBoolean) redPane else yellowPane).setName(" "))

        onMove = if (randomBoolean) 1 else 2

        gui.setSlot(17, GuiButton(skullPlayer).setName("&9${player.name}").setLore(" ", "&7&l| &7Color: ${if (randomBoolean) "&cRed" else "&eYellow"}"))
        val button1 = if (randomBoolean) GuiButton(redPane.clone()).setName("&cRed") else GuiButton(yellowPane).setName("&eYellow")
        if (onMove == 1) button1.addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)
        gui.setSlot(8, button1.addItemFlag(ItemFlag.HIDE_ENCHANTS))

        gui.setSlot(44, GuiButton(skullOpponent).setName("&9${opponent.name}").setLore(" ", "&7&l| &7Color: ${if (!randomBoolean) "&cRed" else "&eYellow"}"))
        val button2 = if (!randomBoolean) GuiButton(redPane.clone()).setName("&cRed") else GuiButton(yellowPane).setName("&eYellow")
        if (onMove == 2) button2.addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)
        gui.setSlot(53, button2.addItemFlag(ItemFlag.HIDE_ENCHANTS))

        gui.open(player)
        gui.open(opponent)

        // Prepare SLOTS

        val redGlowing = GuiButton(redPane.clone()).addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)
        val yellowGlowing = GuiButton(yellowPane.clone()).addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1)

        gui.setOnClick { event ->
            if (event.currentItem == null || end) return@setOnClick

            //player.sendMessage("You clicked on slot ${event.slot}")

            val playerNumber = playerColor[event.whoClicked as Player]?.let { if (it) 1 else 2 } ?: 0

            if (onMove != playerNumber) return@setOnClick

            val pair: Pair<Int, Int> = dropToken(grid, event.slot % 9, playerNumber) ?: return@setOnClick

            val color: String = if (playerNumber == 2) "&cRed" else "&eYellow"
            val lore: List<String> = if ((event.whoClicked as Player).uniqueId == player.uniqueId) listOf("", "&7&l| &7On move: ${opponent.name}") else listOf("", "&7&l| &7On move: ${player.name}")

            gui.fillSlots(gui.getCurrentPageIndex(), border, GuiButton(if (onMove == 2) redPane.clone() else yellowPane.clone()).setName(color).setLoreList(lore).addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1))

            val slotToEnchant = if ((event.whoClicked as Player).uniqueId != player.uniqueId) 8 else 53
            val secondSlotToDisenchant = if (slotToEnchant == 8) 53 else 8
            val old = gui.getSlot(slotToEnchant)!!
            val old2 = gui.getSlot(secondSlotToDisenchant)!!
            gui.setSlot(slotToEnchant, old.addEnchantment(XEnchantment.SILK_TOUCH.enchant!!, 1))
            gui.setSlot(secondSlotToDisenchant, old2.removeEnchantment(XEnchantment.SILK_TOUCH.enchant!!))

            onMove = if (onMove == 1) 2 else 1
            // update gui button on landed row
            val button: ItemStack = if (playerNumber == 1) redPane.clone() else yellowPane.clone()
            // if pair.first == 0, and pair.second == 2 then slot = 2
            gui.setSlot(pair.first * 9 + pair.second, button)

            val win: List<Pair<Int, Int>> = checkWin(grid, playerNumber) ?: return@setOnClick
            win.forEach {
                gui.setSlot(it.first * 9 + it.second, GuiButton(greenPane.clone()).setName("WIN"))
            }

            playingPlayers.remove(player)
            end = true
            //val winner = event.whoClicked as Player

            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                opponent.closeInventory()
                player.closeInventory()
            }, 20L * 2 + 10L)
        }

        gui.setOnClose { event ->
            if (event.player.uniqueId == player.uniqueId)
                gui.close(opponent)
            else
                gui.close(player)
            playingPlayers.remove(player)
        }
    }

    private fun dropToken(grid: Array<IntArray>, column: Int, player: Int): Pair<Int, Int>? {
        for (row in 5 downTo 0) {
            if (grid[row][column] == 0) {
                grid[row][column] = player
                return Pair(row, column)
            }
        }
        return null
    }

    private fun checkWin(grid: Array<IntArray>, player: Int): List<Pair<Int, Int>>? {
        for (row in 0..5) {
            for (col in 0..3) {
                if ((0..3).all { grid[row][col + it] == player }) {
                    return (0..3).map { row to (col + it) }
                }
            }
        }

        for (col in 0..6) {
            for (row in 0..2) {
                if ((0..3).all { grid[row + it][col] == player }) {
                    return (0..3).map { (row + it) to col }
                }
            }
        }

        for (row in 0..2) {
            for (col in 0..3) {
                if ((0..3).all { grid[row + it][col + it] == player }) {
                    return (0..3).map { (row + it) to (col + it) }
                }
            }
        }

        for (row in 3..5) {
            for (col in 0..3) {
                if ((0..3).all { grid[row - it][col + it] == player }) {
                    return (0..3).map { (row - it) to (col + it) }
                }
            }
        }

        return null
    }
}