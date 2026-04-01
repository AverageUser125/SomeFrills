package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ServerJoinEvent;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.misc.Utils.*;
import static net.fabricmc.loader.impl.FabricLoaderImpl.MOD_ID;

public class AutoUpdate extends Feature {

    private static boolean hasCheckedThisSession = false;

    // Cached result (shared with ModMenu)
    private static UpdateResult cachedResult = null;

    // Track ongoing request to avoid duplicate fetches
    private static CompletableFuture<UpdateResult> fetchFuture = null;

    public AutoUpdate() {
        super(FrillsConfig.instance.misc.autoUpdate.enabled);
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        checkUpdate();
    }

    private static int getVersionNumber(String version) {
        String[] numbers = version.split("\\.");
        if (numbers.length >= 3) {
            return parseInt(numbers[0]).orElse(0) * 1000
                    + parseInt(numbers[1]).orElse(0) * 100
                    + parseInt(numbers[2]).orElse(0);
        }
        return 0;
    }

    public static String getLink(String version) {
        return "https://github.com/AverageUser125/SomeFrills/releases/" + version;
    }

    // 🔥 PUBLIC ASYNC ENTRY POINT (used everywhere)
    public static CompletableFuture<UpdateResult> fetchUpdateAsync() {
        if (cachedResult != null) {
            return CompletableFuture.completedFuture(cachedResult);
        }

        if (fetchFuture != null) {
            return fetchFuture;
        }

        fetchFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String version = FabricLoader.getInstance()
                        .getModContainer(MOD_ID)
                        .orElseThrow()
                        .getMetadata()
                        .getVersion()
                        .getFriendlyString();

                InputStream connection = URI.create(
                        "https://raw.githubusercontent.com/AverageUser125/SomeFrills/refs/heads/main/gradle.properties"
                ).toURL().openStream();

                for (String line : IOUtils.toString(connection, StandardCharsets.UTF_8).split("\n")) {
                    if (line.startsWith("mod_version=")) {
                        String newest = line.replace("mod_version=", "");

                        boolean updateAvailable =
                                getVersionNumber(newest) > getVersionNumber(version);

                        cachedResult = new UpdateResult(version, newest, updateAvailable);
                        return cachedResult;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("SomeFrills update check failed.", e);
            }

            cachedResult = new UpdateResult("unknown", "unknown", false);
            return cachedResult;
        });

        return fetchFuture;
    }

    public static void checkUpdate() {
        if (hasCheckedThisSession) return;
        hasCheckedThisSession = true;

        fetchUpdateAsync().thenAccept(result -> {
            if (result.updateAvailable()) {
                infoLink(format(
                        "§a§lNew version available! §aClick here to open the Github releases page. §7Current: {}, Newest: {}",
                        result.currentVersion(),
                        result.latestVersion()
                ), getLink(result.latestVersion()));
            }
        });
    }

    public record UpdateResult(String currentVersion, String latestVersion, boolean updateAvailable) {
    }
}