package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.text.ClickEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.net.URI

object ChatUtils {
    fun getTag(): MutableText {
        return Text.literal("").append(
            Text.literal("[SomeFrills] ")
                .setStyle(Style.EMPTY.withColor(0x00ff00))
        ) as MutableText
    }

    fun infoRawInternal(message: MutableText) {
        val mutableMessage = if (message.style == null || message.style.color == null) {
            message.withColor(0xffffff)
        } else {
            message
        }
        mc.inGameHud.chatHud.addMessage(getTag().append(mutableMessage))
    }

    fun info(message: String) {
        ChatUtils.infoRawInternal(Text.literal(message) as MutableText)
    }

    fun infoButton(message: String, command: String) {
        val click = ClickEvent.RunCommand(command)
        ChatUtils.infoRawInternal(
            (Text.literal(message) as MutableText)
                .setStyle(Style.EMPTY.withClickEvent(click))
        )
    }

    fun infoLink(message: String, url: String) {
        val click = ClickEvent.OpenUrl(URI.create(url))
        ChatUtils.infoRawInternal(
            (Text.literal(message) as MutableText)
                .setStyle(Style.EMPTY.withClickEvent(click))
        )
    }

    fun infoRaw(message: MutableText) {
        ChatUtils.infoRawInternal(message)
    }

    fun infoFormat(message: String, vararg values: Any?) {
        ChatUtils.infoRawInternal(Text.literal(TextUtils.format(message, *values)) as MutableText)
    }
}