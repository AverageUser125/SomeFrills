package com.example.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.example.Main.mc;

public class GlowTeamManager {

    private static final Map<Formatting, Team> TEAMS = new EnumMap<>(Formatting.class);
    private static final Map<String, Team> ORIGINAL_TEAMS = new HashMap<>();

    public static void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Scoreboard scoreboard = client.world.getScoreboard();

        for (Formatting color : Formatting.values()) {
            if (!color.isColor()) continue;

            String name = "examplemod_glow_" + color.getName();
            Team team = scoreboard.getTeam(name);

            if (team == null) {
                team = scoreboard.addTeam(name);
                team.setColor(color);
                team.setShowFriendlyInvisibles(false);
                team.setFriendlyFireAllowed(true);
            }

            TEAMS.put(color, team);
        }
    }

    public static void assign(String playerName, Formatting color) {
        if (mc.world == null) return;

        Team team = TEAMS.get(color);
        if (team == null) return;

        // Save original team if not already saved
        Scoreboard scoreboard = mc.world.getScoreboard();
        if (!ORIGINAL_TEAMS.containsKey(playerName)) {
            Team originalTeam = scoreboard.getScoreHolderTeam(playerName);
            if (originalTeam != null && !TEAMS.containsValue(originalTeam)) {
                ORIGINAL_TEAMS.put(playerName, originalTeam);
            }
        }

        scoreboard.addScoreHolderToTeam(playerName, team);
    }

    public static void remove(String playerName) {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        // Remove from glow team if in one
        for (Team team : TEAMS.values()) {
            if (scoreboard.getScoreHolderTeam(playerName) == team) {
                scoreboard.removeScoreHolderFromTeam(playerName, team);
                break;
            }
        }

        // Restore original team if one was saved
        Team originalTeam = ORIGINAL_TEAMS.remove(playerName);
        if (originalTeam != null) {
            scoreboard.addScoreHolderToTeam(playerName, originalTeam);
        }
    }

    public static void clear() {
        if (mc.world == null) return;

        Scoreboard scoreboard = mc.world.getScoreboard();

        // Remove all players from glow teams and restore their original teams
        for (String playerName : ORIGINAL_TEAMS.keySet()) {
            remove(playerName);
        }

        for (Team team : TEAMS.values()) {
            scoreboard.removeTeam(team);
        }

        TEAMS.clear();
        ORIGINAL_TEAMS.clear();
    }
}
