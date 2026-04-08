package com.somefrills.config.about;

import com.somefrills.features.update.UpdateManager;
import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor;
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import net.minecraft.client.gui.DrawContext;

import static com.somefrills.Main.LOGGER;
import static com.somefrills.Main.mc;

public class GuiOptionEditorUpdateCheck extends GuiOptionEditor {
    private static final int BUTTON_HEIGHT = 20;
    private String currentVersion = "Unknown";

    public GuiOptionEditorUpdateCheck(ProcessedOption option) {
        super(option);
        updateCurrentVersion();
    }

    private void updateCurrentVersion() {
        currentVersion = UpdateManager.getCurrentVersion();
    }

    @Override
    public void render(RenderContext context, int x, int y, int width) {
        try {
            int adjustedWidth = width - 20;
            int xPos = x + 10;

            // Get update state info
            UpdateManager.UpdateState state = UpdateManager.getUpdateState();
            String nextVersion = UpdateManager.getLatestVersion();

            // Render button text based on state
            String buttonText = getButtonText(state, nextVersion);
            renderButton(context, xPos, y, adjustedWidth, buttonText);

            // Render version info
            renderVersionInfo(context, xPos, y + 25, adjustedWidth, nextVersion);

            // Render message if downloaded
            if (state == UpdateManager.UpdateState.DOWNLOADED) {
                renderDownloadedMessage(context, xPos, y + 40, adjustedWidth);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to render update check", e);
        }
    }

    private String getButtonText(UpdateManager.UpdateState state, String nextVersion) {
        return switch (state) {
            case AVAILABLE -> "Download update";
            case QUEUED -> "Downloading...";
            case DOWNLOADED -> "Downloaded";
            case NONE -> nextVersion == null ? "Check for Updates" : "Up to date";
        };
    }

    private void renderButton(RenderContext context, int x, int y, int width, String text) {
        try {
            DrawContext drawCtx = (DrawContext) context;
            int buttonWidth = Math.min(150, width - 10);
            int buttonX = x + width - buttonWidth;

            // Draw button background
            drawCtx.fill(buttonX, y, buttonX + buttonWidth, y + BUTTON_HEIGHT, 0xFF8B8B8B);
            // Draw button border
            drawCtx.fill(buttonX, y, buttonX + buttonWidth, y + 1, 0xFFFFFFFF);
            drawCtx.fill(buttonX, y, buttonX + 1, y + BUTTON_HEIGHT, 0xFFFFFFFF);

            // Draw text
            drawCtx.drawCenteredTextWithShadow(mc.textRenderer, text, buttonX + buttonWidth / 2, y + 6, 0xFFFFFFFF);
        } catch (Exception e) {
            LOGGER.debug("Failed to render update button", e);
        }
    }

    private void renderVersionInfo(RenderContext context, int x, int y, int width, String nextVersion) {
        try {
            DrawContext drawCtx = (DrawContext) context;
            String versionText;
            int color;

            // Always show current version
            versionText = "v" + currentVersion;

            if (UpdateManager.getUpdateState() == UpdateManager.UpdateState.NONE) {
                color = 0xFF00AA00; // Green - up to date
            } else {
                color = 0xFFAA0000; // Red - update available or in progress
            }

            // Add arrow and next version if available
            if (nextVersion != null && !currentVersion.equalsIgnoreCase(nextVersion)) {
                versionText += " ➜ v" + nextVersion;
            }

            drawCtx.drawCenteredTextWithShadow(mc.textRenderer, versionText, x + width / 2, y, color);
        } catch (Exception e) {
            LOGGER.debug("Failed to render version info", e);
        }
    }

    private void renderDownloadedMessage(RenderContext context, int x, int y, int width) {
        try {
            DrawContext drawCtx = (DrawContext) context;
            drawCtx.drawCenteredTextWithShadow(mc.textRenderer, "The update will be installed after your next restart.", x + width / 2, y, 0xFF00AA00);
        } catch (Exception e) {
            LOGGER.debug("Failed to render downloaded message", e);
        }
    }

    @Override
    public int getHeight() {
        return 65;
    }

    @Override
    public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY, MouseEvent event) {
        int adjustedWidth = width - 20;
        int xPos = x + 10;
        int buttonWidth = Math.min(150, adjustedWidth - 10);
        int buttonX = xPos + adjustedWidth - buttonWidth;

        // Check if mouse is over button
        if (isMouseOver(mouseX, mouseY, buttonX, y, buttonX + buttonWidth, y + BUTTON_HEIGHT)) {
            handleButtonClick();
            return true;
        }

        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }

    private void handleButtonClick() {
        UpdateManager.UpdateState state = UpdateManager.getUpdateState();
        switch (state) {
            case AVAILABLE:
                UpdateManager.queueUpdate();
                break;
            case QUEUED:
            case DOWNLOADED:
                // No action needed
                break;
            case NONE:
                UpdateManager.checkUpdate();
                break;
        }
    }

    @Override
    public boolean keyboardInput(KeyboardEvent event) {
        return false;
    }

    @Override
    public boolean fulfillsSearch(String word) {
        return super.fulfillsSearch(word) || word.contains("download") || word.contains("update");
    }
}
