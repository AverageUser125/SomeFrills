package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.Area
import com.somefrills.misc.Area.Companion.fromString
import com.somefrills.utils.plainCustomName
import com.somefrills.utils.setCustomName
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class AreaSelectionMenu(previousScreen: ChestUI?, private val info: MatchInfo) :
    ChestUI("Select Area", previousScreen) {
    init {
        rebuild()
    }

    override fun build() {
        for (area in Area.entries) {
            addItem(createChoiceItem(area.item, area.displayName, area.colorHex))
        }
        addItem(createChoiceItem(Items.STRUCTURE_VOID, "None", 0xFF0000))
    }

    private fun createChoiceItem(baseItem: Item, displayName: String, colorHex: Int): ItemStack {
        val stack = baseItem.defaultInstance
        stack.setCustomName(Style.EMPTY.withColor(TextColor.fromRgb(colorHex)), displayName)
        return stack
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        val customName = stack?.plainCustomName ?: return
        if (customName == "None") {
            info.area = null
        } else {
            info.area = fromString(customName)
        }
        onClose()
    }
}
