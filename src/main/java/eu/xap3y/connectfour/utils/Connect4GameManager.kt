package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XMaterial
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.utils.StaticItems.greenPane
import eu.xap3y.xagui.GuiMenu
import eu.xap3y.xagui.models.GuiButton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Connect4GameManager(private val plugin: ConnectFour = ConnectFour.instance) {

    private val playingPlayers = ConcurrentHashMap<Player, Player>()
    private val playerMapper = hashMapOf<UUID, PlayerModel>()

    /*private val borderPane = (XMaterial.BLACK_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1))
    private val redPane = (XMaterial.RED_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)).apply { itemMeta = itemMeta?.apply { displayName = Texter.colored("&cRed") } }
    private val yellowPane = XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1).apply { itemMeta = itemMeta?.apply { displayName = Texter.colored("&eYellow") } }
    //private val bluePane = XMaterial.BLUE_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)
    private val greenPane = XMaterial.LIME_STAINED_GLASS_PANE.parseItem() ?: ItemStack(Material.STAINED_GLASS_PANE, 1)*/

    private val gridMapper = hashMapOf<Int, Array<IntArray>>()

    fun isPlaying(player: Player): Boolean {
        return playingPlayers.containsKey(player) || playingPlayers.containsValue(player)
    }

    fun startGame(player: Player, opponent: Player) {

        var onMove: Int // 0 - player, 1 - opponent | 0 - red, 1 - yellow
        var end = false
        val gameId = (0..99999).random() // Random game ID in range 0-99999

        playingPlayers[player] = opponent

        val randomNum = (0..1).random() // 0 or 1

        playerMapper[player.uniqueId] = PlayerModel(if (randomNum == 0) 0 else 1)
        playerMapper[opponent.uniqueId] = PlayerModel(if (randomNum == 0) 1 else 0)

        // Create GUI
        val gui: GuiMenu = plugin.guiManager.createMenu("&bConnect4 &7| &e${player.name} &c- &e${opponent.name}", 6)

        // 2D Array
        gridMapper[gameId] = Array(6) { IntArray(7) { 0 } }

        // Static border
        val border: Set<Int> = setOf(26, 35, 7, 52)

        gui.fillSlots(gui.getCurrentPageIndex(), border, GuiButton(StaticItems.borderPane).setName(" "))

        // Constructing player heads
        val isFirstRed = randomNum == 0
        val skullPlayer: ItemStack = (XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.SKULL_ITEM,1)).apply {
            val skullPlayerMeta: SkullMeta = itemMeta as SkullMeta
            skullPlayerMeta.setOwner(player.name)
            itemMeta = skullPlayerMeta
        }
        gui.setSlot(17, GuiButton(skullPlayer).setName("&9${player.name}").setLore(" ", "&7&l| &7Color: ${if (isFirstRed) "&cRed" else "&eYellow"}"))
        gui.setSlot(8, GuiButton(if (isFirstRed) StaticItems.redPane.clone() else StaticItems.yellowPane.clone()).addItemFlag(ItemFlag.HIDE_ENCHANTS)) // Set player color (red or yellow

        val skullOpponent: ItemStack = (XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.SKULL_ITEM,1)).apply {
            val skullOpponentMeta: SkullMeta = itemMeta as SkullMeta
            skullOpponentMeta.setOwner(opponent.name)
            itemMeta = skullOpponentMeta
        }
        gui.setSlot(44, GuiButton(skullOpponent).setName("&9${opponent.name}").setLore(" ", "&7&l| &7Color: ${if (isFirstRed.not()) "&cRed" else "&eYellow"}"))
        gui.setSlot(53, GuiButton(if (isFirstRed.not()) StaticItems.redPane.clone() else StaticItems.yellowPane.clone()).addItemFlag(ItemFlag.HIDE_ENCHANTS)) // Set player color (red or yellow

        // get starting player
        onMove = (0..1).random()
        gui.setGlow(onMove, if (isFirstRed) 8 else 53)
        val playerOnMove = if (playerMapper[player.uniqueId]?.id == onMove) player else opponent
        gui.switchMove(onMove, playerOnMove.name)

        gui.setOnClose { event ->
            if (event.player.uniqueId == player.uniqueId)
                gui.close(opponent)
            else
                gui.close(player)
            playingPlayers.remove(player)
            playerMapper.remove(player.uniqueId)
            playerMapper.remove(opponent.uniqueId)
            gridMapper.remove(gameId)
        }

        gui.setOnClick { event ->
            if (event.currentItem == null || end) return@setOnClick
            val clickedColumn: Int = event.slot % 9
            //event.whoClicked.sendMessage("You clicked on slot $clickedColumn")
            if (clickedColumn > 6) return@setOnClick
            val playerNumber: Int = playerMapper[event.whoClicked.uniqueId]?.id ?: return@setOnClick
            if (onMove != playerNumber) return@setOnClick
            val (row: Int, column: Int) = dropToken(gameId, clickedColumn, playerNumber + 1) ?: return@setOnClick
            val nextPlayer: Player = if (event.whoClicked.uniqueId == player.uniqueId) opponent else player
            onMove = (onMove + 1) % 2
            gui.switchMove(onMove, nextPlayer.name)
            gui.setGlow(onMove, if (isFirstRed) 8 else 53)
            val button = GuiButton(if (playerNumber == 0) StaticItems.redPane.clone() else StaticItems.yellowPane.clone())
            gui.setSlot(row * 9 + column, button)

            val win = checkWin(gameId, playerNumber + 1) ?: return@setOnClick
            end = true
            win.forEach {
                gui.setSlot(it.first * 9 + it.second, GuiButton(greenPane.clone()).setName("&a&lWIN"))
            }

            Bukkit.getScheduler().runTaskLater(plugin, {
                gui.close(player)
            }, 20L * 2 + 10L)
        }

        // AND FINALLY OPEN THE GUI OMG
        gui.open(player)
        gui.open(opponent)
    }

    private fun dropToken(gameId: Int, column: Int, player: Int): Pair<Int, Int>? {
        val grid: Array<IntArray> = gridMapper[gameId] ?: return null
        for (row: Int in 5 downTo 0) {
            if (grid[row][column] == 0) {
                grid[row][column] = player
                return Pair(row, column)
            }
        }
        return null
    }

    private fun checkWin(gameId: Int, player: Int): List<Pair<Int, Int>>? {
        val grid: Array<IntArray> = gridMapper[gameId] ?: return null
        for (row: Int in 0..5) {
            for (col in 0..3) {
                if ((0..3).all { grid[row][col + it] == player }) {
                    return (0..3).map { row to (col + it) }
                }
            }
        }

        for (col: Int in 0..6) {
            for (row: Int in 0..2) {
                if ((0..3).all { grid[row + it][col] == player }) {
                    return (0..3).map { (row + it) to col }
                }
            }
        }

        for (row: Int in 0..2) {
            for (col: Int in 0..3) {
                if ((0..3).all { grid[row + it][col + it] == player }) {
                    return (0..3).map { (row + it) to (col + it) }
                }
            }
        }

        for (row: Int in 3..5) {
            for (col: Int in 0..3) {
                if ((0..3).all { grid[row - it][col + it] == player }) {
                    return (0..3).map { (row - it) to (col + it) }
                }
            }
        }
        return null
    }
}

data class PlayerModel(
    val id: Int,
    var moves: Int = 0
)

private val border: Set<Int> = setOf(16, 25, 34, 43)

// EXTENSION FUNCTION
fun GuiMenu.switchMove(int: Int, p0: String) {
    val item: GuiButton = if (int == 0) StaticItems.redPaneGlow else StaticItems.yellowPaneGlow
    val button: GuiButton = item.setLoreList(listOf(" ", "&7&l| &fOn move: &9$p0"))
    border.forEach { slot ->
        setSlot(slot, button)
    }
}

fun GuiMenu.setGlow(color: Int, slot: Int) {
    val realSlot = if (slot == 8 && color == 0) 8 else if (slot == 8 && color == 1) 53 else if (slot == 53 && color == 0) 8 else 53
    val item = if (color == 0) StaticItems.redPaneGlow else StaticItems.yellowPaneGlow
    val itemToRevert = if (color == 0) StaticItems.yellowPane.clone() else StaticItems.redPane.clone()
    setSlot(realSlot, item)
    setSlot(if (realSlot == 8) 53 else 8, itemToRevert)
}