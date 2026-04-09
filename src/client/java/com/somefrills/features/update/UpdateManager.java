package com.somefrills.features.update;

import com.google.gson.JsonElement;
import com.somefrills.misc.Utils;
import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.PotentialUpdate;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateTarget;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.Main.mc;

public class UpdateManager {
    private static volatile CompletableFuture<?> activePromise = null;
    private static volatile UpdateState updateState = UpdateState.NONE;
    private static volatile PotentialUpdate potentialUpdate = null;
    private static volatile boolean hasCheckedThisSession = false;

    public enum UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
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
        if (potentialUpdate != null) {
            return potentialUpdate.getUpdate().getVersionNumber().getAsString();
        }
        return getCurrentVersion();
    }

    public static boolean isUpdateAvailable() {
        return updateState == UpdateState.AVAILABLE;
    }

    private static final UpdateContext context = new UpdateContext(
            new CustomGithubReleaseUpdateSource("AverageUser125", "SomeFrills"),
            UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager.class),
            new CurrentVersion() {
                @Override
                public String display() {
                    return getCurrentVersion();
                }

                @Override
                public boolean isOlderThan(JsonElement element) {
                    if (element == null || !element.isJsonPrimitive()) return false;
                    try {
                        String asString = element.getAsString();
                        int currentParsed = parseSemanticVersion(getCurrentVersion());
                        int latestParsed = parseSemanticVersion(asString);
                        return currentParsed < latestParsed;
                    } catch (Exception e) {
                        LOGGER.warn("Failed to compare versions", e);
                        return false;
                    }
                }
            },
            com.somefrills.Main.MOD_ID
    );

    public static void reset() {
        updateState = UpdateState.NONE;
        potentialUpdate = null;
        hasCheckedThisSession = false;
        cancelActivePromise();
        LOGGER.debug("Update state reset");
    }

    public static void checkUpdate() {
        checkUpdate(false);
    }

    public static void checkUpdate(boolean autoQueue) {
        if (hasCheckedThisSession) {
            LOGGER.info("Already checked for updates this session");
            return;
        }

        if (updateState != UpdateState.NONE && updateState != UpdateState.AVAILABLE) {
            LOGGER.info("Trying to perform update check while another update is already in progress");
            return;
        }

        if (updateState == UpdateState.AVAILABLE) {
            updateState = UpdateState.NONE;
            LOGGER.info("Resetting update state to force download");
        }

        hasCheckedThisSession = true;
        LOGGER.info("Starting update check (autoQueue: {})", autoQueue);
        activePromise = context.checkUpdate("full").thenAcceptAsync(update -> {
            LOGGER.info("Update check completed");
            if (updateState != UpdateState.NONE) {
                LOGGER.info("This appears to be the second update check. Ignoring this one");
                return;
            }

            potentialUpdate = update;
            if (update.isUpdateAvailable()) {
                updateState = UpdateState.AVAILABLE;
                String versionName = update.getUpdate().getVersionName();
                LOGGER.info("Update available: {}", versionName);
                Utils.infoFormat("Update available: {}", versionName);

                if (autoQueue) {
                    LOGGER.info("Auto-queuing update");
                    Utils.infoFormat("Auto-queuing update");
                    queueUpdate();
                }
            } else {
                LOGGER.info("No update available");
            }
        }, mc).exceptionally(e -> {
            LOGGER.error("[SomeFrills] Failed to check for updates", e);
            hasCheckedThisSession = false;
            return null;
        });
    }

    public static void queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            LOGGER.info("Trying to enqueue an update while another one is already downloaded or none is present");
            return;
        }

        if (potentialUpdate == null) {
            LOGGER.error("Cannot queue update: potentialUpdate is null");
            return;
        }

        updateState = UpdateState.QUEUED;
        LOGGER.info("Queuing update download");
        Utils.infoFormat("Queuing update download");
        activePromise = CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Update download started");
            Utils.infoFormat("Update download started");
            try {
                potentialUpdate.prepareUpdate();
            } catch (IOException e) {
                LOGGER.error("Failed to download update", e);
                Utils.infoFormat("Failed to download update: {}", e.getMessage());
                updateState = UpdateState.AVAILABLE;
                hasCheckedThisSession = false;
            } catch (NullPointerException e) {
                LOGGER.error("Update was cleared while downloading", e);
                updateState = UpdateState.AVAILABLE;
                hasCheckedThisSession = false;
            }
            return null;
        }).thenAcceptAsync(__ -> {
            if (potentialUpdate == null) {
                LOGGER.error("Update was cleared before installation");
                return;
            }
            LOGGER.info("Update download completed");
            Utils.infoFormat("Update download completed. Will be installed on next restart.");
            updateState = UpdateState.DOWNLOADED;
            potentialUpdate.executePreparedUpdate();
        }, mc);
    }

    private static void cancelActivePromise() {
        if (activePromise != null) {
            activePromise.cancel(true);
            activePromise = null;
        }
    }

    private static int parseSemanticVersion(String version) {
        if (version == null || version.isEmpty()) return 0;

        String[] numbers = version.split("\\.");
        if (numbers.length < 3) return 0;

        int major = parseInt(numbers[0]).orElse(0);
        int minor = parseInt(numbers[1]).orElse(0);
        int patch = parseInt(numbers[2]).orElse(0);
        return major * 1000 + minor * 100 + patch;
    }

    private static Optional<Integer> parseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
