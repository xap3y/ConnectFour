package eu.xap3y.connectfour.adapter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ParseUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    // Legacy formatting codes (styling)
    private static final Map<Character, String> FORMAT_TAGS = new HashMap<>();
    static {
        FORMAT_TAGS.put('l', "bold");
        FORMAT_TAGS.put('o', "italic");
        FORMAT_TAGS.put('n', "underlined");
        FORMAT_TAGS.put('m', "strikethrough");
        FORMAT_TAGS.put('k', "obfuscated");
        FORMAT_TAGS.put('r', "reset");
    }

    // Legacy color codes (&0..&f) to MiniMessage names
    private static final Map<Character, String> COLOR_TAGS = new HashMap<>();
    static {
        COLOR_TAGS.put('0', "black");
        COLOR_TAGS.put('1', "dark_blue");
        COLOR_TAGS.put('2', "dark_green");
        COLOR_TAGS.put('3', "dark_aqua");
        COLOR_TAGS.put('4', "dark_red");
        COLOR_TAGS.put('5', "dark_purple");
        COLOR_TAGS.put('6', "gold");
        COLOR_TAGS.put('7', "gray");
        COLOR_TAGS.put('8', "dark_gray");
        COLOR_TAGS.put('9', "blue");
        COLOR_TAGS.put('a', "green");
        COLOR_TAGS.put('b', "aqua");
        COLOR_TAGS.put('c', "red");
        COLOR_TAGS.put('d', "light_purple");
        COLOR_TAGS.put('e', "yellow");
        COLOR_TAGS.put('f', "white");
    }

    // Regex for hash hex form: &#RRGGBB
    private static final Pattern HASH_HEX = Pattern.compile("(?i)&#([0-9a-f]{6})");
    // General tag presence check (quick heuristic)
    private static final Pattern ANY_TAG = Pattern.compile("<[^>]+>");

    private ParseUtil() {}

    public static Component parseText(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        // 1. Normalize section sign to ampersands
        String normalized = input.replace('§', '&');

        // 2. Convert nibble hex (&x&F&F&0&0&0&A) -> <#FF000A>
        normalized = convertNibbleHex(normalized);

        // 3. Convert hash hex (&#FF00AA) -> <#FF00AA>
        normalized = convertHashHex(normalized);

        // 4. Convert legacy color and format codes to MiniMessage tags
        // (We do this unconditionally so mixed inputs unify.)
        normalized = convertLegacyCodesToTags(normalized);

        // 5. Optional small cleanup: remove duplicate immediately consecutive formatting tags
        normalized = deduplicateAdjacentTags(normalized);

        boolean hasMiniTags = ANY_TAG.matcher(normalized).find();

        Component component;
        if (hasMiniTags) {
            try {
                component = MINI.deserialize(normalized);
            } catch (Exception ex) {
                // Fallback: do pure legacy parse on the original normalized (before conversions could be stored separately if desired)
                component = LegacyComponentSerializer.legacyAmpersand().deserialize(input.replace('§', '&'));
            }
        } else {
            // No tags created: treat as legacy only
            component = LegacyComponentSerializer.legacyAmpersand().deserialize(normalized);
        }

        // 6. Force non-italic unless explicitly set
        component = component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        return component;
    }

    private static String convertHashHex(String s) {
        Matcher m = HASH_HEX.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1).toUpperCase();
            m.appendReplacement(sb, "<#" + hex + ">");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // Convert &x&R&R&G&G&B&B sequences to <#RRGGBB>
    private static String convertNibbleHex(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            if (i + 13 < s.length()
                    && s.charAt(i) == '&'
                    && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X')
                    && isNibbleSequence(s, i)) {

                // Extract digits positions: i+3,5,7,9,11,13
                StringBuilder hex = new StringBuilder(6);
                for (int off = 3; off <= 13; off += 2) {
                    hex.append(s.charAt(i + off));
                }
                out.append("<#").append(hex.toString().toUpperCase()).append(">");
                i += 14;
            } else {
                out.append(s.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    private static boolean isNibbleSequence(String s, int start) {
        // Pattern: & x & h & h & h & h & h & h
        for (int k = 0; k < 6; k++) {
            int ampIndex = start + 2 + (k * 2);
            int hexIndex = ampIndex + 1;
            if (hexIndex >= s.length()) return false;
            if (s.charAt(ampIndex) != '&') return false;
            char c = s.charAt(hexIndex);
            if (!isHex(c)) return false;
        }
        return true;
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
    }

    // Convert all & codes (legacy) into MiniMessage tags
    private static String convertLegacyCodesToTags(String s) {
        StringBuilder out = new StringBuilder(s.length() + 32);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(i + 1));

                // Formatting
                if (FORMAT_TAGS.containsKey(code)) {
                    String tag = FORMAT_TAGS.get(code);
                    out.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
                // Color
                if (COLOR_TAGS.containsKey(code)) {
                    String tag = COLOR_TAGS.get(code);
                    out.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    // Remove immediate duplicates like <bold><bold>
    private static String deduplicateAdjacentTags(String s) {
        // Simple linear scan
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        String lastTag = null;
        while (i < s.length()) {
            if (s.charAt(i) == '<') {
                int close = s.indexOf('>', i);
                if (close == -1) {
                    // malformed, append rest
                    sb.append(s.substring(i));
                    break;
                }
                String tagContent = s.substring(i + 1, close);
                String fullTag = s.substring(i, close + 1);
                if (fullTag.equals(lastTag)) {
                    // skip duplicate
                } else {
                    sb.append(fullTag);
                    lastTag = fullTag;
                }
                i = close + 1;
            } else {
                sb.append(s.charAt(i));
                lastTag = null; // reset duplicate tracking across text boundaries
                i++;
            }
        }
        return sb.toString();
    }
}
