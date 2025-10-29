package eu.xap3y.connectfour.util;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlayerExtensions {

    public static void ps(Player p0, XSound p1, float p2, float p3) {
        if (p0 != null) {
            p1.play(p0, p2, p3);
        }
    }
}
