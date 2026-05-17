package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.misc.MyMapColor
import com.somefrills.misc.RenderColor
import com.somefrills.misc.RenderColor.Companion.fromHex
import com.somefrills.misc.Utils
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Style
import java.util.*

class ColorSelectionMenu(previousScreen: ChestUI?, private val color: RenderColor) : ChestUI("Color", previousScreen) {
    init {
        rebuild()
    }

    override fun build() {
        for (color in MyMapColor.values()) {
            val stack = ItemStack(color.item)
            val colorHex = color.hex
            val name = Utils.capitalizeType(color.name.lowercase(Locale.getDefault()))
            Utils.setCustomName(stack, Style.EMPTY.withColor(colorHex), name)
            val nbt = NbtCompound()
            nbt.putInt("color", colorHex)
            Utils.setCustomData(stack, nbt)
            addItem(stack)
        }
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack?.isEmpty ?: return) return
        val colorHex = Utils.getCustomData(stack).getInt("color").orElse(0xFFFFFF)
        color.set(fromHex(colorHex))
        close()
    }
}
