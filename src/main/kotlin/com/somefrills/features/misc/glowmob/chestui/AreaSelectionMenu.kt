package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.Area
import com.somefrills.misc.Area.Companion.fromString
import com.somefrills.misc.Utils
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.TextColor

class AreaSelectionMenu(previousScreen: ChestUI?, private val info: MatchInfo) :
    ChestUI("Select Area", previousScreen) {
    init {
        rebuild()
    }

    override fun build() {
        for (area in Area.values()) {
            addItem(createChoiceItem(area.item, area.displayName, area.colorHex))
        }
        addItem(createChoiceItem(Items.STRUCTURE_VOID, "None", 0xFF0000))
    }

    private fun createChoiceItem(baseItem: Item, displayName: String?, colorHex: Int): ItemStack {
        val stack = baseItem.getDefaultStack()
        Utils.setCustomName(stack, Style.EMPTY.withColor(TextColor.fromRgb(colorHex)), displayName)
        return stack
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        val customName = Utils.getPlainCustomName(stack)
        if (customName == "None") {
            info.area = null
        } else {
            info.area = fromString(customName)
        }
        close()
    }
}
