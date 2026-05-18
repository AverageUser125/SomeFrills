package com.somefrills.utils

import com.mojang.authlib.GameProfile
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Style
import net.minecraft.text.Text
import com.somefrills.Main.mc

object ItemStackUtils {
    fun getSkyblockId(customData: NbtCompound): String? {
        if (!customData.contains("id")) return null
        return customData.getString("id").orElse(null)
    }

    @JvmStatic
    fun getCustomData(stack: ItemStack): NbtCompound? {
        if (!stack.isEmpty) {
            val data = stack.get(DataComponentTypes.CUSTOM_DATA)
            if (data != null) {
                return data.nbt
            }
        }
        return null
    }

    fun getTextures(stack: ItemStack): GameProfile? {
        val profile = stack.components.get(DataComponentTypes.PROFILE)
        return if (!stack.isEmpty && profile != null) profile.gameProfile else null
    }

    fun getTextureUrl(profile: GameProfile): String {
        val service = mc.apiServices.sessionService()
        val property = service.getPackedTextures(profile)
        val textures = service.unpackTextures(property)
        return textures.skin()?.url ?: ""
    }
}

// ========== ItemStack Extension Functions ==========


fun ItemStack.getCustomData(): NbtCompound? = ItemStackUtils.getCustomData(this)

fun ItemStack.setCustomData(data: NbtCompound) {
    set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data))
}

val ItemStack.skyblockId: String?
    get() = ItemStackUtils.getSkyblockId(getCustomData() ?: return null)

fun ItemStack.isFishingRod(): Boolean = skyblockId?.contains("ROD") ?: false

fun ItemStack.hasItemQuantity(): Boolean {
    return Regex(".* x[0-9]*").matches(name.toPlain())
}

fun ItemStack.getTextures(): GameProfile? = ItemStackUtils.getTextures(this)

fun ItemStack.getTextureUrl(): String {
    val textures = getTextures() ?: return ""
    return ItemStackUtils.getTextureUrl(textures)
}

fun ItemStack.isTextureEqual(textureId: String): Boolean {
    val textures = getTextures() ?: return false
    val url = ItemStackUtils.getTextureUrl(textures)
    return url.endsWith("texture/$textureId")
}

fun ItemStack.getLoreText(): List<Text> {
    val lore = components.get(DataComponentTypes.LORE)
    return lore?.lines() ?: emptyList()
}

fun ItemStack.getLoreLines(): List<String> {
    return getLoreText().map { it.toPlain().trim() }
}

fun ItemStack.getRightClickAbility(): String {
    for (line in getLoreLines()) {
        if (line.contains("Ability: ") && line.endsWith("RIGHT CLICK")) {
            return line
        }
    }
    return ""
}

val ItemStack.hasRightClickAbility: Boolean
    get() = getRightClickAbility().isNotEmpty()

fun ItemStack.hasEitherStat(vararg stats: String): Boolean {
    val lines = getLoreLines()
    for (stat in stats) {
        for (line in lines) {
            if (line.startsWith("$stat:")) {
                return true
            }
        }
    }
    return false
}


val ItemStack.hasGlint: Boolean
    get() {
        val component = componentChanges.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)
        return component != null && component.isPresent
    }

fun ItemStack.setCustomName(style: Style, name: String) {
    set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).setStyle(style.withItalic(false)))
}

val ItemStack.plainCustomName: String
    get() {
        val name = get(DataComponentTypes.CUSTOM_NAME)
        return name?.toPlain() ?: ""
    }

