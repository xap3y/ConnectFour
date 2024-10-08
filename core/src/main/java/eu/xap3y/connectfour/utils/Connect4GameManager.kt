package eu.xap3y.connectfour.utils

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XSound
import eu.xap3y.connectfour.ConnectFour
import eu.xap3y.connectfour.ps
import eu.xap3y.connectfour.utils.StaticItems.greenPane
import eu.xap3y.xagui.GuiMenu
import eu.xap3y.xagui.models.GuiButton
import eu.xap3y.xalib.objects.TextModifier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val playerMapper = hashMapOf<UUID, PlayerModel>()

class Connect4GameManager(private val plugin: ConnectFour = ConnectFour.instance) {

    private val playingPlayers = ConcurrentHashMap<Player, Player>()

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
        var falling = false
        var fallingTask: BukkitTask? = null
        var totalMoves = 0

        playingPlayers[player] = opponent

        val randomNum = (0..1).random() // 0 or 1
        val isFirstRed: Boolean = randomNum == 0

        plugin.configLoader.checkPlayer(player)
        plugin.configLoader.checkPlayer(opponent)

        // Create GUI
        val gui: GuiMenu = plugin.guiManager.createMenu(LangManager.getString("gui.title") ?: "&6&lConnectFour", 6)

        val yellowName: String = LangManager.getString("gui.yellow") ?: "&eYellow"
        val redName: String = LangManager.getString("gui.red") ?: "&cRed"

        // 2D Array
        gridMapper[gameId] = Array(6) { IntArray(7) { 0 } }

        // Static border
        val border: Set<Int> = setOf(26, 35, 7, 52)

        gui.fillSlots(gui.getCurrentPageIndex(), border, GuiButton(StaticItems.borderPane).setName(" "))

