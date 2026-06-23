package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.multiplayer.chat.GuiMessageSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import java.net.URI

object ChatUtils {
    fun getTag(): MutableComponent {
        return Component.literal("").append(
            Component.literal("[SomeFrills] ")
                .setStyle(Style.EMPTY.withColor(0x00ff00))
        ) as MutableComponent
    }

    fun infoRawInternal(message: MutableComponent) {
        val mutableMessage = if (message.style == null || message.style.color == null) {
            message.withColor(0xffffff)
        } else {
            message
        }
        mc.gui.chat.addMessage(getTag().append(mutableMessage), null, GuiMessageSource.SYSTEM_CLIENT, null)
    }

    fun info(message: String) {
        ChatUtils.infoRawInternal(Component.literal(message) as MutableComponent)
    }

    fun infoButton(message: String, command: String) {
        val click = ClickEvent.RunCommand(command)
        ChatUtils.infoRawInternal(
            (Component.literal(message) as MutableComponent)
                .setStyle(Style.EMPTY.withClickEvent(click))
        )
    }

    fun infoLink(message: String, url: String) {
        val click = ClickEvent.OpenUrl(URI.create(url))
        ChatUtils.infoRawInternal(
            (Component.literal(message) as MutableComponent)
                .setStyle(Style.EMPTY.withClickEvent(click))
        )
    }

    fun infoRaw(message: MutableComponent) {
        ChatUtils.infoRawInternal(message)
    }

    fun infoFormat(message: String, vararg values: Any?) {
        ChatUtils.infoRawInternal(Component.literal(TextUtils.format(message, *values)) as MutableComponent)
    }
}