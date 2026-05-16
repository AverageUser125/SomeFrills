package com.somefrills.features.misc;

import com.somefrills.Main;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages persistent caching of price data (both LowestBin and Bazaar).
 * Stores raw JSON response strings to separate cache files on disk.
 */
public final class PriceDataCacheManager {
    private static final Logger LOGGER = Main.LOGGER;
    private static final Path CACHE_DIR = FabricLoader.getInstance().getConfigDir().resolve("skyblock_enhancements");
    private static final Path LOWESTBIN_CACHE_FILE = CACHE_DIR.resolve("lowestbin-prices.json");
    private static final Path BAZAAR_CACHE_FILE = CACHE_DIR.resolve("bazaar-prices.json");

    static {
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            LOGGER.warn("Failed to create cache directory", e);
        }
    }

    private PriceDataCacheManager() {
    }

    /**
     * Loads cached JSON response for lowestbin prices.
     *
     * @return JSON string, or null if cache doesn't exist
     */
    public static String loadLowestBin() {
        return loadCache(LOWESTBIN_CACHE_FILE, "lowestbin");
    }

    /**
     * Loads cached JSON response for bazaar prices.
     *
     * @return JSON string, or null if cache doesn't exist
     */
    public static String loadBazaar() {
        return loadCache(BAZAAR_CACHE_FILE, "bazaar");
    }

    /**
     * Saves raw JSON response for lowestbin prices.
     *
     * @param jsonResponse Raw JSON string from HTTP response
     */
    public static void saveLowestBin(String jsonResponse) {
        saveCache(LOWESTBIN_CACHE_FILE, jsonResponse, "lowestbin");
    }

    /**
     * Saves raw JSON response for bazaar prices.
     *
     * @param jsonResponse Raw JSON string from HTTP response
     */
    public static void saveBazaar(String jsonResponse) {
        saveCache(BAZAAR_CACHE_FILE, jsonResponse, "bazaar");
    }

    /**
     * Internal helper: Loads cache from a specific file.
     */
    private static String loadCache(Path cacheFile, String cacheType) {
        try {
            if (!Files.exists(cacheFile)) {
                return null;
            }
            return Files.readString(cacheFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("Failed to load {} price cache", cacheType, e);
            return null;
        }
    }

    /**
     * Internal helper: Saves cache to a specific file.
     */
    private static void saveCache(Path cacheFile, String jsonResponse, String cacheType) {
        try {
            Files.createDirectories(CACHE_DIR);
            Files.writeString(cacheFile, jsonResponse, StandardCharsets.UTF_8);
            LOGGER.debug("Cached {} price data", cacheType);
        } catch (Exception e) {
            LOGGER.warn("Failed to save {} price cache", cacheType, e);
        }
    }
}

