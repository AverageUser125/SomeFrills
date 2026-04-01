package com.somefrills.config;

import com.somefrills.Main;
import com.somefrills.features.misc.AutoUpdate;
import com.terraformersmc.modmenu.api.*;
import net.minecraft.text.Text;
import org.jspecify.annotations.NonNull;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Main::getConfigScreen;
    }

    @Override
    public UpdateChecker getUpdateChecker() {
        return () -> {
            var result = AutoUpdate.fetchUpdateAsync().join(); // ⛔ blocks here
            return new ModApiSomeFrillsUpdateInfo(
                    result.updateAvailable(),
                    result.latestVersion(),
                    "https://github.com/AverageUser125/SomeFrills/releases/" + result.latestVersion(),
                    UpdateChannel.RELEASE
            );
        };
    }

    private record ModApiSomeFrillsUpdateInfo(
            boolean isUpdateAvailable,
            String latestVersion,
            String downloadLink,
            UpdateChannel updateChannel
    ) implements UpdateInfo {

        @Override
        public boolean isUpdateAvailable() {
            return isUpdateAvailable;
        }

        @Override
        public @NonNull Text getUpdateMessage() {
            return Text.of("A new version of Some Frills is available: " + latestVersion);
        }

        @Override
        public String getDownloadLink() {
            return downloadLink;
        }

        @Override
        public UpdateChannel getUpdateChannel() {
            return updateChannel;
        }
    }
}