package com.somefrills.misc

import com.somefrills.Main
import com.somefrills.Main.mc
import com.somefrills.events.*
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.NetworkUtils
import com.somefrills.utils.SkyblockUtils
import com.somefrills.utils.Symbols
import com.somefrills.utils.toPlain
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.client.gui.hud.PlayerListHud
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.util.Formatting
import java.util.regex.Pattern

object SkyblockData {
    private val scoreRegex: Pattern = Pattern.compile("Team Score: [0-9]* (.*)")

    /**
     * Returns the current location from the scoreboard, such as "⏣ Your Island". The location prefix is not omitted.
     */
    @JvmStatic
    var location: String = ""
        private set

    /**
     * Returns the current area from the tab list, such as "Area: Private Island". The area/dungeon prefix is omitted.
     */
    @JvmStatic
    var area: Area = Area.UNKNOWN
        private set

    @JvmStatic
    var isInSkyblock: Boolean = false
        private set

    @JvmStatic
    var isInstanceOver: Boolean = false
        private set

    @JvmStatic
    var tabListLines: MutableList<String> = ArrayList()
        private set

    @JvmStatic
    var lines: MutableList<String> = ArrayList()
        private set
    private var showPing = false
    private var tabListDirty = true
    private var scoreboardDirty = true

    fun showPing() {
        showPing = true
        NetworkUtils.sendPingPacket()
    }

    @JvmStatic
    fun markTabListDirty() {
        tabListDirty = true
    }

    private fun updateTabListIfDirty() {
        val lines: MutableList<String> = ArrayList()
        if (mc.inGameHud == null || mc.player == null || mc.player?.networkHandler == null) return
        val playerListHud: PlayerListHud = mc.inGameHud.playerListHud ?: return

        for (entry in playerListHud.collectPlayerEntries()) {
            val displayName = entry.displayName?.toPlain() ?: continue
            val name = displayName.trim { it <= ' ' }
            if (name.isEmpty()) continue

            if (name.startsWith("Area: ") || name.startsWith("Dungeon: ")) {
                val areaStr = name.split(":".toRegex(), limit = 2).toTypedArray()[1].trim { it <= ' ' }
                val newArea = Area.fromString(areaStr)
                if (newArea != area) {
                    area = newArea
                    Main.LOGGER.info("Detected area change: {}", area.displayName)
                    AreaChangeEvent(area).post()
                }
            }
            lines.add(name)
        }
        tabListLines = lines
    }

    private fun updateTabList() {
        if (tabListDirty) {
            updateTabListIfDirty()
            tabListDirty = false
            TabListUpdateEvent(tabListLines).post()
        }
    }

    @JvmStatic
    fun updateObjective() {
        if (mc.player == null) return
        val scoreboard = mc.player?.networkHandler?.scoreboard ?: return
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1))
        if (objective != null) {
            isInSkyblock = objective.displayName.toPlain().contains("SKYBLOCK")
        }
    }

    @JvmStatic
    fun markScoreboardDirty() {
        scoreboardDirty = true
    }

    private fun updateScoreboardIfDirty() {
        if (mc.player != null) {
            val currentLines = ArrayList<String>()
            val scoreboard = mc.player?.networkHandler?.scoreboard ?: return
            val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1))
            for (scoreHolder in scoreboard.knownScoreHolders) {
                if (!scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) continue
                val team = scoreboard.getScoreHolderTeam(scoreHolder.nameForScoreboard)
                if (team != null) {
                    val line = Formatting.strip(team.prefix.string + team.suffix.string)!!
                        .trim { it <= ' ' }
                    if (!line.isEmpty()) {
                        if (line.startsWith(Symbols.zone) || line.startsWith(Symbols.zoneRift)) {
                            location = line
                        }
                        if (SkyblockUtils.isInKuudra() && !isInstanceOver) {
                            isInstanceOver = line.startsWith("Instance Shutdown")
                        }
                        currentLines.add(line)
                    }
                }
            }
            lines = currentLines
        }
    }

    private fun updateScoreboard() {
        if (scoreboardDirty) {
            updateScoreboardIfDirty()
            scoreboardDirty = false
        }
    }

    @EventHandler
    @JvmStatic
    private fun onChat(event: ChatMsgEvent) {
        if (!SkyblockUtils.isInDungeons()) return
        if (!isInstanceOver && scoreRegex.matcher(event.plainMessage.trim { it <= ' ' }).matches()) {
            isInstanceOver = true
        }
    }

    @EventHandler
    @JvmStatic
    private fun onJoinServer(event: ServerJoinEvent) {
        isInstanceOver = false
        isInSkyblock = false
        location = ""
        area = Area.UNKNOWN
        lines.clear()
    }

    @EventHandler
    @JvmStatic
    private fun onPing(event: ReceivePacketEvent) {
        if (!showPing) return
        event.packet.let {
            if (it is PingResultS2CPacket) {
                ChatUtils.infoFormat("§aPing: §f{}ms", it.startTime)
                showPing = false
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @JvmStatic
    private fun onWorldTick(event: TickEventPost?) {
        updateTabList()
        updateScoreboard()
    }
}