package com.somefrills.features.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.MiscCategory.NpcLocatorConfig;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.events.WorldTickEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.SkyblockData;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.somefrills.Main.mc;

public class NpcLocator extends Feature {
    private static final ConcurrentHashMap<String, NpcLocation> npcLocations = new ConcurrentHashMap<>();
    private static RenderColor color = new RenderColor(255, 100, 100, 255);
    private static Area cachedIsland = null;
    private static Map<String, Vec3d> cachedNpcs = new HashMap<>();
    private final NpcLocatorConfig config;

    public NpcLocator() {
        super(FrillsConfig.instance.misc.npcLocator.enabled);
        config = FrillsConfig.instance.misc.npcLocator;
        config.color.addObserver((oldVal, newVal) -> onColorConfigChanged(newVal));
        onColorConfigChanged(config.color.get());
    }

    private static void onColorConfigChanged(ChromaColour newColor) {
        color = RenderColor.fromChroma(newColor);
    }

    public static void addNpcLocation(String npcName) {
        Vec3d location = getNpcCoordinates(npcName);
        if (location != null) {
            npcLocations.put(npcName, new NpcLocation(npcName, location));
            Utils.info(Utils.format("Added {} to NPC Locator.", npcName));
        } else {
            Utils.info(Utils.format("Could not find NPC: {}", npcName));
        }
    }

    public static void removeNpcLocation(String npcName) {
        npcLocations.remove(npcName);
        Utils.info(Utils.format("Removed {} from NPC Locator.", npcName));
    }

    public static void clearAllNpcLocations() {
        npcLocations.clear();
        Utils.info("Cleared all NPC locations.");
    }

    public static Collection<NpcLocation> getAllNpcLocations() {
        return Collections.unmodifiableCollection(npcLocations.values());
    }

    public static Set<String> getAvailableNpcsForCurrentIsland() {
        ensureCacheLoaded();
        return cachedNpcs.keySet();
    }

    private static void ensureCacheLoaded() {
        Area currentIsland = SkyblockData.getArea();
        if (!Objects.equals(cachedIsland, currentIsland)) {
            cachedIsland = currentIsland;
            cachedNpcs = loadIslandNpcs(currentIsland);
        }
    }

    private static Vec3d getNpcCoordinates(String npcName) {
        ensureCacheLoaded();
        return cachedNpcs.get(npcName);
    }

    private static Map<String, Vec3d> loadIslandNpcs(Area area) {
        Map<String, Vec3d> npcs = new HashMap<>();

        String locationFileName = area.getDisplayName().replace(" ", "_").toUpperCase() + ".json";
        Path locationFile = FabricLoader.getInstance().getConfigDir()
                .resolve("skyhanni/repo/constants/island_graphs/" + locationFileName);

        if (!Files.exists(locationFile)) {
            return npcs;
        }

        try {
            String jsonContent = Files.readString(locationFile);
            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                JsonElement element = jsonObject.get(key);
                if (!element.isJsonObject()) continue;

                JsonObject node = element.getAsJsonObject();

                // Check if this node has the "npc" tag
                boolean isNpc = false;
                if (node.has("Tags") && node.get("Tags").isJsonArray()) {
                    var tagsArray = node.getAsJsonArray("Tags");
                    if (tagsArray != null) {
                        for (JsonElement tag : tagsArray) {
                            if (tag.isJsonPrimitive() && tag.getAsString().equals("npc")) {
                                isNpc = true;
                                break;
                            }
                        }
                    }
                }

                if (!isNpc) continue;

                // Extract NPC name and position
                if (!node.has("Name") || !node.has("Position")) continue;

                String npcName = node.get("Name").getAsString();
                String positionStr = node.get("Position").getAsString();

                Vec3d position = parsePosition(positionStr);
                if (position != null) {
                    npcs.put(npcName, position);
                }
            }
        } catch (IOException | RuntimeException e) {
            // Log error if needed
        }

        return npcs;
    }

    private static Vec3d parsePosition(String positionStr) {
        try {
            String[] parts = positionStr.split(":");
            if (parts.length != 3) return null;

            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);

            return new Vec3d(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @EventHandler
    public void onWorldTick(WorldTickEvent event) {
        if (!config.autoRemoveWaypoint) return;
        if (mc.player == null) return;
        if (npcLocations.isEmpty()) return;

        Vec3d playerPos = mc.player.getEyePos();
        npcLocations.entrySet().removeIf(entry -> {
            Vec3d npcPos = entry.getValue().position;
            double distance = playerPos.distanceTo(npcPos);
            return distance <= config.waypointRemoveDistance;
        });
    }

    @EventHandler
    public void onRenderEvent(WorldRenderEvent event) {
        for (var npc : npcLocations.values()) {
            Vec3d center = npc.position.add(0.5, 0, 0.5);
            if (config.beaconBeam) {
                event.drawBeam(center, 255, true, color);
            }
            if (config.tracer) {
                event.drawTracer(center, color);
            }
            if (config.outlineBox) {
                Box box = new Box(npc.position.subtract(0, 1, 0), npc.position.add(1, 1, 1));
                event.drawOutline(box, true, color);
            }
        }
    }

    public record NpcLocation(String npcName, Vec3d position) {
    }
}


