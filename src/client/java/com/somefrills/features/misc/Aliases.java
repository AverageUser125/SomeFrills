package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingJson;
import com.somefrills.events.ModifyCommandEvent;
import meteordevelopment.orbit.EventHandler;
import com.google.gson.JsonObject;

public class Aliases {
    public static final Feature instance = new Feature(true);

    private static final SettingJson aliases;

    static {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("gd", "warp garden");
        defaults.addProperty("da", "warp da");
        defaults.addProperty("cc", "chat coop");
        defaults.addProperty("gc", "chat guild");
        defaults.addProperty("isle", "warp isle");
        defaults.addProperty("end", "warp end");
        aliases = new SettingJson(defaults);
    }

    @EventHandler
    private static void onCommand(ModifyCommandEvent event) {
        JsonObject obj = aliases.value();
        if (obj != null && obj.has(event.command)) {
            event.command = obj.get(event.command).getAsString();
        }
    }
}
