package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.misc.Utils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import java.util.*
import java.util.function.Consumer

class SearchAddon : UIAddon {
    private var searchQuery: String? = null

    var searchSlot: Int = ChestUI.INV_SIZE - 9 + 2
    var clearSlot: Int = ChestUI.INV_SIZE - 9 + 1

    override fun processItems(ui: ChestUI, items: MutableList<ItemStack>) {
        if (searchQuery == null || searchQuery!!.isEmpty()) return

        val query = searchQuery!!.lowercase(Locale.getDefault())
        items.removeIf { stack: ItemStack? ->
            val name = Utils.getPlainCustomName(stack)
            name == null || !name.lowercase(Locale.getDefault()).contains(query)
        }
    }

    override fun drawDecoration(ui: ChestUI, inventory: Inventory) {
        // --- Search Compass ---
        val searchItem = ItemStack(Items.COMPASS)
        Utils.setCustomName(searchItem, colorStyle(Formatting.AQUA).withItalic(false), "Search")

        val lore: MutableList<Text?> = ArrayList<Text?>()
        lore.add(
            Text.literal("Current Filter: ").setStyle(colorStyle(Formatting.GRAY))
                .append(
                    Text.literal(if (searchQuery == null) "None" else searchQuery)
                        .setStyle(colorStyle(if (searchQuery == null) Formatting.RED else Formatting.YELLOW))
                )
        )

        lore.add(Text.literal(""))
        lore.add(Text.literal("Click to filter results").setStyle(colorStyle(Formatting.YELLOW)))

        searchItem.set<LoreComponent?>(DataComponentTypes.LORE, LoreComponent(lore, lore))
        inventory.setStack(searchSlot, searchItem)

        // --- Clear Search (Oak Sign) ---
        if (searchQuery != null && !searchQuery!!.isEmpty()) {
            val clearItem = ItemStack(Items.OAK_SIGN)
            Utils.setCustomName(clearItem, colorStyle(Formatting.RED).withItalic(false), "Clear Search")

            val clearLore: MutableList<Text?> = ArrayList<Text?>()
            clearLore.add(Text.literal("Reset the search filter").setStyle(colorStyle(Formatting.GRAY)))
            clearItem.set(DataComponentTypes.LORE, LoreComponent(clearLore, clearLore))

            inventory.setStack(clearSlot, clearItem)
        }
    }

    override fun onClick(ui: ChestUI, stack: ItemStack, name: String, button: Int): Boolean {
        if (name == "Search") {
            // Line 1: Header, Line 2: Current Query
            val signText: Array<String> =
                arrayOf<String>("Enter Query", (if (searchQuery == null) "" else searchQuery)!!, "", "")

            SignGui.open(signText, Consumer { lines: Array<String> ->
                // We take the input from the second line (index 1)
                val input = lines[1].trim { it <= ' ' }
                this.searchQuery = input.ifEmpty { null }

                ui.rebuild()
                Utils.setScreen(ui)
            })
            return true
        }

        if (name == "Clear Search") {
            this.searchQuery = null
            ui.rebuild()
            return true
        }
        return false
    }

    private fun colorStyle(color: Formatting): Style {
        val colorValue = color.getColorValue()
        return if (colorValue == null) Style.EMPTY else Style.EMPTY.withColor(TextColor.fromRgb(colorValue))
    }
}