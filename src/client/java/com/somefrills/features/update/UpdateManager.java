package com.somefrills.features.update;

import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.PotentialUpdate;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateSource;
import moe.nea.libautoupdate.UpdateTarget;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.somefrills.Main.LOGGER;

public class UpdateManager {

    private static volatile CompletableFuture<?> activePromise = null;
    private static volatile PotentialUpdate potentialUpdate = null;

    public enum UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }

    private static volatile UpdateState updateState = UpdateState.NONE;

    private static final UpdateContext context = new UpdateContext(
            UpdateSource.githubUpdateSource("AverageUser125", "SomeFrills"),
            UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager.class),
            new CurrentVersion() {
                @Override
                public String display() {
                    return FabricLoader.getInstance()
                            .getModContainer(com.somefrills.Main.MOD_ID)
                            .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                            .orElse("unknown");
                }

                @Override
                public boolean isOlderThan(com.google.gson.JsonElement element) {
                    if (element == null) return true;
                    try {
                        String currentVersionString = FabricLoader.getInstance()
                                .getModContainer(com.somefrills.Main.MOD_ID)
                                .orElseThrow()
                                .getMetadata()
                                .getVersion()
                                .getFriendlyString();

                        String newestVersionString = element.getAsString();

                        // Normalize both versions by removing "v" prefix if present
                        currentVersionString = normalizeVersion(currentVersionString);
                        newestVersionString = normalizeVersion(newestVersionString);

                        return parseSemanticVersion(currentVersionString) < parseSemanticVersion(newestVersionString);
                    } catch (Exception e) {
                        LOGGER.error("Failed to compare versions", e);
                        return false;
                    }
                }
            },
            "SomeFrills"
    );

    static {
        context.cleanup();
    }

    public static UpdateState getUpdateState() {
        return updateState;
    }

    public static String getCurrentVersion() {
        return FabricLoader.getInstance()
                .getModContainer(com.somefrills.Main.MOD_ID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    public static String getLatestVersion() {
        if (potentialUpdate != null && potentialUpdate.getUpdate() != null) {
            String version = potentialUpdate.getUpdate().getVersionNumber().getAsString();
            return normalizeVersion(version);
        }
        return null;
    }

    private static String normalizeVersion(String version) {
        if (version != null && version.startsWith("v")) {
            return version.substring(1);
        }
        return version;
    }

    public static boolean isUpdateAvailable() {
        return updateState == UpdateState.AVAILABLE || (potentialUpdate != null && potentialUpdate.isUpdateAvailable());
    }

    public static void checkUpdate() {
        if (updateState != UpdateState.NONE) {
            LOGGER.debug("Update check already in progress");
            return;
        }

        LOGGER.info("Starting update check");
        updateState = UpdateState.NONE; // Reset to check again

        cancelActivePromise();
        activePromise = context.checkUpdate("somefrills.jar").thenAcceptAsync(potentialUpd -> {
            LOGGER.debug("Update check completed");
            potentialUpdate = potentialUpd;

            if (potentialUpd != null && potentialUpd.isUpdateAvailable()) {
                updateState = UpdateState.AVAILABLE;
                LOGGER.info("Update available: {}", potentialUpd.getUpdate().getVersionNumber().getAsString());
            } else {
                LOGGER.debug("No update available");
            }
        });
    }

    public static void queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            LOGGER.warn("Trying to queue update when no update is available");
            return;
        }

        updateState = UpdateState.QUEUED;
        cancelActivePromise();

        activePromise = CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Update download started");
            try {
                potentialUpdate.prepareUpdate();
            } catch (IOException e) {
                LOGGER.error("Failed to prepare update", e);
            }
            return null;
        }).thenAcceptAsync(v -> {
            LOGGER.info("Update download completed");
            updateState = UpdateState.DOWNLOADED;
            potentialUpdate.executePreparedUpdate();
        });
    }

    public static void reset() {
        updateState = UpdateState.NONE;
        potentialUpdate = null;
        cancelActivePromise();
        LOGGER.debug("Update state reset");
    }

    private static void cancelActivePromise() {
        if (activePromise != null) {
            activePromise.cancel(true);
            activePromise = null;
        }
    }

    private static int parseSemanticVersion(String version) {
        String[] numbers = version.split("\\.");
        if (numbers.length >= 3) {
            int major = parseInt(numbers[0]).orElse(0);
            int minor = parseInt(numbers[1]).orElse(0);
            int patch = parseInt(numbers[2]).orElse(0);
            return major * 1000 + minor * 100 + patch;
        }
        return 0;
    }

    private static Optional<Integer> parseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
