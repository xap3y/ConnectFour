package eu.xap3y.connectfour.service;

import eu.xap3y.connectfour.ConnectFour;
import eu.xap3y.connectfour.adapter.PaperAdapter;
import eu.xap3y.connectfour.api.dto.TextModifierDto;
import eu.xap3y.connectfour.api.dto.TexterObjDto;
import eu.xap3y.connectfour.api.enums.DefaultFontInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
public class Texter {
    private final TexterObjDto data;

    public Texter(TexterObjDto data) {
        this.data = data;
    }

    public Texter(String prefix, boolean debug, @Nullable File file) {
        this.data = new TexterObjDto(prefix, debug, file);
    }

    public void response(CommandSender p0, String text, TextModifierDto modifiers) {
        String textToSend = modifiers.colored() ? colored(text) : text;
        String prefix = modifiers.withPrefix() ? colored(data.getPrefix()) : "";
        String finalText = prefix + textToSend;
        if (ConnectFour.isPaper) {
            PaperAdapter.sendToPlayer(p0, finalText);
        } else {
            p0.sendMessage(finalText);
        }
    }

    public void response(CommandSender p0, String text, boolean colored, boolean wPrefix) {
        String textToSend = colored ? colored(text) : text;
        String prefix = wPrefix ? colored(data.getPrefix()) : "";
        String finalText = prefix + textToSend;
        if (ConnectFour.isPaper) {
            PaperAdapter.sendToPlayer(p0, finalText);
        } else {
            p0.sendMessage(finalText);
        }
    }

    public void response(CommandSender p0, String text) {
        response(p0, text, new TextModifierDto(true, true));
    }

    public void console(String text, TextModifierDto modifiers) {
        response(Bukkit.getConsoleSender(), text, modifiers);
    }

    public void console(String text, boolean wPrefix) {
        response(Bukkit.getConsoleSender(), text, new TextModifierDto(wPrefix, true));
    }

    public void console(String text) {
        response(Bukkit.getConsoleSender(), text);
    }

    public void debugLog(String text, Level level) {
        if (!data.isDebug() || data.getDebugFile() == null) return;

        File debugFile = data.getDebugFile();
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile();
            } catch (Exception e) {
                return;
            }
        }
        String levelName = level.getName() != null ? level.getName() : "";
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String textToLog = String.format("[%s] [%s] %s%n", currentTime, levelName, text);
        try {
            java.nio.file.Files.write(debugFile.toPath(), textToLog.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    public static String colored(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String centered(String message) {
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()) {
            if (c == '&') {
                previousCode = true;
                continue;
            } else if (previousCode) {
                previousCode = false;
                isBold = (c == 'l' || c == 'L');
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        StringBuilder sb = new StringBuilder();
        int compensated = 0;
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += DefaultFontInfo.SPACE.getLength() + 1;
        }
        return sb + message;
    }
}
