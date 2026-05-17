package com.somefrills.features.misc

import com.somefrills.config.FrillsMod

import com.somefrills.events.ChatMsgEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import meteordevelopment.orbit.EventHandler
import java.util.regex.Pattern

@FrillsFeature
object FilterMessages : Feature(FrillsMod.config.misc.chatFilter.enabled) {
    private val IMPLOSION_PATTERN: Pattern =
        Pattern.compile("Your Implosion hit (\\d+) enem(?:y|ies) for ([\\d,.]+) damage\\.")

    private fun shouldFilter(msg: String): Boolean {
        if (msg.contains("There are blocks in the way!")) {
            return true
        }
        return IMPLOSION_PATTERN.matcher(msg).matches()
    }

    @EventHandler
    private fun onChatMessage(event: ChatMsgEvent) {
        if (shouldFilter(event.plainMessage)) {
            event.cancel()
        }
    }
}