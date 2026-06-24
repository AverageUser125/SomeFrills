package com.somefrills.features.farming

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod
import com.somefrills.events.ChatMsgEvent
import com.somefrills.events.ServerJoinEvent
import com.somefrills.features.core.AreaFeature
import com.somefrills.misc.Area
import com.somefrills.modules.FrillsFeature
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.PlayerUtils
import com.somefrills.utils.SkyblockUtils
import com.somefrills.events.core.EventHandle
import java.util.regex.Matcher
import java.util.regex.Pattern

@FrillsFeature
object AutoPestSetHome : AreaFeature(FrillsMod.config.farming.autoPestSetHomeEnabled) {
    private const val IGNORE_WINDOW_MS = 10000L
    private val PEST_SPAWN_PATTERN: Pattern = Pattern.compile(
        "\\bPest[s]?\\b.*?spawn(?:ed)?\\b.*?Plot\\s*-?\\s*\\d+",
        Pattern.CASE_INSENSITIVE
    )
    private var lastServerJoinTime = 0L

    @EventHandle
    private fun onServerJoin(event: ServerJoinEvent) {
        lastServerJoinTime = System.currentTimeMillis()
    }

    @EventHandle
    private fun onChatMessage(event: ChatMsgEvent) {
        val msg = event.plainMessage
        if (msg.isEmpty()) return
        val m: Matcher = PEST_SPAWN_PATTERN.matcher(msg)
        if (!m.find()) return

        val now = System.currentTimeMillis()
        if (now - lastServerJoinTime < IGNORE_WINDOW_MS) return

        if (mc.player == null || mc.player?.packetContext == null) return

        try {
            ChatUtils.info("Pests spawned detected in chat, running /sethome")
            PlayerUtils.runCommand("sethome")
        } catch (_: Exception) {
        }
    }

    override fun checkArea(area: Area): Boolean {
        return area == Area.GARDEN && SkyblockUtils.isOnGardenPlot()
    }
}