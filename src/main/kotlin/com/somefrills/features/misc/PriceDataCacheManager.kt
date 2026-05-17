package com.somefrills.features.misc

import com.somefrills.Main
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Manages persistent caching of price data (both LowestBin and Bazaar).
 * Stores raw JSON response strings to separate cache files on disk.
 */
object PriceDataCacheManager {
    private val LOGGER = Main.LOGGER
    private val CACHE_DIR: Path = FabricLoader.getInstance().getConfigDir().resolve("skyblock_enhancements")
    private val LOWESTBIN_CACHE_FILE: Path = CACHE_DIR.resolve("lowestbin-prices.json")
    private val BAZAAR_CACHE_FILE: Path = CACHE_DIR.resolve("bazaar-prices.json")

    init {
        try {
            Files.createDirectories(CACHE_DIR)
        } catch (e: IOException) {
            LOGGER.warn("Failed to create cache directory", e)
        }
    }

    /**
     * Loads cached JSON response for lowestbin prices.
     *
     * @return JSON string, or null if cache doesn't exist
     */
    @JvmStatic
    fun loadLowestBin(): String? {
        return loadCache(LOWESTBIN_CACHE_FILE, "lowestbin")
    }

    /**
     * Loads cached JSON response for bazaar prices.
     *
     * @return JSON string, or null if cache doesn't exist
     */
    @JvmStatic
    fun loadBazaar(): String? {
        return loadCache(BAZAAR_CACHE_FILE, "bazaar")
    }

    /**
     * Saves raw JSON response for lowestbin prices.
     *
     * @param jsonResponse Raw JSON string from HTTP response
     */
    @JvmStatic
    fun saveLowestBin(jsonResponse: String) {
        saveCache(LOWESTBIN_CACHE_FILE, jsonResponse, "lowestbin")
    }

    /**
     * Saves raw JSON response for bazaar prices.
     *
     * @param jsonResponse Raw JSON string from HTTP response
     */
    @JvmStatic
    fun saveBazaar(jsonResponse: String) {
        saveCache(BAZAAR_CACHE_FILE, jsonResponse, "bazaar")
    }

    /**
     * Internal helper: Loads cache from a specific file.
     */
    private fun loadCache(cacheFile: Path, cacheType: String?): String? {
        try {
            if (!Files.exists(cacheFile)) {
                return null
            }
            return Files.readString(cacheFile, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.warn("Failed to load {} price cache", cacheType, e)
            return null
        }
    }

    /**
     * Internal helper: Saves cache to a specific file.
     */
    private fun saveCache(cacheFile: Path, jsonResponse: String, cacheType: String?) {
        try {
            Files.createDirectories(CACHE_DIR)
            Files.writeString(cacheFile, jsonResponse, StandardCharsets.UTF_8)
            LOGGER.debug("Cached {} price data", cacheType)
        } catch (e: Exception) {
            LOGGER.warn("Failed to save {} price cache", cacheType, e)
        }
    }
}