package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.utils.GuiUtils
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.SignEditScreen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.SignBlockEntity
import net.minecraft.world.level.block.entity.SignText
import java.util.function.Consumer

class SignGui(
    sign: SignBlockEntity,
    private val front: Boolean,
    filtered: Boolean,
    private val onClose: Consumer<Array<String>>
) : SignEditScreen(sign, front, filtered) {

    override fun init() {
        super.init()

        addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                onClose()
            }.bounds(
                width / 2 - 100,
                height / 4 + 144,
                200,
                20
            ).build()
        )
    }

    override fun onClose() {
        val text = sign.getText(front)
        val output = Array<String>(4) { i ->
            text.getMessage(i, false).string
        }

        onClose.accept(output)
        super.onClose()
    }


    class FakeSign(lines: Array<Component>) :
        SignBlockEntity(
            BlockPos.ZERO,
            Blocks.OAK_SIGN.defaultBlockState()
        ) {

        init {
            val padded = Array(4) { i ->
                lines.getOrElse(i) { Component.empty() }
            }

            val signText = SignText(
                padded,
                padded,
                DyeColor.WHITE,
                false
            )

            setText(signText, true)
        }

        override fun setText(
            text: SignText,
            front: Boolean
        ): Boolean {
            if (front) {
                frontText = text
            } else {
                backText = text
            }
            return true
        }
    }


    companion object {

        @JvmStatic
        fun open(
            rows: Array<String>,
            onClose: Consumer<Array<String>>
        ) {
            open(
                rows.map(Component::literal).toTypedArray(),
                onClose
            )
        }


        @JvmStatic
        fun open(
            rows: Array<Component>,
            onClose: Consumer<Array<String>>
        ) {
            val sign = FakeSign(rows)

            GuiUtils.setScreen(
                SignGui(
                    sign,
                    front = true,
                    filtered = false,
                    onClose = onClose
                )
            )
        }
    }
}