        // Constructing player heads
        val skullPlayer: ItemStack = (XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.PLAYER_HEAD,1)).apply {
            val skullPlayerMeta: SkullMeta = itemMeta as SkullMeta
            if (ConnectFour.useOld) skullPlayerMeta.setOwner(player.name) else skullPlayerMeta.setOwningPlayer(player)
            itemMeta = skullPlayerMeta
        }


        var temp = LangManager.getListPrefixed("gui.skull.lore", hashMapOf(
            "color" to if (isFirstRed) redName else yellowName
        ))
        gui.setSlot(17, GuiButton(skullPlayer).setName(LangManager.getStringPrefixed("gui.skull.name", hashMapOf("player" to player.name))).setLoreList(temp))
        val pane = if (isFirstRed) StaticItems.redPane.clone() else StaticItems.yellowPane.clone()
        gui.setSlot(8, GuiButton(pane).addItemFlag(ItemFlag.HIDE_ENCHANTS)) // Set player color (red or yellow
        //if (isFirstRed) colorMapper[gameId] = Triple(playerMapper[player.uniqueId]?.id ?: 0, 8, true) else colorMapper[gameId] = Triple(playerMapper[player.uniqueId]?.id ?: 0, 8, false)

        val skullOpponent: ItemStack = (XMaterial.PLAYER_HEAD.parseItem() ?: ItemStack(Material.PLAYER_HEAD,1)).apply {
            val skullOpponentMeta: SkullMeta = itemMeta as SkullMeta
            if (ConnectFour.useOld) skullOpponentMeta.setOwner(opponent.name) else skullOpponentMeta.setOwningPlayer(opponent)
            itemMeta = skullOpponentMeta
        }
        temp = LangManager.getListPrefixed("gui.skull.lore", hashMapOf(
            "color" to if (isFirstRed.not()) redName else yellowName
        ))
        gui.setSlot(44, GuiButton(skullOpponent).setName(LangManager.getStringPrefixed("gui.skull.name", hashMapOf("player" to opponent.name))).setLoreList(temp))
        gui.setSlot(53, GuiButton(if (isFirstRed.not()) StaticItems.redPane.clone() else StaticItems.yellowPane.clone()).addItemFlag(ItemFlag.HIDE_ENCHANTS)) // Set player color (red or yellow

        playerMapper[player.uniqueId] = PlayerModel(if (randomNum == 0) 0 else 1, isRed = isFirstRed, paneSlot = 8)
        playerMapper[opponent.uniqueId] = PlayerModel(if (randomNum == 0) 1 else 0, isRed = isFirstRed.not(), paneSlot = 53)

        // get starting player
        onMove = if (plugin.configModel.inviterStart.not()) (0..1).random() else playerMapper[player.uniqueId]?.id ?: 0
        gui.setGlow(onMove)
        val playerOnMove: Player = if (playerMapper[player.uniqueId]?.id == onMove) player else opponent
        gui.switchMove(onMove, playerOnMove.name)

        /*plugin.texter.console("Player: ${player.name} | Opponent: ${opponent.name}")
        plugin.texter.console("isFirstRed: $isFirstRed | randomNum: $randomNum | onMove: $onMove")
        plugin.texter.console("PANE: ${pane.type}")*/

        var onOpen = false
        gui.setOnOpen {
            if (onOpen) return@setOnOpen
            onOpen = true
            player.ps(XSound.BLOCK_CHEST_OPEN)
            opponent.ps(XSound.BLOCK_CHEST_OPEN)
            ConnectFour.totalGames++
        }

        var oneClose = false
        gui.setOnClose { event ->
            if (oneClose) return@setOnClose
            oneClose = true
            plugin.openedGuis.remove(player)
            plugin.openedGuis.remove(opponent)
            //plugin.texter.console("CLOSE TRIGGERED")
            if (fallingTask != null) {
                fallingTask?.cancel()
                fallingTask = null
            }
            if (event.player.uniqueId == player.uniqueId)
                gui.close(opponent)
            else
                gui.close(player)
            playingPlayers.remove(player)
            playerMapper.remove(player.uniqueId)
            playerMapper.remove(opponent.uniqueId)
            gridMapper.remove(gameId)

            if (!end) {
                plugin.texter.response(player, LangManager.getStringPrefixed("game_closed"), TextModifier(false))
                plugin.texter.response(opponent, LangManager.getStringPrefixed("game_closed"), TextModifier(false))
                player.ps(XSound.BLOCK_ANVIL_LAND)
                opponent.ps(XSound.BLOCK_ANVIL_LAND)
            }
        }

        gui.setOnClick { event ->
            if (end || falling) return@setOnClick
            //if (ConnectFour.useOld && event.currentItem == null) return@setOnClick
            val clickedColumn: Int = event.slot % 9
            //event.whoClicked.sendMessage("You clicked on slot $clickedColumn")
            if (clickedColumn > 6) return@setOnClick
            val playerNumber: Int = playerMapper[event.whoClicked.uniqueId]?.id ?: return@setOnClick
            if (onMove != playerNumber) return@setOnClick
            val (row: Int, column: Int) = dropToken(gameId, clickedColumn, playerNumber + 1) ?: return@setOnClick
            totalMoves++
            player.ps(XSound.BLOCK_NOTE_BLOCK_HARP)
            opponent.ps(XSound.BLOCK_NOTE_BLOCK_HARP)

            // Fall animation
            val button: ItemStack = if (playerNumber == 0) StaticItems.redPane.clone() else StaticItems.yellowPane.clone()


            fallingTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                if (plugin.configModel.tokenFallAnimation) {
                    falling = true
                    for (r in 0 until row+1) {
                        //plugin.texter.console("Row: $r")
                        val newSlot = r * 9 + column
                        val oldSlot = (r - 1) * 9 + column
                        //plugin.texter.console("NEW: $newSlot | OLD: $oldSlot")
                        if (oldSlot > 0) gui.updateSlot(oldSlot, Material.AIR)
                        else gui.updateSlot(0 + column, Material.AIR)
                        gui.setSlot(newSlot, button)
                        /*gui.inventory.setItem(newSlot, button)*/
                        Thread.sleep(plugin.configModel.tokenFallSpeed)
                    }
                    falling = false
                } else
                    gui.setSlot(row * 9 + column, button)

                val nextPlayer: Player = if (event.whoClicked.uniqueId == player.uniqueId) opponent else player
                onMove = (onMove + 1) % 2
                //gui.setSlot(row * 9 + column, button)
                //val win = checkWin(gameId, playerNumber + 1) ?: return@setOnClick
                val win = findWinningPatterns(gameId, playerNumber + 1)
                if (totalMoves < 42 && win == null) {
                    gui.switchMove(onMove, nextPlayer.name)
                    gui.setGlow(onMove)
                    return@Runnable
                }
                else if (totalMoves > 41 && win == null) {
                    end = true
                    gui.fillSlots(gui.getCurrentPageIndex(), setOf(16, 25, 34, 43), GuiButton(StaticItems.orangePane.clone()).setName(LangManager.getString("gui.draw") ?: "&6&lDRAW"))
                    plugin.configLoader.data[player.uniqueId.toString()]?.let {
                        it.gamesPlayed++
                        it.draws++
                    }
                    plugin.configLoader.data[opponent.uniqueId.toString()]?.let {
                        it.gamesPlayed++
                        it.draws++
                    }
                    plugin.configLoader.saveData()
                } else if (win != null) {
                    end = true
                    win.forEach { rows ->
                        rows.forEach {
                            gui.setSlot(it.first * 9 + it.second, GuiButton(greenPane.clone()).setName(LangManager.getString("gui.win") ?: "&a&lWIN"))
                        }
                        //gui.setSlot(it.first * 9 + it.second, GuiButton(greenPane.clone()).setName("&a&lWIN"))
                    }

                    (event.whoClicked as Player).ps(XSound.ENTITY_PLAYER_LEVELUP)

                    plugin.configLoader.data[event.whoClicked.uniqueId.toString()]?.let {
                        it.gamesPlayed++
                        it.wins++
                    }

                    plugin.configLoader.data[nextPlayer.uniqueId.toString()]?.let {
                        it.gamesPlayed++
                        it.losses++
                    }
                    plugin.configLoader.saveData()

                    if (plugin.configModel.winRewardsEnable) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            plugin.configModel.winRewards?.forEach { command ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", event.whoClicked.name))
                            }
                        })
                    }

                    gui.fillSlots(gui.getCurrentPageIndex(), setOf(16, 25, 34, 43), GuiButton(StaticItems.borderPane.clone()).setName("&a&l►"))
                    val paneSlot = playerMapper[event.whoClicked.uniqueId]?.paneSlot ?: 53
                    val winnerText = LangManager.getString("gui.winner") ?: "&e&lWINNER"
                    if (paneSlot == 8) {
                        gui.setSlot(paneSlot, StaticItems.greenPaneGlow.setName("&a&l▼&6&l▼ $winnerText &a&l▼&6&l▼"))
                        gui.setSlot(paneSlot+18, StaticItems.greenPaneGlow.setName("&a&l▲&6&l▲ $winnerText &a&l▲&6&l▲"))
                    }
                    else {
                        gui.setSlot(paneSlot, StaticItems.greenPaneGlow.setName("&a&l▲&6&l▲ $winnerText &a&l▲&6&l▲"))
                        gui.setSlot(paneSlot-18, StaticItems.greenPaneGlow.setName("&a&l▼&6&l▼ $winnerText &a&l▼&6&l▼"))
                    }
                }
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    gui.close(player)
                }, 20L * 2 + 10L)
            })
        }

        // AND FINALLY OPEN THE GUI
        plugin.openedGuis.add(player)
        plugin.openedGuis.add(opponent)
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

    /*private fun checkWin(gameId: Int, player: Int): List<Pair<Int, Int>>? {
        val grid: Array<IntArray> = gridMapper[gameId] ?: return null
        for (row: Int in 0..5) {
            for (col in 0..3) {
                if ((0..3).all { grid[row][col + it] == player }) {
                    return (0..3).map { row to col + it }
                }
            }
        }

        for (col: Int in 0..6) {
            for (row: Int in 0..2) {
                if ((0..3).all { grid[row + it][col] == player }) {
                    return (0..3).map { row + it to col }
                }
            }
        }

        for (row: Int in 0..2) {
            for (col: Int in 0..3) {
                if ((0..3).all { grid[row + it][col + it] == player }) {
                    return (0..3).map { row + it to col + it }
                }
            }
        }

        for (row: Int in 3..5) {
            for (col: Int in 0..3) {
                if ((0..3).all { grid[row - it][col + it] == player }) {
                    return (0..3).map { row - it to col + it }
                }
            }
        }
        return null
    }*/

    private fun findWinningPatterns(gameId: Int, player: Int): List<List<Pair<Int, Int>>>? {
        val directions = listOf(
            Pair(1, 0),  // horizontal
            Pair(0, 1),  // vertical
            Pair(1, 1),  // diagonal /
            Pair(1, -1)  // diagonal \
        )
        val board = gridMapper[gameId] ?: return null
        val rows = board.size
        val cols = board[0].size
        val winningPatterns = mutableListOf<List<Pair<Int, Int>>>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (board[row][col] == player) {
                    for ((dr, dc) in directions) {
                        var r = row
                        var c = col
                        val pattern = mutableListOf<Pair<Int, Int>>()
                        while (r in 0 until rows && c in 0 until cols && board[r][c] == player) {
                            pattern.add(Pair(r, c))
                            r += dr
                            c += dc
                        }
                        if (pattern.size >= 4) {
                            winningPatterns.add(pattern)
                        }
                    }
                }
            }
        }
        if (winningPatterns.isEmpty()) return null
        return winningPatterns
    }
}

