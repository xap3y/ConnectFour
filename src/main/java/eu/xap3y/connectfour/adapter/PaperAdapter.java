package eu.xap3y.connectfour.adapter;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.manager.LangManager;
import eu.xap3y.connectfour.service.Texter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PaperAdapter {

    public static void sendInviteText(Player target, Player player, String lineHeader, String line2, String lineFooter,
                               String button1, String acceptHoverText,
                               String button2, String button2Hover,
                               String additionalInfo, String spacesText) {

        net.kyori.adventure.text.TextComponent tcomp = Component.text(spacesText)
                .append(
                        ParseUtil.parseText(button1)
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(ParseUtil.parseText(acceptHoverText)))
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.callback((ctx) -> {
                                    ConnectFour.getInviteManager().accept(target, player);
                                }))
                )
                .append(Component.text("    "))
                .append(
                        ParseUtil.parseText(button2)
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(ParseUtil.parseText(button2Hover)))
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.callback((ctx) -> {
                                    if (!ConnectFour.getInviteManager().isInvitedBy(player, target)) {
                                        ConnectFour.getTexter().response(target, LangManager.getStringPrefixed("no_invites_from_player"), true, false);
                                        return;
                                    }
                                    ConnectFour.getInviteManager().reject(target, player);
                                }))
                )
                .append(ParseUtil.parseText(additionalInfo));
        if (lineHeader != null) target.sendMessage(ParseUtil.parseText(lineHeader));
        target.sendMessage(ParseUtil.parseText(Texter.centered(line2)));
        target.sendMessage(tcomp);
        if (lineFooter != null) target.sendMessage(ParseUtil.parseText(lineFooter));
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
