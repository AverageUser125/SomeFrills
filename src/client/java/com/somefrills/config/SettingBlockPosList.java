package com.somefrills.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience setting wrapper that stores a list of BlockPos under the key "waypoints" in the
 * feature's JSON object. Provides typed accessors to work with BlockPos directly.
 */
public class SettingBlockPosList extends SettingJson {
    public SettingBlockPosList(JsonObject defaultValue) {
        super(defaultValue);
    }

    public List<BlockPos> valueList() {
        JsonObject obj = this.value();
        List<BlockPos> res = new ArrayList<>();
        if (obj == null || !obj.has("waypoints")) return res;
        JsonElement e = obj.get("waypoints");
        if (!e.isJsonArray()) return res;
        JsonArray arr = e.getAsJsonArray();
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            try {
                int x = o.get("x").getAsInt();
                int y = o.get("y").getAsInt();
                int z = o.get("z").getAsInt();
                res.add(new BlockPos(x, y, z));
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    public void add(BlockPos pos) {
        this.edit(data -> {
            JsonArray arr = data.has("waypoints") ? data.getAsJsonArray("waypoints") : new JsonArray();
            JsonObject obj = new JsonObject();
            obj.addProperty("x", pos.getX());
            obj.addProperty("y", pos.getY());
            obj.addProperty("z", pos.getZ());
            arr.add(obj);
            data.add("waypoints", arr);
        });
    }

    public void removeLast() {
        this.edit(data -> {
            if (!data.has("waypoints")) return;
            JsonArray arr = data.getAsJsonArray("waypoints");
            if (arr.isEmpty()) return;
            arr.remove(arr.size() - 1);
            data.add("waypoints", arr);
        });
    }

    public void clearWaypoints() {
        this.edit(d -> d.add("waypoints", new JsonArray()));
    }
}
