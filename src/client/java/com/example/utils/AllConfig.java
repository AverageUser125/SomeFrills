package com.example.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for GlowPlayer mod.
 * Automatically serializes/deserializes all public static fields to/from JSON using reflection.
 * Config file: ~/.minecraft/config/glowplayer.json
 */
public class AllConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("glowplayer.json");
    // Experiment Solver settings
    public static boolean enableChronomatron = true;  // enable Chronomatron solver
    public static boolean enableUltrasequencer = true; // enable Ultrasequencer solver
    public static long clickDelay = 200;        // ms between clicks (0-1000)
    public static boolean autoClose = true;     // auto-close GUI after completing minigame
    public static int serumCount = 3;           // consumed metaphysical serum count (0-3)
    public static boolean getMaxXp = false;     // solve for max XP (Chroma 15, Ultra 20)
    // Creeper settings
    public static boolean creeperNotInvisible = true;  // make creepers not invisible (show them)
    public static boolean creeperNotCharged = true;    // hide charged creeper effect
    public static boolean creeperShowHP = true;        // show creeper HP when not invisible
    // Chocolate Factory settings
    public static boolean claimStray = false;          // auto-claim stray rabbits
    public static long claimDelay = 150;               // ms between claim clicks (0-1500)
    // RNG Meter Display settings
    public static boolean showRngMeter = true;         // show RNG meter information
    public static boolean debugRngDisplay = false;     // enable debug output for RNG display
    public static boolean gemstoneDsyncFix = true;
    public static boolean breakResetFix = true;
    public static boolean itemCountFix = true;
    public static boolean noPearlCooldown = true;
    public static boolean middleClickFix = true;
    public static boolean doubleUseFix = true;
    public static boolean middleClickOverride = true;
    public static boolean noLoadingScreen = false;
    public static boolean disconnectFix = true;
    public static boolean animationsFix = true;
    public static boolean noAbilityPlace = true;

    static {
        load();
    }

    /**
     * Automatically save all public static fields to JSON.
     * Uses reflection to dynamically serialize.
     */
    public static void save() {
        try {
            Map<String, Object> configData = new HashMap<>();

            // Reflect over all public static fields
            for (Field field : AllConfig.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                // Only serialize public static non-final fields
                if (java.lang.reflect.Modifier.isPublic(modifiers) &&
                        java.lang.reflect.Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);
                    configData.put(field.getName(), field.get(null));
                }
            }

            // Ensure config directory exists
            Files.createDirectories(CONFIG_FILE.getParent());

            // Write to file
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(configData, writer);
            }
        } catch (IOException | IllegalAccessException e) {
            System.err.println("Failed to save GlowPlayer config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Automatically load all public static fields from JSON.
     * Uses reflection to dynamically deserialize.
     * Missing properties will use their default values.
     */
    public static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            // Config file doesn't exist yet; use defaults
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> configData = GSON.fromJson(reader, Map.class);

            if (configData == null) return;

            // Reflect over all public static fields and set from JSON
            for (Field field : AllConfig.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(modifiers) &&
                        java.lang.reflect.Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);

                    if (configData.containsKey(field.getName())) {
                        Object value = configData.get(field.getName());

                        // Handle type conversions (Gson deserializes numbers as Double)
                        try {
                            if (field.getType() == long.class && value instanceof Number) {
                                field.setLong(null, ((Number) value).longValue());
                            } else if (field.getType() == int.class && value instanceof Number) {
                                field.setInt(null, ((Number) value).intValue());
                            } else if (field.getType() == boolean.class && value instanceof Boolean) {
                                field.setBoolean(null, (Boolean) value);
                            } else {
                                field.set(null, value);
                            }
                        } catch (IllegalAccessException e) {
                            System.err.println("Failed to set field " + field.getName() + ": " + e.getMessage());
                        }
                    } else {
                        // Property not in JSON - keep the default value already set in the class field
                        System.out.println("Config property '" + field.getName() + "' not found in JSON, using default value");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load GlowPlayer config: " + e.getMessage());
        }
    }
}