data class PlayerModel(
    val id: Int,
    var moves: Int = 0,
    var isRed: Boolean,
    var paneSlot: Int
)

private val border: Set<Int> = setOf(16, 25, 34, 43)

// EXTENSION FUNCTION
fun GuiMenu.switchMove(int: Int, p0: String) {
    val item: GuiButton = if (int == 0) StaticItems.redPaneGlow else StaticItems.yellowPaneGlow
    val list = LangManager.getListPrefixed("gui.border_item_lore", hashMapOf(
        "player" to p0
    ))
    val button: GuiButton = item.setLoreList(list)
    border.forEach { slot ->
        setSlot(slot, button)
    }
}

fun GuiMenu.setGlow(color: Int) {
    /*val isRed = colorMapper[gameId] ?: return
    val item = if (isRed) StaticItems.redPaneGlow else StaticItems.yellowPaneGlow
    val itemToRevert = if (isRed) StaticItems.yellowPane.clone() else StaticItems.redPane.clone()
    setSlot(if (color == 0) 8 else 53, item)
    setSlot(if (color == 0) 53 else 8, itemToRevert)*/

    val nextOnMovePlayer = playerMapper.values.firstOrNull { it.id == color } ?: return
    val item = if (nextOnMovePlayer.isRed) StaticItems.redPaneGlow else StaticItems.yellowPaneGlow
    val slot = nextOnMovePlayer.paneSlot
    val itemToRevert = if (nextOnMovePlayer.isRed) StaticItems.yellowPane.clone() else StaticItems.redPane.clone()
    setSlot(slot, item.clearLore())
    setSlot(if (slot == 8) 53 else 8, itemToRevert)
}