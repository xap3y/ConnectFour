package eu.xap3y.connectfour.adapter;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.manager.LangManager;
import eu.xap3y.connectfour.service.Texter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class PaperAdapter {

    public static void sendInviteText(Player target, Player player, String lineHeader, String line2, String lineFooter,
                               String button1, String acceptHoverText,
                               String button2, String button2Hover,
                               String additionalInfo, String spacesText) {
        net.kyori.adventure.text.TextComponent tcomp = Component.text(spacesText)
                .append(
                        Component.text(Texter.colored(button1.replaceAll("&", "§")))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(acceptHoverText)))
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.callback((ctx) -> {
                                    ConnectFour.getInviteManager().accept(target, player);
                                }))
                )
                .append(Component.text("    "))
                .append(
                        Component.text(Texter.colored(button2.replaceAll("&", "§")))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(Texter.colored(button2Hover.replaceAll("&", "§")))))
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.callback((ctx) -> {
                                    if (!ConnectFour.getInviteManager().isInvitedBy(player, target)) {
                                        ConnectFour.getTexter().response(target, LangManager.getStringPrefixed("no_invites_from_player"), true, false);
                                        return;
                                    }
                                    ConnectFour.getInviteManager().reject(target, player);
                                }))
                )
                .append(Component.text(additionalInfo.replaceAll("&", "§")));
        if (lineHeader != null) target.sendMessage(Texter.colored(lineHeader));
        target.sendMessage(Texter.colored(Texter.centered(line2)));
        target.sendMessage(tcomp);
        if (lineFooter != null) target.sendMessage(Texter.colored(lineFooter));
    }


    public static void sendToPlayer(CommandSender player, String text) {
        player.sendMessage(ParseUtil.parseText(text));
    }

    public static ItemStack setName(ItemStack item, String name) {
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ParseUtil.parseText(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
