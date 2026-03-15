package com.glowplayer.features;

import com.glowplayer.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.loader.impl.FabricLoaderImpl.MOD_ID;

public class UpdateChecker {
    private static boolean hasChecked = false;

    public static void checkUpdate() {
        if (hasChecked) return;
        hasChecked = true;
        checkForUpdate();
    }

    private static void checkForUpdate() {
        Thread.startVirtualThread(() -> {
            try {
                String version = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
                InputStream connection = URI.create("https://raw.githubusercontent.com/AverageUser125/GlowPlayer/refs/heads/main/gradle.properties").toURL().openStream();
                for (String line : IOUtils.toString(connection, StandardCharsets.UTF_8).split("\n")) {
                    if (line.startsWith("mod_version=")) {
                        String newest = line.replace("mod_version=", "");
                        if (Utils.getVersionNumber(newest) > Utils.getVersionNumber(version)) {
                            Utils.infoLink(Utils.format("§a§lNew version available! §aClick here to open the Modrinth releases page. §7Current: {}, Newest: {}", version, newest), "https://modrinth.com/mod/nofrills/versions");
                            return;
                        }
                    }
                }
            } catch (IOException exception) {
                Utils.info("§cAn error occurred while checking for an update. Additional information can be found in the log.");
            }
        });
    }
}
