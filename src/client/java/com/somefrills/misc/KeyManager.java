package com.somefrills.misc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple generic key manager - stores keys in memory and persists to JSON
 * Can be replaced with a more secure backend implementation later
 */
public class KeyManager {
    private static final Map<String, String> keys = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final String KEYSTORE_FILENAME = "somefrills/keystore.json";
    private static Path keystorePath;

    /**
     * Initialize KeyManager with a config directory
     *
     * @param configDir Path to the config directory (e.g., ~/.minecraft/config/somefrills)
     */
    public static void initialize(Path configDir) {
        keystorePath = configDir.resolve(KEYSTORE_FILENAME);
        load();
    }

    /**
     * Store a key
     *
     * @param keyName Name of the key (e.g., "hypixel", "discord", "slack")
     * @param value   The key value
     */
    public static void setKey(String keyName, String value) {
        keys.put(keyName, value);
        save();
    }

    /**
     * Get a stored key
     *
     * @param keyName Name of the key
     * @return The key value, or null if not set
     */
    public static String getKey(String keyName) {
        return keys.get(keyName);
    }

    /**
     * Check if a key is set
     *
     * @param keyName Name of the key
     * @return true if the key exists and is not empty
     */
    public static boolean hasKey(String keyName) {
        String value = keys.get(keyName);
        return value != null && !value.isEmpty();
    }

    /**
     * Remove a stored key
     *
     * @param keyName Name of the key
     */
    public static void removeKey(String keyName) {
        keys.remove(keyName);
        save();
    }

    /**
     * Clear all stored keys
     */
    public static void clearAllKeys() {
        keys.clear();
        save();
    }

    /**
     * Save all keys to keystore.json
     */
    public static void save() {
        if (keystorePath == null) {
            System.err.println("[KeyManager] Keystore path not initialized. Call initialize() first.");
            return;
        }

        try {
            // Create directory if it doesn't exist
            Files.createDirectories(keystorePath.getParent());

            // Write to JSON file
            try (FileWriter writer = new FileWriter(keystorePath.toFile())) {
                GSON.toJson(keys, writer);
            }

            System.out.println("[KeyManager] Keys saved to " + keystorePath);
        } catch (Exception e) {
            System.err.println("[KeyManager] Error saving keys: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all keys from keystore.json
     */
    public static void load() {
        if (keystorePath == null) {
            System.err.println("[KeyManager] Keystore path not initialized. Call initialize() first.");
            return;
        }

        try {
            File file = keystorePath.toFile();
            if (!file.exists()) {
                System.out.println("[KeyManager] Keystore file not found, creating new one.");
                return;
            }

            // Read from JSON file
            try (FileReader reader = new FileReader(file)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                keys.clear();

                if (json != null) {
                    for (String key : json.keySet()) {
                        keys.put(key, json.get(key).getAsString());
                    }
                }
            }

            System.out.println("[KeyManager] Loaded " + keys.size() + " keys from " + keystorePath);
        } catch (Exception e) {
            System.err.println("[KeyManager] Error loading keys: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




