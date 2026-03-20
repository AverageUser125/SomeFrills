package com.somefrills.features.farming;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.somefrills.config.Feature;
import com.somefrills.config.SettingJson;
import com.somefrills.config.SettingDescription;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Rendering;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static com.somefrills.Main.mc;

public class Rewarp {
    public static final Feature instance = new Feature("rewarp");
    // Use the feature key as the parent so settings are grouped under the feature
    @SettingDescription("Stored rewarp points (edited with commands)")
    public static SettingJson warps = new SettingJson(new JsonObject());

    // Add current player position as a waypoint (x,y,z)
    public static void addWaypoint() {
        if (mc.player == null) return;
        BlockPos pos = mc.player.getBlockPos();
        warps.edit(data -> {
            JsonArray arr = data.has("waypoints") ? data.getAsJsonArray("waypoints") : new JsonArray();
            JsonObject obj = new JsonObject();
            obj.addProperty("x", pos.getX());
            obj.addProperty("y", pos.getY());
            obj.addProperty("z", pos.getZ());
            arr.add(obj);
            data.add("waypoints", arr);
        });
        Utils.infoFormat("Added waypoint at {},{},{}.", pos.getX(), pos.getY(), pos.getZ());
    }

    // Remove the last waypoint (if any)
    public static void removeLastWaypoint() {
        warps.edit(data -> {
            if (!data.has("waypoints")) return;
            JsonArray arr = data.getAsJsonArray("waypoints");
            if (arr.size() == 0) return;
            arr.remove(arr.size() - 1);
            data.add("waypoints", arr);
        });
        Utils.info("Removed last waypoint.");
    }

    public static void clearWaypoints() {
        warps.edit(d -> d.add("waypoints", new JsonArray()));
        Utils.info("Cleared all waypoints.");
    }

    @EventHandler
    public static void onWorldRender(WorldRenderEvent event) {
        // Only render if player exists
        if (mc.player == null) return;

        JsonObject data = warps.value();
        if (data == null || !data.has("waypoints")) return;

        JsonArray arr = data.getAsJsonArray("waypoints");
        if (arr == null) return;

        RenderColor cyan = RenderColor.fromHex(0x00FFFF, 0.4f);

        for (JsonElement e : arr) {
            try {
                if (!e.isJsonObject()) continue;
                JsonObject obj = e.getAsJsonObject();
                int x = obj.get("x").getAsInt();
                int y = obj.get("y").getAsInt();
                int z = obj.get("z").getAsInt();
                Vec3d center = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
                Box box = Box.of(center, 0.5, 0.5, 0.5);
                Rendering.drawFilled(event.matrices, event.consumer, event.camera, box, true, cyan);
            } catch (Exception ignored) {
            }
        }
    }
}
