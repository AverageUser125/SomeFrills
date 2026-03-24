package com.somefrills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.somefrills.Main;
import com.somefrills.misc.Utils;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.somefrills.Main.LOGGER;

public class Config {
    private static final Path folderPath = FabricLoader.getInstance().getConfigDir().resolve("SomeFrills");
    private static final Path filePath = folderPath.resolve("Configuration.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject data = new JsonObject();
    private static int hash = 0;

    public static Path getFolderPath() {
        return folderPath;
    }

    public static void load() {
        // minimal logging: only report load errors
        if (Files.exists(filePath)) {
            try {
                data = JsonParser.parseString(Files.readString(filePath)).getAsJsonObject();
            } catch (Exception exception) {
                LOGGER.error("Unable to load SomeFrills config file!", exception);
            }
        } else {
            save();
        }
        computeHash();
        // After loading config, subscribe all features that are enabled in the config
        reconcileFeatureSubscriptions();
    }

    public static void save() {
        // serialize under lock to avoid concurrent mutation while serializing and writing
        String json;
        int currentHash;
        synchronized (Config.class) {
            json = GSON.toJson(data);
            currentHash = json.hashCode();
            // skip saving if nothing changed
            if (currentHash == hash) {
                return;
            }
        }

        try {
            Utils.atomicWrite(filePath, json);

            synchronized (Config.class) {
                hash = currentHash;
            }

        } catch (Exception exception) {
            LOGGER.error("Unable to save SomeFrills config file!", exception);
        }
    }

    public static void saveAsync() {
        try {
            // try virtual threads first (Java 21+), fallback to normal thread otherwise
            Thread.startVirtualThread(Config::save);
        } catch (Throwable t) {
            Thread thread = new Thread(Config::save);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static void computeHash() {
        synchronized (Config.class) {
            hash = data.hashCode();
        }
    }

    public static JsonObject get() {
        return data;
    }

    private static void reconcileFeatureSubscriptions() {
        try {
            if (FeatureRegistry.getFeatures().isEmpty()) {
                FeatureRegistry.init();
            }
        } catch (Exception e) {
            LOGGER.debug("FeatureRegistry.init() failed during reconcile: {}", e.toString());
        }

        for (FeatureRegistry.FeatureInfo info : FeatureRegistry.getFeatures()) {
            try {
                Feature feat = info.featureInstance;
                if (feat == null) continue;
                if (feat.isActive()) {
                    Main.eventBus.subscribe(info.clazz);
                } else {
                    Main.eventBus.unsubscribe(info.clazz);
                }
            } catch (Throwable t) {
                LOGGER.debug("Error reconciling feature {}: {}", info.clazz.getName(), t.toString());
            }
        }
    }
}