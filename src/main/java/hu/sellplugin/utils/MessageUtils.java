package hu.sellplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtils {

    private static final LegacyComponentSerializer LEGACY =
        LegacyComponentSerializer.legacyAmpersand();

    public static Component color(String message) {
        return LEGACY.deserialize(message);
    }

    public static String stripColor(String message) {
        return message.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }
}
