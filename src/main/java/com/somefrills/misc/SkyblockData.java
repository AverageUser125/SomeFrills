package com.somefrills.misc;

import com.somefrills.events.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

public class SkyblockData {
    private static final Pattern scoreRegex = Pattern.compile("Team Score: [0-9]* (.*)");
    private static String location = "";
    private static Area area = Area.UNKNOWN;
    private static boolean inSkyblock = false;
    private static boolean instanceOver = false;
    private static List<String> tabListLines = new ArrayList<>();
    private static List<String> lines = new ArrayList<>();
    private static boolean showPing = false;
    private static boolean tabListDirty = true;
    private static boolean scoreboardDirty = true;

    /**
     * Returns the current location from the scoreboard, such as "⏣ Your Island". The location prefix is not omitted.
     */
    public static String getLocation() {
        return location;
    }

    /**
     * Returns the current area from the tab list, such as "Area: Private Island". The area/dungeon prefix is omitted.
     */
    public static Area getArea() {
        return area;
    }

    public static boolean isInSkyblock() {
        return inSkyblock;
    }

    public static boolean isInInstance() {
        return Utils.isInDungeons() || Utils.isInKuudra();
    }

    public static boolean isInstanceOver() {
        return instanceOver;
    }

    public static List<String> getTabListLines() {
        return tabListLines;
    }

    public static List<String> getLines() {
        return lines;
    }

    public static void showPing() {
        showPing = true;
        Utils.sendPingPacket();
    }

    public static void markTabListDirty() {
        tabListDirty = true;
    }

    private static void updateTabListIfDirty() {
        List<String> lines = new ArrayList<>();
        if (mc.inGameHud == null || mc.player == null || mc.player.networkHandler == null) return;
        var playerListHud = mc.inGameHud.getPlayerListHud();
        if (playerListHud == null) return;

        for (PlayerListEntry entry : playerListHud.collectPlayerEntries()) {
            if (entry == null || entry.getDisplayName() == null) continue;

            String name = Utils.toPlain(entry.getDisplayName()).trim();
            if (name.isEmpty()) continue;

            if (name.startsWith("Area: ") || name.startsWith("Dungeon: ")) {
                String areaStr = name.split(":", 2)[1].trim();
                Area newArea = Area.fromString(areaStr);
                if (newArea != area) {
                    area = newArea;
                    eventBus.post(new AreaChangeEvent(area));
                }
            }
            lines.add(name);
        }
        tabListLines = lines;
    }

    private static void updateTabList() {
        if (tabListDirty) {
            updateTabListIfDirty();
            tabListDirty = false;
            eventBus.post(new TabListUpdateEvent(tabListLines));
        }
    }

    public static void updateObjective() {
        if (mc.player != null) {
            Scoreboard scoreboard = mc.player.networkHandler.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            if (objective != null) {
                inSkyblock = Utils.toPlain(objective.getDisplayName()).contains("SKYBLOCK");
            }
        }
    }

    public static void markScoreboardDirty() {
        scoreboardDirty = true;
    }

    private static void updateScoreboardIfDirty() {
        if (mc.player != null) {
            List<String> currentLines = new ArrayList<>();
            Scoreboard scoreboard = mc.player.networkHandler.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());
                    if (team != null) {
                        String line = Formatting.strip(team.getPrefix().getString() + team.getSuffix().getString()).trim();
                        if (!line.isEmpty()) {
                            if (line.startsWith(Utils.Symbols.zone) || line.startsWith(Utils.Symbols.zoneRift)) {
                                location = line;
                            }
                            if (Utils.isInKuudra() && !instanceOver) {
                                instanceOver = line.startsWith("Instance Shutdown");
                            }
                            currentLines.add(line);
                        }
                    }
                }
            }
            lines = currentLines;
        }
    }

    private static void updateScoreboard() {
        if (scoreboardDirty) {
            updateScoreboardIfDirty();
            scoreboardDirty = false;
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (Utils.isInDungeons()) {
            if (!instanceOver && scoreRegex.matcher(event.messagePlain.trim()).matches()) {
                instanceOver = true;
            }
        }
    }

    @EventHandler
    private static void onJoinServer(ServerJoinEvent event) {
        instanceOver = false;
        inSkyblock = false;
        location = "";
        area = Area.UNKNOWN;
        lines.clear();
    }

    @EventHandler
    private static void onPing(ReceivePacketEvent event) {
        if (showPing && event.packet instanceof PingResultS2CPacket(long startTime)) {
            Utils.infoFormat("§aPing: §f{}ms", Util.getMeasuringTimeMs() - startTime);
            showPing = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onWorldTick(TickEventPost event) {
        updateTabList();
        updateScoreboard();
    }
}