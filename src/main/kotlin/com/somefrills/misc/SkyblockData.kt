package com.somefrills.misc

import com.somefrills.Main.mc
import com.somefrills.events.*
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

    val isInInstance: Boolean
        get() = Utils.isInDungeons() || Utils.isInKuudra()

    fun showPing() {
        showPing = true
        Utils.sendPingPacket()
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
            if (entry == null || entry.displayName == null) continue

            val name = Utils.toPlain(entry.displayName).trim { it <= ' ' }
            if (name.isEmpty()) continue

            if (name.startsWith("Area: ") || name.startsWith("Dungeon: ")) {
                val areaStr = name.split(":".toRegex(), limit = 2).toTypedArray()[1].trim { it <= ' ' }
                val newArea = Area.fromString(areaStr)
                if (newArea != area) {
                    area = newArea
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
            isInSkyblock = Utils.toPlain(objective.displayName).contains("SKYBLOCK")
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
                        if (line.startsWith(Utils.Symbols.zone) || line.startsWith(Utils.Symbols.zoneRift)) {
                            location = line
                        }
                        if (Utils.isInKuudra() && !isInstanceOver) {
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
    private fun onChat(event: ChatMsgEvent) {
        if (!Utils.isInDungeons()) return
        if (!isInstanceOver && scoreRegex.matcher(event.plainMessage.trim { it <= ' ' }).matches()) {
            isInstanceOver = true
        }
    }

    @EventHandler
    private fun onJoinServer(event: ServerJoinEvent) {
        isInstanceOver = false
        isInSkyblock = false
        location = ""
        area = Area.UNKNOWN
        lines.clear()
    }

    @EventHandler
    private fun onPing(event: ReceivePacketEvent) {
        if (!showPing) return
        event.packet.let {
            if (it is PingResultS2CPacket) {
                Utils.infoFormat("§aPing: §f{}ms", it.startTime)
                showPing = false
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onWorldTick(event: TickEventPost?) {
        updateTabList()
        updateScoreboard()
    }
}