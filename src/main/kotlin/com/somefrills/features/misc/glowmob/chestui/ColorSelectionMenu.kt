package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.misc.MyMapColor
import com.somefrills.misc.RenderColor
import com.somefrills.misc.RenderColor.Companion.fromHex
import com.somefrills.utils.TextUtils
import com.somefrills.utils.setCustomData
import com.somefrills.utils.getCustomData
import com.somefrills.utils.setCustomName
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import java.util.*

class ColorSelectionMenu(previousScreen: ChestUI?, private val color: RenderColor) : ChestUI("Color", previousScreen) {
    init {
        rebuild()
    }

    override fun build() {
        for (color in MyMapColor.entries) {
            val stack = ItemStack(color.item)
            val colorHex = color.hex
            val name = TextUtils.capitalizeType(color.name.lowercase())
            stack.setCustomName(Style.EMPTY.withColor(colorHex), name)
            val nbt = CompoundTag()
            nbt.putInt("color", colorHex)
            stack.setCustomData(nbt)
            addItem(stack)
        }
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack?.isEmpty ?: return) return
        val colorHex = stack.getCustomData()?.getInt("color")?.orElse(0xFFFFFF) ?: 0xFFFFFF
        color.set(fromHex(colorHex))
        onClose()
    }
}
