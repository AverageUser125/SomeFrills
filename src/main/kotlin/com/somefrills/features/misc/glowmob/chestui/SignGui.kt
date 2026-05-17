package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.misc.Utils
import net.minecraft.block.SignBlock
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.block.entity.SignText
import net.minecraft.client.gui.screen.ingame.SignEditScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer
import net.minecraft.client.util.SelectionManager
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.function.Consumer

class SignGui private constructor(
    sign: SignBlockEntity,
    private val front: Boolean,
    filtered: Boolean,
    private val onClose: Consumer<Array<String>>
) : SignEditScreen(sign, front, filtered) {

    override fun init() {
        addDrawableChild(
            ButtonWidget.builder(ScreenTexts.DONE) {
                close()
            }.dimensions(
                width / 2 - 100,
                height / 4 + 144,
                200,
                20
            ).build()
        )

        selectionManager = SelectionManager(
            { messages[currentRow] },
            { message -> setCurrentRowMessage(message) },
            SelectionManager.makeClipboardGetter(client),
            SelectionManager.makeClipboardSetter(client),
            { textLine ->
                client.textRenderer.getWidth(textLine) <= blockEntity.maxTextWidth
            }
        )

        val standing =
            blockEntity.cachedState.block is SignBlock

        model = SignBlockEntityRenderer.createSignModel(
            client.loadedEntityModels,
            signType,
            standing
        )

        currentRow = 1
        selectionManager?.putCursorAtEnd()
    }

    override fun close() {
        val finalText = blockEntity.getText(front)

        val out = Array(4) { i ->
            finalText.getMessage(i, false).string
        }

        onClose.accept(out)
    }

    class FakeSign(lines: Array<Text>) : SignBlockEntity(
        BlockPos.ORIGIN,
        Registries.BLOCK.get(
            Identifier.of("minecraft", "oak_sign")
        ).defaultState
    ) {

        init {
            val padded = Array(4) { i ->
                lines.getOrElse(i) { Text.empty() }
            }

            val text = SignText(
                padded,
                padded,
                DyeColor.WHITE,
                false
            )

            setText(text, true)
        }

        override fun setText(text: SignText, front: Boolean): Boolean {
            return if (front) {
                setFrontText(text)
            } else {
                setBackText(text)
            }
        }

        override fun isPlayerTooFarToEdit(uuid: UUID): Boolean {
            return false
        }

        private fun setBackText(backText: SignText): Boolean {
            if (backText != this.backText) {
                this.backText = backText
                return true
            }

            return false
        }

        private fun setFrontText(frontText: SignText): Boolean {
            if (frontText != this.frontText) {
                this.frontText = frontText
                return true
            }

            return false
        }
    }

    companion object {

        @JvmStatic
        fun open(
            rows: Array<String>,
            onClose: Consumer<Array<String>>
        ) {
            val textRows = rows.map(Text::of).toTypedArray()

            val sign = FakeSign(textRows)
            val gui = SignGui(sign, true, false, onClose)

            Utils.setScreen(gui)
        }

        @JvmStatic
        fun open(
            rows: Array<Text>,
            onClose: Consumer<Array<String>>
        ) {
            val sign = FakeSign(rows)
            val gui = SignGui(sign, true, false, onClose)

            Utils.setScreen(gui)
        }
    }
}