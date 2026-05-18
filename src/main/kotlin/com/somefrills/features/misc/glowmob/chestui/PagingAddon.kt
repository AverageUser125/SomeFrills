package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.utils.setCustomName
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PagingAddon : UIAddon {
    private var currentPage = 0
    private var totalPages = 1

    // Sensible Defaults
    var prevSlot: Int = ChestUI.INV_SIZE - 9 + 3
    var nextSlot: Int = ChestUI.INV_SIZE - 9 + 5
    var perPage: Int = 7 * 4

    override fun processItems(ui: ChestUI, items: MutableList<ItemStack>) {
        // Calculate total pages based on current items (filtered or otherwise)
        // Math.max(1, ...) ensures we don't have 0 pages, which simplifies logic
        totalPages = max(1, ceil(items.size.toDouble() / perPage).toInt())

        if (currentPage >= totalPages) currentPage = totalPages - 1
        if (currentPage < 0) currentPage = 0

        val start = currentPage * perPage
        val end = min(start + perPage, items.size)

        // If list is empty or start > size, subList might throw error, so we check
        if (start < items.size) {
            val pageItems: MutableList<ItemStack> = ArrayList<ItemStack>(items.subList(start, end))
            items.clear()
            items.addAll(pageItems)
        } else {
            items.clear()
        }
    }

    override fun drawDecoration(ui: ChestUI, inventory: Inventory) {
        // 1. Only show "Previous" if we aren't on the first page
        if (currentPage > 0) {
            val back = ItemStack(Items.ARROW)
            back.setCustomName(Style.EMPTY, "Previous Page")
            inventory.setStack(prevSlot, back)
        }

        // 2. Only show "Next" if there is at least one page ahead of us
        // This implicitly handles the "Single Page" case: 0 < 1 - 1 is false.
        if (currentPage < totalPages - 1) {
            val forward = ItemStack(Items.ARROW)
            forward.setCustomName(Style.EMPTY, "Next Page")
            inventory.setStack(nextSlot, forward)
        }
    }

    override fun onClick(ui: ChestUI, stack: ItemStack, name: String, button: Int): Boolean {
        if (name == "Previous Page") {
            if (currentPage > 0) {
                currentPage--
                ui.rebuild()
            }
            return true
        }
        if (name == "Next Page") {
            if (currentPage < totalPages - 1) {
                currentPage++
                ui.rebuild()
            }
            return true
        }
        return false
    }
}