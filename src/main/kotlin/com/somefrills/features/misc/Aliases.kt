package com.somefrills.features.misc

import com.google.gson.JsonObject
import com.somefrills.config.FrillsMod

import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature

@FrillsFeature
class Aliases : Feature(FrillsMod.config.misc.commandAliases.enabled) {
    private val config get() = FrillsMod.config.misc.commandAliases

    fun convertCommand(message: String): String {
        if (!config.enabled.get()) return message
        if (message.isEmpty()) return message

        val obj: JsonObject = aliases
        if (!obj.has(message)) {
            return message
        }

        try {
            val element = obj.get(message)
            if (element != null && element.isJsonPrimitive) {
                return element.asString
            }
        } catch (e: Exception) {
            return message
        }

        return message
    }

    companion object {
        private val aliases: JsonObject = JsonObject()

        init {
            aliases.addProperty("gd", "warp garden")
            aliases.addProperty("da", "warp da")
            aliases.addProperty("cc", "chat coop")
            aliases.addProperty("gc", "chat guild")
            aliases.addProperty("isle", "warp isle")
            aliases.addProperty("end", "warp end")
            // additional aliases requested
            aliases.addProperty("crypt", "warp crypt")
            aliases.addProperty("deep", "warp deep")
            aliases.addProperty("gold", "warp gold")
            aliases.addProperty("bayou", "warp bayou")
            aliases.addProperty("park", "warp park")
            aliases.addProperty("void", "warp void")
            aliases.addProperty("mu", "warp museum")
            aliases.addProperty("rift", "warp rift")
            // "base" was listed twice in the provided list; add it once
            aliases.addProperty("base", "warp base")
            aliases.addProperty("cn", "warp cn")
            aliases.addProperty("mines", "warp mines")
            aliases.addProperty("forge", "warp forge")
            aliases.addProperty("trapper", "warp trapper")
            aliases.addProperty("desert", "warp desert")
            // short chat/commands
            aliases.addProperty("sc", "sax")
            aliases.addProperty("v2", "visit visit2")
            // handle both with and without accidental trailing space
            aliases.addProperty("sbs ", "skyblocker config")
            aliases.addProperty("sbs", "skyblocker config")
        }
    }
}
