package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingJson;
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
        // additional aliases requested
        defaults.addProperty("crypt", "warp crypt");
        defaults.addProperty("deep", "warp deep");
        defaults.addProperty("gold", "warp gold");
        defaults.addProperty("bayou", "warp bayou");
        defaults.addProperty("park", "warp park");
        defaults.addProperty("void", "warp void");
        defaults.addProperty("mu", "warp museum");
        defaults.addProperty("rift", "warp rift");
        // "base" was listed twice in the provided list; add it once
        defaults.addProperty("base", "warp base");
        defaults.addProperty("cn", "warp cn");
        defaults.addProperty("mines", "warp mines");
        defaults.addProperty("forge", "warp forge");
        defaults.addProperty("trapper", "warp trapper");
        defaults.addProperty("desert", "warp desert");
        // short chat/commands
        defaults.addProperty("sc", "sax");
        defaults.addProperty("v2", "visit visit2");
        // handle both with and without accidental trailing space
        defaults.addProperty("sbs ", "skyblocker config");
        defaults.addProperty("sbs", "skyblocker config");
        aliases = new SettingJson(defaults);
    }


    public static String convertCommand(String message) {
        if(!instance.isActive()) return message;
        JsonObject obj = aliases.value();
        if (obj != null && obj.has(message)) {
            return obj.get(message).getAsString();
        }
        return message;
    }
}
