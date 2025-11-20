package eu.xap3y.connectfour.manager;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.api.model.PlayerStatModel;
import eu.xap3y.connectfour.service.Texter;
import eu.xap3y.connectfour.util.PlayerExtensions;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InviteManager {

    private final ConnectFour plugin;

    @AllArgsConstructor
    private static class Invite {
        final Player inviter;
        final Player invited;
        int taskId;
        Integer bet;

        Invite(Player inviter, Player invited, int taskId) {
            this.inviter = inviter;
            this.invited = invited;
            this.taskId = taskId;
            this.bet = null;
        }
    }

    private final List<Invite> inviteMapper = new ArrayList<>();

    public InviteManager() {
        this(ConnectFour.getInstance());
    }

    public InviteManager(ConnectFour plugin) {
        this.plugin = plugin;
    }

    public void invite(Player player, Player target) {
        invite(player, target, null);
    }

    public void invite(Player player, Player target, Integer bet) {
        int taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            synchronized (inviteMapper) {
                inviteMapper.removeIf(i -> i.invited.getUniqueId().equals(target.getUniqueId()) && i.inviter.getUniqueId().equals(player.getUniqueId()));
            }
            ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("invite_expired"), true, false);
        }, 20L * ConnectFour.getConfigModel().getInviteTimeout()).getTaskId();

        synchronized (inviteMapper) {
            inviteMapper.add(new Invite(player, target, taskId, bet));
        }

        String lineHeader = LangManager.getString("invite.header");
        String lineFooter = LangManager.getString("invite.footer");
        String line2 = LangManager.getStringPrefixed("invite.text", java.util.Map.of("player", player.getName()));

        String button1 = Objects.requireNonNullElse(LangManager.getString("invite.accept.text"), "&7[&aACCEPT&7]");
        String button2 = Objects.requireNonNullElse(LangManager.getString("invite.reject.text"), "&7[&cREJECT&7]");
        String button1Hover = Objects.requireNonNullElse(LangManager.getString("invite.accept.hover"), "&aClick to accept");
        String button2Hover = Objects.requireNonNullElse(LangManager.getString("invite.reject.hover"), "&cClick to reject");

        String acceptExtraLore = null;
        if (bet != null) {
            int pot = bet * 2;
            acceptExtraLore =
                    Texter.colored("&7Bet: &a" + bet + "$ &7| Pot: &e" + pot);
        }

        String additionalInfo = (bet == null) ? "" : "  &7(Bet: &a" + bet + "$&7)";

        String textButtons = LangManager.getString("invite.buttons");
        if (textButtons == null) textButtons = button1 + "    " + button2;
        textButtons = textButtons.replace("{button1}", button1).replace("{button2}", button2);
        String recreateMid = Texter.centered(textButtons);

        int spaces = recreateMid.indexOf('&');
        if (spaces < 0) spaces = 0;
        String spacesText = recreateMid.substring(0, spaces);

        if (!ConnectFour.useTextComponents) {
            String text = LangManager.getStringPrefixed("invite.bukkit_invite", java.util.Map.of("player", player.getName()));
            ConnectFour.getTexter().response(target, text, true, false);
        } else if (ConnectFour.isPaper) {
            try {
                String acceptHoverText = Texter.colored(button1Hover.replaceAll("&", "§"));
                if (acceptExtraLore != null) {
                    acceptHoverText = acceptHoverText + "\n" + acceptExtraLore.replaceAll("&", "§");
                }

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
            } catch (Exception e) {
                ConnectFour.getTexter().response(target, LangManager.getStringPrefixed("invite_err"), true, false);
            }
        } else if (ConnectFour.useTextComponents && ConnectFour.useNew) {
            try {
                BaseComponent[] acceptHoverComponents;
                if (acceptExtraLore != null) {
                    acceptHoverComponents = new ComponentBuilder(Texter.colored(button1Hover))
                            .append("\n")
                            .append(acceptExtraLore)
                            .create();
                } else {
                    acceptHoverComponents = new BaseComponent[]{new TextComponent(Texter.colored(button1Hover))};
                }
                ComponentBuilder comp = new ComponentBuilder(spacesText)
                        .append(new ComponentBuilder(Texter.colored(button1))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, acceptHoverComponents))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/connectfour accept " + player.getName()))
                                .create()
                        )
                        .append(new ComponentBuilder("    ").create())
                        .append(new ComponentBuilder(Texter.colored(button2))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Texter.colored(button2Hover))}))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/connectfour reject " + player.getName()))
                                .create()
                        )
                        .append(new ComponentBuilder(Texter.colored(additionalInfo)).create());

                if (lineHeader != null) target.sendMessage(Texter.colored(lineHeader));
                target.sendMessage(Texter.colored(Texter.centered(line2)));
                target.spigot().sendMessage(comp.create());
                if (lineFooter != null) target.sendMessage(Texter.colored(lineFooter));
            } catch (Exception e) {
                ConnectFour.getTexter().response(target, LangManager.getStringPrefixed("invite_err"), true, false);
            }
        } else {
            TextComponent compSpaces = new TextComponent(spacesText);
            String acceptHoverText = Texter.colored(button1Hover);
            if (acceptExtraLore != null) {
                acceptHoverText = acceptHoverText + "\n" + acceptExtraLore;
            }
            TextComponent comp1 = new TextComponent(Texter.colored(button1));
            comp1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(acceptHoverText)}));
            comp1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/connectfour accept " + player.getName()));
            TextComponent compSpaceMid = new TextComponent("    ");
            TextComponent comp2 = new TextComponent(Texter.colored(button2));
            comp2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Texter.colored(button2Hover))}));
            comp2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/connectfour reject " + player.getName()));

            TextComponent comp3 = new TextComponent(Texter.colored(additionalInfo));

            if (lineHeader != null) target.sendMessage(Texter.colored(lineHeader));
            target.sendMessage(Texter.colored(Texter.centered(line2)));
            target.spigot().sendMessage(compSpaces, comp1, compSpaceMid, comp2, comp3);
            if (lineFooter != null) target.sendMessage(Texter.colored(lineFooter));
        }

        PlayerExtensions.ps(target, XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        PlayerExtensions.ps(player, XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        System.gc();
    }

    public boolean inviter(Player player) {
        synchronized (inviteMapper) {
            for (Invite i : inviteMapper) {
                if (i.inviter.getUniqueId().equals(player.getUniqueId())) return true;
            }
        }
        return false;
    }

    public boolean isInvitedBy(Player inviter, Player player) {
        synchronized (inviteMapper) {
            for (Invite i : inviteMapper) {
                if (i.inviter.getUniqueId().equals(inviter.getUniqueId()) && i.invited.getUniqueId().equals(player.getUniqueId())) return true;
            }
        }
        return false;
    }

    public void accept(Player player, Player inviter) {
        Invite data = null;
        synchronized (inviteMapper) {
            for (Invite i : inviteMapper) {
                if (i.invited.getUniqueId().equals(player.getUniqueId()) && i.inviter.getUniqueId().equals(inviter.getUniqueId())) {
                    data = i;
                    break;
                }
            }
        }
        if (data == null) return;

        Bukkit.getScheduler().cancelTask(data.taskId);
        synchronized (inviteMapper) {
            inviteMapper.remove(data);
        }

        if (data.bet != null) {
            double inviterBalance = ConnectFour.getInstance().getEconomy().getBalance(data.inviter);
            double targetBalance = ConnectFour.getInstance().getEconomy().getBalance(data.invited);

            if (inviterBalance < data.bet) {
                ConnectFour.getTexter().response(data.inviter, LangManager.getStringPrefixed("not_enough_money", java.util.Map.of("bet", data.bet + "")), true, false);
                ConnectFour.getTexter().response(data.invited, LangManager.getStringPrefixed("invite_accept_no_money_target", java.util.Map.of("player", data.inviter.getName(), "bet", data.bet + "")), true, false);
                return;
            }

            if (targetBalance < data.bet) {
                ConnectFour.getTexter().response(data.invited, LangManager.getStringPrefixed("not_enough_money", java.util.Map.of("bet", data.bet + "")), true, false);
                ConnectFour.getTexter().response(data.inviter, LangManager.getStringPrefixed("invite_accept_no_money_target", java.util.Map.of("player", data.invited.getName(), "bet", data.bet + "")), true, false);
                return;
            }

            ConnectFour.getInstance().getEconomy().withdrawPlayer(data.inviter, data.bet);
            ConnectFour.getInstance().getEconomy().withdrawPlayer(data.invited, data.bet);

            Invite finalData = data;
            Optional.ofNullable(ConnectFour.getConfigLoader().data.get(data.inviter.getUniqueId().toString())).ifPresent(it -> {
                it.setTotalBet(it.getTotalBet() + finalData.bet);
            });

            Optional.ofNullable(ConnectFour.getConfigLoader().data.get(data.invited.getUniqueId().toString())).ifPresent(it -> {
                it.setTotalBet(it.getTotalBet() + finalData.bet);
            });

            ConnectFour.getConfigLoader().savePlayerData(data.inviter);
            ConnectFour.getConfigLoader().savePlayerData(data.invited);

            data.inviter.sendMessage(Texter.colored(LangManager.getStringPrefixed("invite_accept_bet_withdraw", java.util.Map.of("bet", data.bet + ""))));
        }

        ConnectFour.getGameManager().startGame(data.inviter, player, data.bet);
        ConnectFour.getTexter().response(data.inviter, LangManager.getStringPrefixed("invite_accept_other", java.util.Map.of("player", player.getName())), true, false);
        ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("invite_accept_self"), true, false);
    }

    public void reject(Player player, Player inviter) {
        Invite data = null;
        synchronized (inviteMapper) {
            for (Invite i : inviteMapper) {
                if (i.invited.getUniqueId().equals(player.getUniqueId()) && i.inviter.getUniqueId().equals(inviter.getUniqueId())) {
                    data = i;
                    break;
                }
            }
        }
        if (data == null) return;

        Bukkit.getScheduler().cancelTask(data.taskId);
        synchronized (inviteMapper) {
            inviteMapper.remove(data);
        }

        PlayerExtensions.ps(data.inviter, XSound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        ConnectFour.getTexter().response(data.inviter, LangManager.getStringPrefixed("invite_reject_other", java.util.Map.of("player", player.getName())), true, false);
        ConnectFour.getTexter().response(player, LangManager.getStringPrefixed("invite_reject_self"), true, false);
    }
}