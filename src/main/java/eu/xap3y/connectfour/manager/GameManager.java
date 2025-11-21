package eu.xap3y.connectfour.manager;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.destroystokyo.paper.profile.PlayerProfile;
import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.model.StaticItems;
import eu.xap3y.connectfour.service.Texter;
import eu.xap3y.connectfour.util.PlayerExtensions;
import eu.xap3y.xagui.GuiMenu;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static final Map<UUID, PlayerModel> playerMapper = new HashMap<>();
    private static final Map<UUID, Integer> bets = new HashMap<>();

    private final ConnectFour plugin;
    private final ConcurrentHashMap<Player, Player> playingPlayers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> timeToMove = new ConcurrentHashMap<>();
    private final List<UUID> exitingPlayers = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, int[][]> gridMapper = new HashMap<>();

    private static final Set<Integer> BORDER_SWITCH_SLOTS = Set.of(16, 25, 34, 43);

    public GameManager() {
        this(ConnectFour.getInstance());
    }

    public GameManager(ConnectFour plugin) {
        this.plugin = plugin;
    }

    public boolean isPlaying(Player player) {
        return playingPlayers.containsKey(player) || playingPlayers.containsValue(player);
    }

    // Cancel game due to player quit
    public void cancelGame(Player quitPlayer, boolean refundBoth) {

        if (!isPlaying(quitPlayer)) return;

        Player opponent = playingPlayers.get(quitPlayer);
        if (opponent == null) {
            // Find the opponent if quitPlayer is the opponent
            for (Map.Entry<Player, Player> entry : playingPlayers.entrySet()) {
                if (entry.getValue().equals(quitPlayer)) {
                    opponent = entry.getKey();
                    break;
                }
            }
        }

        if (opponent != null && opponent.isOnline()) {
            if (refundBoth) ConnectFour.getTexter().response(opponent, "&cYour opponent left the game, bets were refunded.", true, true);
            else {
                if (bets.containsKey(opponent.getUniqueId()) && bets.get(opponent.getUniqueId()) != null && bets.get(opponent.getUniqueId()) > 0) {
                    ConnectFour.getTexter().response(opponent, LangManager.getStringPrefixed("game_refund_other_cancel", mapOf("pot", String.valueOf(bets.get(opponent.getUniqueId()) * 2))), true, false);
                } else {
                    ConnectFour.getTexter().response(opponent, "&aYour opponent left the game, you won the game!", true, true);
                }

            }
            PlayerExtensions.ps(opponent, XSound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }

        playingPlayers.remove(quitPlayer);
        if (opponent != null) {
            playingPlayers.remove(opponent);
        }
        playerMapper.remove(quitPlayer.getUniqueId());
        if (opponent != null) {
            playerMapper.remove(opponent.getUniqueId());
        }
        exitingPlayers.remove(quitPlayer.getUniqueId());
        if (opponent != null) {
            exitingPlayers.remove(opponent.getUniqueId());
        }

        if (opponent != null) {

            Player finalOpponent = opponent;
            if (ConnectFour.isFolia()) {
                finalOpponent.getScheduler().run(plugin, (e) -> {
                    finalOpponent.closeInventory();
                }, null);
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    finalOpponent.closeInventory();
                });
            }

            if (refundBoth) {
                if (bets.containsKey(finalOpponent.getUniqueId())) {
                    Integer bet = bets.get(finalOpponent.getUniqueId());
                    if (bet != null && bet > 0 && ConnectFour.getInstance().getEconomy() != null) {
                        ConnectFour.getInstance().getEconomy().depositPlayer(finalOpponent, bet);
                    }
                    bets.remove(finalOpponent.getUniqueId());
                }
                if (bets.containsKey(quitPlayer.getUniqueId())) {
                    Integer bet = bets.get(quitPlayer.getUniqueId());
                    if (bet != null && bet > 0 && ConnectFour.getInstance().getEconomy() != null) {
                        ConnectFour.getInstance().getEconomy().depositPlayer(quitPlayer, bet);
                    }
                    bets.remove(quitPlayer.getUniqueId());
                }
            } else {
                bets.remove(quitPlayer.getUniqueId());
                if (bets.containsKey(finalOpponent.getUniqueId())) {
                    Integer bet = bets.get(finalOpponent.getUniqueId());
                    if (bet != null && bet > 0 && ConnectFour.getInstance().getEconomy() != null) {
                        ConnectFour.getInstance().getEconomy().depositPlayer(finalOpponent, bet*2);
                    }
                    bets.remove(finalOpponent.getUniqueId());
                }
            }
        }

    }

    public void startGame(Player player, Player opponent, Integer bet) {
        boolean ignoreEscKey = false;
        int onMove; // 0 - player, 1 - opponent | 0 - red, 1 - yellow
        boolean end = false;
        int gameId = ThreadLocalRandom.current().nextInt(0, 100000); // 0..99999
        final boolean[] falling = {false};
        final BukkitTask[] fallingTask = {null};
        final int[] totalMoves = {0};

        playingPlayers.put(player, opponent);

        bets.put(player.getUniqueId(), bet);
        bets.put(opponent.getUniqueId(), bet);

        int randomNum = ThreadLocalRandom.current().nextInt(0, 2); // 0 or 1
        boolean isFirstRed = randomNum == 0;

        ConnectFour.getConfigLoader().checkPlayer(player);
        ConnectFour.getConfigLoader().checkPlayer(opponent);

        // Create GUI
        String additionTitle = "";
        if (bet != null && bet > 0) {
            additionTitle = " &7(&fPOT: &a" + bet*2 + "$&7)";
        }
        String title = Optional.ofNullable(LangManager.getString("gui.title")).orElse("&6&lConnectFour") + additionTitle;
        GuiMenu gui = ConnectFour.getXagui().createMenu(title, 6);

        String yellowName = Optional.ofNullable(LangManager.getString("gui.yellow")).orElse("&eYellow");
        String redName = Optional.ofNullable(LangManager.getString("gui.red")).orElse("&cRed");

        // 2D Array
        gridMapper.put(gameId, new int[6][7]);

        // Static border (frame)
        gui.fillSlots(gui.getCurrentPageIndex(), new GuiButton(StaticItems.borderPane).setName(" "), 26, 35, 7, 52);

        // Player head
        ItemStack skullPlayer = Optional.ofNullable(XMaterial.PLAYER_HEAD.parseItem()).orElse(new ItemStack(Material.PLAYER_HEAD, 1));
        SkullMeta skullPlayerMeta = (SkullMeta) skullPlayer.getItemMeta();
        if (skullPlayerMeta != null) {
            if (ConnectFour.useOld) skullPlayerMeta.setOwner(player.getName());
            else skullPlayerMeta.setOwningPlayer(player);
            skullPlayer.setItemMeta(skullPlayerMeta);
        }

        if (!ConnectFour.useOld && ConnectFour.isPaper) {
            skullPlayer = getTexturedSkull(player.getName(), player.getUniqueId());
        }

        List<String> temp = LangManager.getListPrefixed("gui.skull.lore", mapOf(
                "color", isFirstRed ? redName : yellowName
        ));
        gui.setSlot(17, new GuiButton(skullPlayer)
                .setName(LangManager.getStringPrefixed("gui.skull.name", mapOf("player", player.getName())))
                .setLoreList(temp));

        ItemStack pane = isFirstRed ? StaticItems.redPane.clone() : StaticItems.yellowPane.clone();
        gui.setSlot(8, new GuiButton(pane).addItemFlag(ItemFlag.HIDE_ENCHANTS));

        // Opponent head
        ItemStack skullOpponent = Optional.ofNullable(XMaterial.PLAYER_HEAD.parseItem()).orElse(new ItemStack(Material.PLAYER_HEAD, 1));
        SkullMeta skullOpponentMeta = (SkullMeta) skullOpponent.getItemMeta();
        if (skullOpponentMeta != null) {
            if (ConnectFour.useOld) skullOpponentMeta.setOwner(opponent.getName());
            else skullOpponentMeta.setOwningPlayer(opponent);
            skullOpponent.setItemMeta(skullOpponentMeta);
        }
        temp = LangManager.getListPrefixed("gui.skull.lore", mapOf(
                "color", (!isFirstRed) ? redName : yellowName
        ));
        gui.setSlot(44, new GuiButton(skullOpponent)
                .setName(LangManager.getStringPrefixed("gui.skull.name", mapOf("player", opponent.getName())))
                .setLoreList(temp));

        gui.setSlot(53, new GuiButton((!isFirstRed ? StaticItems.redPane.clone() : StaticItems.yellowPane.clone()))
                .addItemFlag(ItemFlag.HIDE_ENCHANTS));

        // Show pot amount with a gold bar if there is a bet
        if (bet != null && bet > 0) {
            int pot = bet * 2;
            ItemStack gold = Optional.ofNullable(XMaterial.GOLD_INGOT.parseItem()).orElse(new ItemStack(Material.GOLD_INGOT, 1));
            String potName = Optional.ofNullable(LangManager.getStringPrefixed("gui.pot.name", mapOf("pot", String.valueOf(pot))))
                    .orElse(Texter.colored("&6&lPot: &e" + pot));
            List<String> potLore = Optional.ofNullable(LangManager.getListPrefixed("gui.pot.lore", mapOf("pot", String.valueOf(pot), "bet", String.valueOf(bet))))
                    .orElse(Arrays.asList(
                            Texter.colored("&7Each player bet: &6" + bet),
                            Texter.colored("&7Total pot: &e" + pot)
                    ));
            gui.setSlot(52, new GuiButton(gold).setName(potName).setLoreList(potLore));
        }

        // Map players to model
        playerMapper.put(player.getUniqueId(), new PlayerModel((randomNum == 0) ? 0 : 1, 0, isFirstRed, 8));
        playerMapper.put(opponent.getUniqueId(), new PlayerModel((randomNum == 0) ? 1 : 0, 0, !isFirstRed, 53));

        // get starting player (if inviterStarts -> inviter starts, else random)
        onMove = (!ConnectFour.getConfigModel().isInviterStart())
                ? ThreadLocalRandom.current().nextInt(0, 2)
                : Optional.ofNullable(playerMapper.get(player.getUniqueId())).map(pm -> pm.id).orElse(0);

        setGlow(gui, onMove);
        Player playerOnMove = Optional.ofNullable(playerMapper.get(player.getUniqueId()))
                .map(pm -> pm.id == onMove ? player : opponent)
                .orElse(player);
        switchMove(gui, onMove, playerOnMove.getName());

        final boolean[] onOpen = {false};
        gui.setOnOpen(event -> {
            if (onOpen[0]) return;
            onOpen[0] = true;
            PlayerExtensions.ps(player, XSound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            PlayerExtensions.ps(opponent, XSound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            ConnectFour.totalGames++;
        });

        final boolean[] oneClose = {false};
        final int[] onMoveRef = {onMove};
        final boolean[] ignoreEscKeyRef = {ignoreEscKey};
        final boolean[] endRef = {end};

        gui.setOnClose(event -> {
            if (ConnectFour.getConfigModel().isDoubleEscape() && !exitingPlayers.contains(event.getPlayer().getUniqueId()) && !ignoreEscKeyRef[0] && player.isOnline() && opponent.isOnline()) {
                Player p = (Player) event.getPlayer();
                if (p.isOnline()) {
                    exitingPlayers.add(p.getUniqueId());
                    ConnectFour.getTexter().response(p, LangManager.getStringPrefixed("esp_confirm"), true, false);
                    if (ConnectFour.isFolia()) {
                        plugin.getServer().getAsyncScheduler().runDelayed(plugin, (e) -> exitingPlayers.remove(p.getUniqueId()), 1500L, TimeUnit.MILLISECONDS);
                        plugin.getServer().getAsyncScheduler().runDelayed(plugin, (e) -> gui.open(p), 250L, TimeUnit.MILLISECONDS);
                    } else {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> exitingPlayers.remove(p.getUniqueId()), 30L);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> gui.open(p), 5L);
                    }
                    return;
                }
            } else if (ConnectFour.getConfigModel().isDoubleEscape() && exitingPlayers.contains(event.getPlayer().getUniqueId())) {
                ignoreEscKeyRef[0] = true;
            }

            if (oneClose[0]) return;
            oneClose[0] = true;

            if (player.isOnline() && opponent.isOnline()) {
                if (bet != null && bet > 0 && !endRef[0] && totalMoves[0] > 1) {
                    // Get the opposite player who did not close the GUI
                    Player refundTo = (event.getPlayer().getUniqueId().equals(player.getUniqueId())) ? this.playingPlayers.get(player) : player;
                    ConnectFour.getInstance().getEconomy().depositPlayer(refundTo, bet*2);
                    ConnectFour.getTexter().response(refundTo, LangManager.getStringPrefixed("game_refund_other_cancel", mapOf("pot", String.valueOf(bet*2))), true, false);
                    ConnectFour.getTexter().response(event.getPlayer(), LangManager.getStringPrefixed("game_lost_due_cancel", mapOf("bet", String.valueOf(bet))), true, false);
                } else if (bet != null && bet > 0 && !endRef[0]) {
                    // Refund bet if no moves were made
                    ConnectFour.getInstance().getEconomy().depositPlayer(player, bet);
                    ConnectFour.getInstance().getEconomy().depositPlayer(opponent, bet);
                    ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("game_refund_cancel", mapOf("bet", String.valueOf(bet))), true, false);
                    ConnectFour.getTexter().response(opponent, LangManager.getStringPrefixed("game_refund_cancel", mapOf("bet", String.valueOf(bet))), true, false);
                }
            }

            plugin.getOpenedGuis().remove(player);
            plugin.getOpenedGuis().remove(opponent);

            if (fallingTask[0] != null) {
                try { fallingTask[0].cancel(); } catch (Throwable ignored) {}
                fallingTask[0] = null;
            }

            if (event.getPlayer().getUniqueId().equals(player.getUniqueId()))
                gui.close(opponent);
            else
                gui.close(player);

            playingPlayers.remove(player);
            playerMapper.remove(player.getUniqueId());
            playerMapper.remove(opponent.getUniqueId());
            gridMapper.remove(gameId);
            exitingPlayers.remove(player.getUniqueId());
            exitingPlayers.remove(opponent.getUniqueId());
            bets.remove(player.getUniqueId());
            bets.remove(opponent.getUniqueId());

            Integer t1 = timeToMove.get(opponent.getUniqueId());
            if (t1 != null) Bukkit.getScheduler().cancelTask(t1);
            Integer t2 = timeToMove.get(player.getUniqueId());
            if (t2 != null) Bukkit.getScheduler().cancelTask(t2);

            if (!endRef[0]) {
                ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("game_closed"), true, false);
                ConnectFour.getTexter().response(opponent, LangManager.getStringPrefixed("game_closed"), true, false);
                PlayerExtensions.ps(player, XSound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                PlayerExtensions.ps(opponent, XSound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            }
        });

        gui.setOnClick(event -> {
            if (endRef[0] || falling[0]) return;

            int clickedColumn = event.getSlot() % 9;
            if (clickedColumn > 6) return;

            Player clicker = (Player) event.getWhoClicked();
            PlayerModel pm = playerMapper.get(clicker.getUniqueId());
            if (pm == null) return;
            int playerNumber = pm.id;

            if (onMoveRef[0] != playerNumber) return;

            Pair rc = dropToken(gameId, clickedColumn, playerNumber + 1);
            if (rc == null) return;

            totalMoves[0]++;
            PlayerExtensions.ps(player, XSound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
            PlayerExtensions.ps(opponent, XSound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

            // token ItemStack
            ItemStack button = (playerNumber == 0) ? StaticItems.redPane.clone() : StaticItems.yellowPane.clone();

            // Clear "Time left:" lines on skulls (17, 44)
            for (int slot : new int[]{17, 44}) {
                ItemStack is = gui.getInventory().getItem(slot);
                if (is == null) continue;
                if (is.getItemMeta() == null) continue;
                List<String> lore = is.getItemMeta().getLore();
                if (lore != null && lore.size() >= 2) {
                    String last = lore.get(lore.size() - 1);
                    if (last != null && last.contains("Time left:")) {
                        lore.remove(lore.size() - 1);
                        lore.remove(lore.size() - 1);
                        var im = is.getItemMeta();
                        im.setLore(lore);
                        is.setItemMeta(im);
                    }
                }
            }

            // Move timeout counter
            if (ConnectFour.getConfigModel().getMoveTimeout() > 0) {
                Integer tempId = timeToMove.get(clicker.getUniqueId());
                if (tempId != null) {
                    Bukkit.getScheduler().cancelTask(tempId);
                }

                Player playerToMove = clicker.getUniqueId().equals(player.getUniqueId()) ? opponent : player;
                final int[] current = {0};
                int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                    if (current[0] < ConnectFour.getConfigModel().getMoveTimeout()) {
                        int slot = playerToMove.getUniqueId().equals(player.getUniqueId()) ? 17 : 44;
                        ItemStack item = gui.getInventory().getItem(slot);
                        if (item != null && item.getItemMeta() != null) {
                            List<String> lore = item.getItemMeta().getLore();
                            if (lore != null) {
                                if (current[0] > 0 && lore.size() >= 2) {
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                }
                                lore.add(" ");
                                lore.add(Texter.colored("&cTime left: &f" + (ConnectFour.getConfigModel().getMoveTimeout() - current[0])));
                                var im = item.getItemMeta();
                                im.setLore(lore);
                                item.setItemMeta(im);
                            }
                        }
                    } else {
                        ignoreEscKeyRef[0] = true;
                        try { gui.close(opponent); } catch (Throwable ignored) {}
                        try { gui.close(player); } catch (Throwable ignored) {}
                        if (fallingTask[0] != null) {
                            try { fallingTask[0].cancel(); } catch (Throwable ignored) {}
                            fallingTask[0] = null;
                        }
                        ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("move_timeout", mapOf("player", playerToMove.getDisplayName())), true, false);
                        ConnectFour.getTexter().response(opponent, LangManager.getStringPrefixed("move_timeout", mapOf("player", playerToMove.getDisplayName())), true, false);
                    }
                    current[0]++;
                }, 0L, 20L).getTaskId();
                timeToMove.put(playerToMove.getUniqueId(), taskId);
            }

            // Falling animation
            fallingTask[0] = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (ConnectFour.getConfigModel().isTokenFallAnimation()) {
                    falling[0] = true;
                    for (int r = 0; r <= rc.row; r++) {
                        int newSlot = r * 9 + rc.col;
                        int oldSlot = (r - 1) * 9 + rc.col;
                        if (oldSlot > 0) gui.updateSlot(oldSlot, Material.AIR);
                        else gui.updateSlot(rc.col, Material.AIR);
                        gui.setSlot(newSlot, button);
                        try { Thread.sleep(ConnectFour.getConfigModel().getTokenFallSpeed()); } catch (InterruptedException ignored) {}
                    }
                    falling[0] = false;
                } else {
                    gui.setSlot(rc.row * 9 + rc.col, button);
                }

                Player nextPlayer = clicker.getUniqueId().equals(player.getUniqueId()) ? opponent : player;
                onMoveRef[0] = (onMoveRef[0] + 1) % 2;

                List<List<Pair>> win = findWinningPatterns(gameId, playerNumber + 1);
                if (totalMoves[0] < 42 && win == null) {
                    // Continue game
                    switchMove(gui, onMoveRef[0], nextPlayer.getName());
                    setGlow(gui, onMoveRef[0]);
                    return;
                } else if (totalMoves[0] > 41 && win == null) {
                    // DRAW
                    Integer t1 = timeToMove.get(opponent.getUniqueId());
                    if (t1 != null) Bukkit.getScheduler().cancelTask(t1);
                    Integer t2 = timeToMove.get(player.getUniqueId());
                    if (t2 != null) Bukkit.getScheduler().cancelTask(t2);
                    bets.remove(player.getUniqueId());
                    bets.remove(opponent.getUniqueId());
                    endRef[0] = true;
                    gui.fillSlots(gui.getCurrentPageIndex(),
                            new GuiButton(StaticItems.orangePane.clone()).setName(Optional.ofNullable(LangManager.getString("gui.draw")).orElse("&6&lDRAW")), 16, 25, 34, 43
                    );

                    if (bet != null && bet > 0) {
                        if (ConnectFour.getInstance().getEconomy() != null) {
                            ConnectFour.getInstance().getEconomy().depositPlayer(player, bet);
                            ConnectFour.getInstance().getEconomy().depositPlayer(opponent, bet);

                            Map<String, String> map = mapOf(
                                    "bet", String.valueOf(bet),
                                    "pot", String.valueOf(bet*2)
                            );

                            String text = LangManager.getStringPrefixed("draw_pot", map);

                            ConnectFour.getTexter().response(player, text, true, false);
                            ConnectFour.getTexter().response(opponent, text, true, false);
                        }
                    }

                    // stats: draws
                    ConnectFour.totalDraws++;
                    Optional.ofNullable(ConnectFour.getConfigLoader().data.get(player.getUniqueId().toString())).ifPresent(it -> {
                        it.setGamesPlayed(it.getGamesPlayed() + 1);
                        it.setDraws(it.getDraws() + 1);
                    });
                    Optional.ofNullable(ConnectFour.getConfigLoader().data.get(opponent.getUniqueId().toString())).ifPresent(it -> {
                        it.setGamesPlayed(it.getGamesPlayed() + 1);
                        it.setDraws(it.getDraws() + 1);
                    });
                    ConnectFour.getConfigLoader().savePlayerData(opponent);
                    ConnectFour.getConfigLoader().savePlayerData(player);
                } else {
                    // WIN
                    Integer t1 = timeToMove.get(opponent.getUniqueId());
                    if (t1 != null) Bukkit.getScheduler().cancelTask(t1);
                    Integer t2 = timeToMove.get(player.getUniqueId());
                    if (t2 != null) Bukkit.getScheduler().cancelTask(t2);
                    endRef[0] = true;
                    ignoreEscKeyRef[0] = true;

                    bets.remove(player.getUniqueId());
                    bets.remove(opponent.getUniqueId());

                    if (bet != null && bet > 0) {
                        if (ConnectFour.getInstance().getEconomy() != null) {
                            ConnectFour.getInstance().getEconomy().depositPlayer(clicker, bet*2);

                            Map<String, String> map = mapOf(
                                    "bet", String.valueOf(bet),
                                    "pot", String.valueOf(bet*2)
                            );

                            Player opposite = clicker.getUniqueId().equals(player.getUniqueId()) ? opponent : player;

                            ConnectFour.getTexter().response(clicker, LangManager.getStringPrefixed("won_bet", map), true, false);
                            ConnectFour.getTexter().response(opposite, LangManager.getStringPrefixed("lost_bet", map), true, false);
                        }
                    }

                    for (List<Pair> rows : win) {
                        for (Pair p : rows) {
                            gui.setSlot(p.row * 9 + p.col,
                                    new GuiButton(StaticItems.greenPane.clone())
                                            .setName(Optional.ofNullable(LangManager.getString("gui.win")).orElse("&a&lWIN"))
                            );
                        }
                    }

                    PlayerExtensions.ps(clicker, XSound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    Optional.ofNullable(ConnectFour.getConfigLoader().data.get(clicker.getUniqueId().toString())).ifPresent(it -> {
                        it.setGamesPlayed(it.getGamesPlayed() + 1);
                        it.setWins(it.getWins() + 1);
                        if (bet != null && bet > 0) {
                            it.setTotalWon(it.getTotalBet() + bet);
                        }
                    });

                    Optional.ofNullable(ConnectFour.getConfigLoader().data.get(nextPlayer.getUniqueId().toString())).ifPresent(it -> {
                        it.setGamesPlayed(it.getGamesPlayed() + 1);
                        it.setLosses(it.getLosses() + 1);
                        if (bet != null && bet > 0) {
                            it.setTotalLost(it.getTotalLost() + bet);
                        }
                    });

                    ConnectFour.getConfigLoader().savePlayerData(opponent);
                    ConnectFour.getConfigLoader().savePlayerData(player);

                    if (ConnectFour.getConfigModel().isWinRewardsEnable()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            List<String> cmds = ConnectFour.getConfigModel().getWinRewards();
                            if (cmds != null) {
                                for (String command : cmds) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", clicker.getName()));
                                }
                            }
                        });
                    }

                    gui.fillSlots(gui.getCurrentPageIndex(),
                            new GuiButton(StaticItems.borderPane.clone()).setName("&a&l►"), 16, 25, 34, 43
                    );

                    int paneSlot = Optional.ofNullable(playerMapper.get(clicker.getUniqueId()))
                            .map(m -> m.paneSlot).orElse(53);

                    String winnerText = Optional.ofNullable(LangManager.getString("gui.winner")).orElse("&e&lWINNER");

                    if (paneSlot == 8) {
                        gui.setSlot(paneSlot, StaticItems.greenPaneGlow.setName("&a&l▼&6&l▼ " + winnerText + " &a&l▼&6&l▼"));
                        gui.setSlot(paneSlot + 18, StaticItems.greenPaneGlow.setName("&a&l▲&6&l▲ " + winnerText + " &a&l▲&6&l▲"));
                    } else {
                        gui.setSlot(paneSlot, StaticItems.greenPaneGlow.setName("&a&l▲&6&l▲ " + winnerText + " &a&l▲&6&l▲"));
                        gui.setSlot(paneSlot - 18, StaticItems.greenPaneGlow.setName("&a&l▼&6&l▼ " + winnerText + " &a&l▼&6&l▼"));
                    }
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> gui.close(player), 20L * 2 + 10L);
            });
        });

        // Open the GUI for both players
        plugin.getOpenedGuis().add(player);
        plugin.getOpenedGuis().add(opponent);
        gui.open(player);
        gui.open(opponent);
    }

    private Pair dropToken(int gameId, int column, int player) {
        int[][] grid = gridMapper.get(gameId);
        if (grid == null) return null;
        for (int row = 5; row >= 0; row--) {
            if (grid[row][column] == 0) {
                grid[row][column] = player;
                return new Pair(row, column);
            }
        }
        return null;
    }

    private List<List<Pair>> findWinningPatterns(int gameId, int player) {
        int[][] board = gridMapper.get(gameId);
        if (board == null) return null;

        int rows = board.length;
        int cols = board[0].length;
        List<List<Pair>> winningPatterns = new ArrayList<>();

        int[][] directions = new int[][]{
                {1, 0},   // horizontal  -
                {0, 1},   // vertical    |
                {1, 1},   // diagonal    /
                {1, -1}   // diagonal    \
        };

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col] == player) {
                    for (int[] d : directions) {
                        int dr = d[0], dc = d[1];
                        int r = row, c = col;
                        List<Pair> pattern = new ArrayList<>();
                        while (r >= 0 && r < rows && c >= 0 && c < cols && board[r][c] == player) {
                            pattern.add(new Pair(r, c));
                            r += dr;
                            c += dc;
                        }
                        if (pattern.size() >= 4) {
                            winningPatterns.add(pattern);
                        }
                    }
                }
            }
        }
        return winningPatterns.isEmpty() ? null : winningPatterns;
    }

    private void switchMove(GuiMenu gui, int color, String playerName) {
        GuiButton item = (color == 0) ? StaticItems.redPaneGlow.setName(LangManager.getString("gui.red") != null ? LangManager.getString("gui.red") : "&cRed") : StaticItems.yellowPaneGlow.setName(LangManager.getString("gui.yellow") != null ? LangManager.getString("gui.yellow") : "&eYellow");
        List<String> list = LangManager.getListPrefixed("gui.border_item_lore", mapOf("player", playerName));
        GuiButton button = item.setLoreList(list);
        for (int slot : BORDER_SWITCH_SLOTS) {
            gui.setSlot(slot, button);
        }
    }

    private void setGlow(GuiMenu gui, int color) {
        PlayerModel next = playerMapper.values()
                .stream()
                .filter(pm -> pm.id == color)
                .findFirst()
                .orElse(null);
        if (next == null) return;

        GuiButton item = next.isRed ? StaticItems.redPaneGlow : StaticItems.yellowPaneGlow;
        int slot = next.paneSlot;
        ItemStack itemToRevert = next.isRed ? StaticItems.yellowPane.clone() : StaticItems.redPane.clone();

        gui.setSlot(slot, item.clearLore());
        gui.setSlot((slot == 8) ? 53 : 8, itemToRevert);
    }

    private static Map<String, String> mapOf(String k1, String v1) {
        Map<String, String> m = new HashMap<>();
        m.put(k1, v1);
        return m;
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        Map<String, String> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private static class Pair {
        final int row, col;
        Pair(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private static class PlayerModel {
        final int id;
        int moves;
        final boolean isRed;
        final int paneSlot;

        PlayerModel(int id, int moves, boolean isRed, int paneSlot) {
            this.id = id;
            this.moves = moves;
            this.isRed = isRed;
            this.paneSlot = paneSlot;
        }
    }

    private static ItemStack getSkullFromProfile(ItemStack head, PlayerProfile profile) {
        head.editMeta(SkullMeta.class, skullMeta -> {
            skullMeta.setPlayerProfile(profile);
        });

        return head;
    }

    private static ItemStack getTexturedSkull(String name, UUID uuid) {

        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        final PlayerProfile playerProfile = Bukkit.createProfile(uuid, name);

        return getSkullFromProfile(head, playerProfile);
    }
}