package com.somefrills.config.about;

import com.google.gson.annotations.Expose;
import com.somefrills.misc.KeyManager;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;
import kotlin.jvm.Transient;

/**
 * Configuration section for managing API keys
 */
public class ApiKeysCategory {
    @Transient
    @ConfigOption(name = "API Key Management", desc = "Manage API keys for various services")
    @Accordion
    public ApiKeySettings apiSettings = new ApiKeySettings();

    public static class ApiKeySettings {

        @Expose
        @ConfigOption(name = "Hypixel API Key", desc = "Your Hypixel API key for SkyBlock data")
        @ConfigEditorText
        public Property<String> hypixelApiKey = Property.of("");

        @Transient
        @ConfigOption(name = "Get Hypixel API Key", desc = "Open browser to get your Hypixel API key")
        @ConfigEditorButton(buttonText = "Open Browser")
        public Runnable getHypixelKey = () -> net.minecraft.util.Util.getOperatingSystem().open("https://api.hypixel.net/");

        @Transient
        @ConfigOption(name = "Save API Key", desc = "Save the API key")
        @ConfigEditorButton(buttonText = "Save")
        public Runnable saveApiKey = this::saveHypixelApiKey;

        @Transient
        @ConfigOption(name = "Clear API Key", desc = "Remove the stored API key")
        @ConfigEditorButton(buttonText = "Clear")
        public Runnable clearApiKey = this::clearHypixelApiKey;

        @Transient
        @ConfigOption(name = "Status", desc = "Whether an API key is currently stored")
        @ConfigEditorText
        public Property<String> apiKeyStatus = Property.of("No key stored");

        private void saveHypixelApiKey() {
            String key = hypixelApiKey.get();
            if (key != null && !key.isEmpty()) {
                KeyManager.setKey("hypixel", key);
                apiKeyStatus.set("✓ API key saved");
                hypixelApiKey.set(""); // Clear the text field for security
                System.out.println("[SomeFrills] Hypixel API key saved");
            } else {
                apiKeyStatus.set("✗ No key provided");
            }
        }

        private void clearHypixelApiKey() {
            KeyManager.removeKey("hypixel");
            apiKeyStatus.set("API key cleared");
            hypixelApiKey.set("");
            System.out.println("[SomeFrills] Hypixel API key cleared");
        }

        /**
         * Loads the API key status on config load
         */
        public void updateStatus() {
            if (KeyManager.hasKey("hypixel")) {
                apiKeyStatus.set("✓ Hypixel API key is stored");
            } else {
                apiKeyStatus.set("✗ No Hypixel API key stored");
            }
        }
    }
}

