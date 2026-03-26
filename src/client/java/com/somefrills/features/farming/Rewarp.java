package com.somefrills.features.farming;

import com.google.gson.JsonObject;
import com.somefrills.config.Feature;
import com.somefrills.config.SettingBlockPosList;
import com.somefrills.config.SettingDescription;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Rendering;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.somefrills.Main.mc;

public class Rewarp {
    public static final Feature instance = new Feature("rewarp");
    // Use the feature key as the parent so settings are grouped under the feature
    @SettingDescription("Stored rewarp points (edited with commands)")
    public static SettingBlockPosList warps = new SettingBlockPosList(new JsonObject());

    // Add current player position as a waypoint (x,y,z)
    public static void addWaypoint() {
        if (mc.player == null) return;
        BlockPos pos = mc.player.getBlockPos();
        warps.add(pos);
        Utils.infoFormat("Added waypoint at {},{},{}.", pos.getX(), pos.getY(), pos.getZ());
    }

    // Remove the last waypoint (if any)
    public static void removeLastWaypoint() {
        warps.removeLast();
        Utils.info("Removed last waypoint.");
    }

    public static void clearWaypoints() {
        warps.clearWaypoints();
        Utils.info("Cleared all waypoints.");
    }

    @EventHandler
    public static void onWorldRender(WorldRenderEvent event) {
        // Only render if player exists
        if (mc.player == null) return;

        List<BlockPos> list = warps.valueList();
        if (list == null || list.isEmpty()) return;

        RenderColor cyan = RenderColor.fromHex(0x00FFFF, 0.4f);

        for (BlockPos pos : list) {
            try {
                Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                Box box = Box.of(center, 0.5, 0.5, 0.5);
                event.drawFilled(box, true, cyan);
            } catch (Exception ignored) {
            }
        }
    }
}
