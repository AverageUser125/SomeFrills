package com.somefrills.config.about

import com.somefrills.Main.LOGGER
import com.somefrills.features.update.UpdateManager
import io.github.notenoughupdates.moulconfig.common.RenderContext
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption

class GuiOptionEditorUpdateCheck(option: ProcessedOption) : GuiOptionEditor(option) {
    private val button = GuiElementButton()
    private val currentVersion get() = UpdateManager.currentVersion

    override fun render(context: RenderContext, x: Int, y: Int, width: Int) {
        try {
            val fr = context.minecraft.defaultFontRenderer

            context.pushMatrix()
            context.translate((x + 10).toFloat(), y.toFloat())
            val adjustedWidth = width - 20
            val nextVersion = UpdateManager.latestVersion

            // Set button text based on state
            button.text = getButtonText(UpdateManager.updateState, nextVersion)
            button.width = button.getWidth(context)
            button.render(context, getButtonPosition(adjustedWidth), 10)

            val widthRemaining = adjustedWidth - button.width - 10

            // Render downloaded message if applicable
            if (UpdateManager.updateState == UpdateManager.UpdateState.DOWNLOADED) {
                context.drawStringCenteredScaledMaxWidth(
                    StructuredText.of("§aThe update will be installed after your next restart."),
                    fr,
                    widthRemaining / 2f,
                    40f,
                    true,
                    widthRemaining,
                    -1
                )
            }

            // Render version text with scaling
            context.scale(2f, 2f)
            val sameVersion = currentVersion.equals(nextVersion, ignoreCase = true)
            val colorCode = if (UpdateManager.updateState == UpdateManager.UpdateState.NONE) "§a" else "§c"
            var versionText = colorCode + currentVersion

            if (!sameVersion) {
                versionText += " ➜ §a$nextVersion"
            }

            context.drawStringCenteredScaledMaxWidth(
                StructuredText.of(versionText),
                fr,
                widthRemaining / 4f,
                10f,
                true,
                widthRemaining / 2,
                -1
            )

            context.popMatrix()
        } catch (e: Exception) {
            LOGGER.error("Failed to render update check", e)
        }
    }

    private fun getButtonText(state: UpdateManager.UpdateState, nextVersion: String?): String {
        return when (state) {
            UpdateManager.UpdateState.AVAILABLE -> "Download update"
            UpdateManager.UpdateState.QUEUED -> "Downloading..."
            UpdateManager.UpdateState.DOWNLOADED -> "Downloaded"
            UpdateManager.UpdateState.NONE -> if (nextVersion == null) "Check for Updates" else "Up to date"
        }
    }

    private fun getButtonPosition(width: Int): Int {
        return width - button.width
    }

    override fun getHeight(): Int {
        return 55
    }

    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int, event: MouseEvent): Boolean {
        try {
            // Only respond to left-click events
            if (event !is MouseEvent.Click) {
                return false
            }

            // Only handle left mouse button (0)
            if (event.mouseButton != 0) {
                return false
            }

            val buttonPos = getButtonPosition(width - 20)

            // Check button click - using SkyHanni logic
            if (isInside(buttonPos, 10, button, x, y, mouseX, mouseY)) {
                when (UpdateManager.updateState) {
                    UpdateManager.UpdateState.AVAILABLE -> UpdateManager.queueUpdate()
                    UpdateManager.UpdateState.QUEUED, UpdateManager.UpdateState.DOWNLOADED -> {
                    }
                    UpdateManager.UpdateState.NONE -> UpdateManager.checkUpdate()
                }
                return true
            }
        } catch (e: Exception) {
            LOGGER.debug("Failed to process mouse input", e)
        }

        return false
    }

    private fun isInside(
        buttonX: Int,
        buttonY: Int,
        btn: GuiElementButton,
        containerX: Int,
        containerY: Int,
        mouseX: Int,
        mouseY: Int
    ): Boolean {
        val inX = mouseX - buttonX - containerX
        val inY = mouseY - buttonY - containerY
        return inX >= 0 && inX <= btn.width && inY >= 0 && inY <= GuiElementButton.HEIGHT
    }

    override fun keyboardInput(event: KeyboardEvent): Boolean {
        return false
    }

    override fun fulfillsSearch(word: String): Boolean {
        return super.fulfillsSearch(word) || word.contains("download") || word.contains("update")
    }
}
