package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ChatMsgEvent;
import meteordevelopment.orbit.EventHandler;

import java.util.regex.Pattern;

public class FilterMessages extends Feature {

    private static final Pattern IMPLOSION_PATTERN =
            Pattern.compile("Your Implosion hit (\\d+) enem(?:y|ies) for ([\\d,.]+) damage\\.");

    public FilterMessages() {
        super(FrillsConfig.instance.misc.chatFilter.enabled);
    }

    private static boolean shouldFilter(String msg) {
        if (msg.contains("There are blocks in the way!")) {
            return true;
        }
        return IMPLOSION_PATTERN.matcher(msg).matches();
    }

    @EventHandler
    private void onChatMessage(ChatMsgEvent event) {
        if (shouldFilter(event.messagePlain)) {
            event.cancel();
        }
    }
}