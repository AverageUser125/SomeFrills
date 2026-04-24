package com.somefrills.features.misc;

import com.google.gson.JsonObject;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.core.Feature;

public class Aliases extends Feature {

    private static final JsonObject aliases;

    static {
        aliases = new JsonObject();
        aliases.addProperty("gd", "warp garden");
        aliases.addProperty("da", "warp da");
        aliases.addProperty("cc", "chat coop");
        aliases.addProperty("gc", "chat guild");
        aliases.addProperty("isle", "warp isle");
        aliases.addProperty("end", "warp end");
        // additional aliases requested
        aliases.addProperty("crypt", "warp crypt");
        aliases.addProperty("deep", "warp deep");
        aliases.addProperty("gold", "warp gold");
        aliases.addProperty("bayou", "warp bayou");
        aliases.addProperty("park", "warp park");
        aliases.addProperty("void", "warp void");
        aliases.addProperty("mu", "warp museum");
        aliases.addProperty("rift", "warp rift");
        // "base" was listed twice in the provided list; add it once
        aliases.addProperty("base", "warp base");
        aliases.addProperty("cn", "warp cn");
        aliases.addProperty("mines", "warp mines");
        aliases.addProperty("forge", "warp forge");
        aliases.addProperty("trapper", "warp trapper");
        aliases.addProperty("desert", "warp desert");
        // short chat/commands
        aliases.addProperty("sc", "sax");
        aliases.addProperty("v2", "visit visit2");
        // handle both with and without accidental trailing space
        aliases.addProperty("sbs ", "skyblocker config");
        aliases.addProperty("sbs", "skyblocker config");
    }

    public Aliases() {
        super(FrillsConfig.instance.misc.commandAliases.enabled);
    }

    public static String convertCommand(String message) {
        if (!FrillsConfig.instance.misc.commandAliases.enabled.get()) return message;
        if (message == null || message.isEmpty()) return message;

        JsonObject obj = aliases;
        if (obj != null && obj.has(message)) {
            try {
                var element = obj.get(message);
                if (element != null && element.isJsonPrimitive()) {
                    return element.getAsString();
                }
            } catch (Exception e) {
                return message;
            }
        }
        return message;
    }
}
