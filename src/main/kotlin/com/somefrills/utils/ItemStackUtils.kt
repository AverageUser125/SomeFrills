package com.somefrills.utils

import com.mojang.authlib.GameProfile
import com.somefrills.Main.mc
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

object ItemStackUtils {
    fun getSkyblockId(customData: CompoundTag): String? {
        if (!customData.contains("id")) return null
        return customData.getString("id").orElse(null)
    }

    @JvmStatic
    fun getCustomData(stack: ItemStack): CompoundTag? {
        if (!stack.isEmpty) {
            val data = stack.get(DataComponents.CUSTOM_DATA)
            if (data != null) {
                return data.tag
            }
        }
        return null
    }

    fun getTextures(stack: ItemStack): GameProfile? {
        val profile = stack.components.get(DataComponents.PROFILE)
        return if (!stack.isEmpty && profile != null) profile.partialProfile() else null
    }

    fun getTextureUrl(profile: GameProfile): String {
        val service = mc.services().sessionService()
        val property = service.getPackedTextures(profile)
        val textures = service.unpackTextures(property)
        return textures.skin()?.url ?: ""
    }
}

// ========== ItemStack Extension Functions ==========


fun ItemStack.getCustomData(): CompoundTag? = ItemStackUtils.getCustomData(this)

fun ItemStack.setCustomData(data: CompoundTag) {
    set(DataComponents.CUSTOM_DATA, CustomData.of(data))
}

val ItemStack.skyblockId: String?
    get() = ItemStackUtils.getSkyblockId(getCustomData() ?: return null)

fun ItemStack.isFishingRod(): Boolean = skyblockId?.contains("ROD") ?: false

fun ItemStack.hasItemQuantity(): Boolean {
    return Regex(".* x[0-9]*").matches(displayName.toPlain())
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

fun ItemStack.getLoreText(): List<Component> {
    val lore = components.get(DataComponents.LORE)
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
        val component: Boolean? = get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
        return component != null && component
    }

fun ItemStack.setCustomName(style: Style, name: String) {
    set(DataComponents.CUSTOM_NAME, Component.literal(name).setStyle(style.withItalic(false)))
}

fun Item.getIdentifierString(): String {
    return BuiltInRegistries.ITEM.getId(this).toString()
}

val ItemStack.plainCustomName: String
    get() {
        val name = get(DataComponents.CUSTOM_NAME)
        return name?.toPlain() ?: ""
    }

