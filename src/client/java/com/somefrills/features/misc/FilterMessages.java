package com.somefrills.features.misc;
import com.somefrills.config.Feature;
import com.somefrills.events.ChatMsgEvent;
import com.somefrills.events.OverlayMsgEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FilterMessages {
    public static final Feature instance = new Feature();

    private static final Pattern IMPLOSION_PATTERN =
            Pattern.compile("Your Implosion hit (\\d+) enem(?:y|ies) for ([\\d,.]+) damage\\.");

    @EventHandler
    private static void onChatMessage(ChatMsgEvent event) {
        if (shouldFilter(event.messagePlain)) {
            event.cancel();
        }
    }

    private static boolean shouldFilter(String msg) {
        if (msg.contains("There are blocks in the way!")) {
            return true;
        }
        return IMPLOSION_PATTERN.matcher(msg).matches();
    }
}