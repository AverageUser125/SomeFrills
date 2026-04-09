package com.somefrills.config.about;

import com.somefrills.features.update.UpdateManager;
import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor;
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent;
import io.github.notenoughupdates.moulconfig.gui.MouseEvent;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;

import static com.somefrills.Main.LOGGER;

public class GuiOptionEditorUpdateCheck extends GuiOptionEditor {
    private final GuiElementButton button = new GuiElementButton();
    private final String currentVersion;

    public GuiOptionEditorUpdateCheck(ProcessedOption option) {
        super(option);
        this.currentVersion = UpdateManager.getCurrentVersion();
    }

    @Override
    public void render(RenderContext context, int x, int y, int width) {
        try {
            var fr = context.getMinecraft().getDefaultFontRenderer();

            context.pushMatrix();
            context.translate(x + 10, y);
            int adjustedWidth = width - 20;
            String nextVersion = UpdateManager.getLatestVersion();

            // Set button text based on state
            button.setText(getButtonText(UpdateManager.getUpdateState(), nextVersion));
            button.width = button.getWidth(context);
            button.render(context, getButtonPosition(adjustedWidth), 10);

            int widthRemaining = adjustedWidth - button.width - 10;

            // Render downloaded message if applicable
            if (UpdateManager.getUpdateState() == UpdateManager.UpdateState.DOWNLOADED) {
                context.drawStringCenteredScaledMaxWidth(
                        StructuredText.of("§aThe update will be installed after your next restart."),
                        fr,
                        widthRemaining / 2f,
                        40f,
                        true,
                        widthRemaining,
                        -1
                );
            }

            // Render version text with scaling
            context.scale(2f, 2f);
            boolean sameVersion = currentVersion.equalsIgnoreCase(nextVersion);
            String colorCode = UpdateManager.getUpdateState() == UpdateManager.UpdateState.NONE ? "§a" : "§c";
            String versionText = colorCode + currentVersion;

            if (nextVersion != null && !sameVersion) {
                versionText += " ➜ §a" + nextVersion;
            }

            context.drawStringCenteredScaledMaxWidth(
                    StructuredText.of(versionText),
                    fr,
                    widthRemaining / 4f,
                    10f,
                    true,
                    widthRemaining / 2,
                    -1
            );

            context.popMatrix();
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

    private int getButtonPosition(int width) {
        return width - button.width;
    }

    @Override
    public int getHeight() {
        return 55;
    }

    @Override
    public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY, MouseEvent event) {
        try {
            // Only respond to left-click events
            if (!(event instanceof MouseEvent.Click click)) {
                return false;
            }

            // Only handle left mouse button (0)
            if (click.getMouseButton() != 0) {
                return false;
            }

            int buttonPos = getButtonPosition(width - 20);

            // Check button click - using SkyHanni logic
            if (isInside(buttonPos, 10, button, x, y, mouseX, mouseY)) {
                UpdateManager.UpdateState state = UpdateManager.getUpdateState();
                switch (state) {
                    case AVAILABLE -> UpdateManager.queueUpdate();
                    case QUEUED, DOWNLOADED -> {
                    }
                    case NONE -> UpdateManager.checkUpdate();
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to process mouse input", e);
        }

        return false;
    }

    private boolean isInside(int buttonX, int buttonY, GuiElementButton btn, int containerX, int containerY, int mouseX, int mouseY) {
        int inX = mouseX - buttonX - containerX;
        int inY = mouseY - buttonY - containerY;
        return inX >= 0 && inX <= btn.width && inY >= 0 && inY <= GuiElementButton.HEIGHT;
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
