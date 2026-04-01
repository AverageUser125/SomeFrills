package com.somefrills.config;

import com.somefrills.Main;
import com.somefrills.features.misc.AutoUpdate;
import com.terraformersmc.modmenu.api.*;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jspecify.annotations.NonNull;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::getConfigScreen;
    }

    private static Screen getConfigScreen(Screen previous) {
        var editor = Main.config.getEditor();
        GuiContext guiContext = new GuiContext(new GuiElementComponent(editor));
        return new MoulConfigScreenComponent(Text.empty(), guiContext, previous);
    }

    @Override
    public UpdateChecker getUpdateChecker() {
        return () -> {
            var result = AutoUpdate.fetchUpdateAsync().join();
            return new ModApiSomeFrillsUpdateInfo(
                    result.updateAvailable(),
                    result.currentVersion(),
                    result.latestVersion(),
                    "https://github.com/AverageUser125/SomeFrills/releases/" + result.latestVersion(),
                    UpdateChannel.RELEASE
            );
        };
    }

    private record ModApiSomeFrillsUpdateInfo(
            boolean isUpdateAvailable,
            String currentVersion,
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
            return Text.of("SomeFrills " + latestVersion + " is available! You are on " + currentVersion + ".");
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