package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.utils.GuiUtils
import com.somefrills.utils.plainCustomName
import com.somefrills.utils.setCustomName
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import java.util.*
import java.util.function.Consumer

class SearchAddon : UIAddon {
    private var searchQuery: String? = null

    var searchSlot: Int = ChestUI.INV_SIZE - 9 + 2
    var clearSlot: Int = ChestUI.INV_SIZE - 9 + 1

    override fun processItems(ui: ChestUI, items: MutableList<ItemStack>) {
        if (searchQuery == null || searchQuery!!.isEmpty()) return

        val query = searchQuery!!.lowercase(Locale.getDefault())
        items.removeIf { stack: ItemStack ->
            !stack.plainCustomName.lowercase(Locale.getDefault()).contains(query)
        }
    }

    override fun drawDecoration(ui: ChestUI, inventory: Container) {
        // --- Search Compass ---
        val searchItem = ItemStack(Items.COMPASS)
        searchItem.setCustomName(colorStyle(ChatFormatting.AQUA).withItalic(false), "Search")

        val lore: MutableList<Component> = ArrayList<Component>()
        lore.add(
            Component.literal("Current Filter: ").setStyle(colorStyle(ChatFormatting.GRAY))
                .append(
                    Component.literal(searchQuery ?: "None")
                        .setStyle(colorStyle(if (searchQuery == null) ChatFormatting.RED else ChatFormatting.YELLOW))
                )
        )

        lore.add(Component.literal(""))
        lore.add(Component.literal("Click to filter results").setStyle(colorStyle(ChatFormatting.YELLOW)))

        searchItem.set(DataComponents.LORE, ItemLore(lore, lore))
        inventory.setItem(searchSlot, searchItem)

        // --- Clear Search (Oak Sign) ---
        if (searchQuery != null && !searchQuery!!.isEmpty()) {
            val clearItem = ItemStack(Items.OAK_SIGN)
            clearItem.setCustomName(colorStyle(ChatFormatting.RED).withItalic(false), "Clear Search")

            val clearLore: MutableList<Component> = ArrayList<Component>()
            clearLore.add(Component.literal("Reset the search filter").setStyle(colorStyle(ChatFormatting.GRAY)))
            clearItem.set(DataComponents.LORE, ItemLore(clearLore, clearLore))

            inventory.setItem(clearSlot, clearItem)
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
                GuiUtils.setScreen(ui)
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

    private fun colorStyle(color: ChatFormatting): Style {
        val colorValue = color.color
        return if (colorValue == null) Style.EMPTY else Style.EMPTY.withColor(TextColor.fromRgb(colorValue))
    }
}