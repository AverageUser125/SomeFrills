package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.Main.mc
import com.somefrills.misc.Utils
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

abstract class ChestUI @JvmOverloads constructor(title: String?, @JvmField val previousScreen: ChestUI? = null) :
    GenericContainerScreen(
        getHandler(INV_SIZE), mc.player!!.getInventory(), Text.of(title)
    ) {
    protected val addons: MutableList<UIAddon> = ArrayList()

    @JvmField
    protected val allItems: MutableList<ItemStack> = ArrayList()
    protected var lastClickTimestamp: Long = 0L

    init {
        addAddon(CloseAddon())
    }

    fun addAddon(addon: UIAddon?) {
        this.addons.add(addon!!)
    }

    fun clearAddons() {
        this.addons.clear()
    }

    protected val inventory: Inventory
        get() = handler.inventory

    fun rebuild() {
        val inv = this.inventory
        inv.clear()
        fillBorder(inv)

        // 1. Let the subclass define what items exist
        allItems.clear()
        build()

        // 2. Run addons to filter or paginate the list
        val displayList: MutableList<ItemStack> = ArrayList(allItems)
        for (addon in addons) {
            addon.processItems(this, displayList)
        }

        // 3. Render the processed list into the UI
        renderList(inv, displayList)

        // 4. Let addons draw their navigation buttons
        for (addon in addons) {
            addon.drawDecoration(this, inv)
        }
    }

    protected abstract fun build()

    private fun renderList(inv: Inventory, items: MutableList<ItemStack>) {
        var slotPtr = 0
        for (stack in items) {
            while (slotPtr < inv.size() && isBorderSlot(slotPtr)) {
                slotPtr++
            }
            if (slotPtr >= inv.size()) break
            inv.setStack(slotPtr++, stack)
        }
    }

    protected fun fillBorder(inventory: Inventory) {
        for (i in 0..<inventory.size()) {
            if (isBorderSlot(i)) {
                inventory.setStack(i, ItemStack(Items.GRAY_STAINED_GLASS_PANE))
            }
        }
    }

    protected fun isBorderSlot(slotIndex: Int): Boolean {
        return slotIndex < 9 || slotIndex >= INV_SIZE - 9 || slotIndex % 9 == 0 || slotIndex % 9 == 8
    }

    protected open fun onItemClick(stack: ItemStack?, button: Int) {
    }

    override fun onMouseClick(slot: Slot?, slotId: Int, button: Int, actionType: SlotActionType?) {
        if (slot == null || !slot.hasStack()) return

        val now = System.currentTimeMillis()
        if (now - this.lastClickTimestamp < CLICK_COOLDOWN_MS) return
        this.lastClickTimestamp = now

        val stack = slot.stack
        val name = Utils.getPlainCustomName(stack) ?: return

        // Addon Interception
        for (addon in addons) {
            if (addon.onClick(this, stack, name, button)) return
        }

        // Hardcoded Close Logic
        if (name == "Close") {
            this.close()
            return
        }

        onItemClick(stack, button)
    }

    protected fun addItem(stack: ItemStack) {
        allItems.add(stack)
    }

    protected open fun onReturn() {
        rebuild()
    }

    override fun close() {
        if (this.client.player != null) this.client.player!!.closeHandledScreen()
        if (this.previousScreen != null) {
            this.previousScreen.onReturn()
            Utils.setScreen(this.previousScreen)
        }
    }

    companion object {
        const val INV_SIZE: Int = 9 * 6
        private const val CLICK_COOLDOWN_MS = 50L

        fun getHandler(invSize: Int): GenericContainerScreenHandler {
            val syncId = 0
            val inventory: Inventory = UIInventory(invSize)
            return GenericContainerScreenHandler.createGeneric9x6(syncId, mc.player!!.getInventory(), inventory)
        }
    }
}