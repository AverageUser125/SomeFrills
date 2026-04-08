package com.somefrills.features.update;

import com.somefrills.config.about.GuiOptionEditorUpdateCheck;
import com.somefrills.misc.Utils;
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor;
import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.PotentialUpdate;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateTarget;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.JsonElement;
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
                if (element == null) return false;
                String asString = element.getAsString();
                int currentParsed = parseSemanticVersion(getCurrentVersion());
                int latestParsed = parseSemanticVersion(asString);
                return currentParsed < latestParsed;
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
            LOGGER.debug("Already checked for updates this session");
            return;
        }

        if (updateState != UpdateState.NONE) {
            if (updateState == UpdateState.AVAILABLE) {
                updateState = UpdateState.NONE;
                LOGGER.debug("Resetting update state to force download");
            } else {
                LOGGER.debug("Trying to perform update check while another update is already in progress");
                return;
            }
        }

        hasCheckedThisSession = true;
        LOGGER.info("Starting update check");
        activePromise = context.checkUpdate("full").thenAcceptAsync(update -> {
            LOGGER.debug("Update check completed");
            if (updateState != UpdateState.NONE) {
                LOGGER.debug("This appears to be the second update check. Ignoring this one");
                return;
            }

            potentialUpdate = update;
            if (update.isUpdateAvailable()) {
                updateState = UpdateState.AVAILABLE;
                LOGGER.info("Update available: {}", update.getUpdate().getVersionName());

                if (autoQueue) {
                    LOGGER.info("Auto-queuing update");
                    queueUpdate();
                }
            } else {
                LOGGER.debug("No update available");
            }
        }, mc).exceptionally(e -> {
            LOGGER.error("[SomeFrills] Failed to check for updates", e);
            return null;
        });
    }

    public static void queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            LOGGER.debug("Trying to enqueue an update while another one is already downloaded or none is present");
            return;
        }

        updateState = UpdateState.QUEUED;
        activePromise = CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Update download started");
            try {
                potentialUpdate.prepareUpdate();
            } catch (IOException e) {
                LOGGER.error("Failed to download update", e);
                updateState = UpdateState.AVAILABLE;
            }
            return null;
        }).thenAcceptAsync(__ -> {
            LOGGER.info("Update download completed");
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
        version = Utils.stripPrefix(version, "v");
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
