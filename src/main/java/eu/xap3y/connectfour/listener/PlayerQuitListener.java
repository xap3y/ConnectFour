package eu.xap3y.connectfour.listener;

import eu.xap3y.connectfour.ConnectFour;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        ConnectFour.getGameManager().cancelGame(player, !event.getReason().equals(PlayerQuitEvent.QuitReason.DISCONNECTED));
    }
}
