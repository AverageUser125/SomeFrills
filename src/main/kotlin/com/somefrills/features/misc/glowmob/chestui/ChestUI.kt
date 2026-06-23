package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.Main.mc
import com.somefrills.utils.GuiUtils
import com.somefrills.utils.plainCustomName
import io.github.notenoughupdates.moulconfig.common.ClickType
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

abstract class ChestUI @JvmOverloads constructor(
    title: String,
    @JvmField val previousScreen: ChestUI? = null
) : ContainerScreen(
    getHandler(INV_SIZE),
    mc.player!!.inventory,
    Component.literal(title)
) {

    protected val addons: MutableList<UIAddon> = ArrayList()

    @JvmField
    protected val allItems: MutableList<ItemStack> = ArrayList()

    protected var lastClickTimestamp: Long = 0L


    init {
        addAddon(CloseAddon())
    }


    fun addAddon(addon: UIAddon?) {
        addons.add(addon!!)
    }

    fun clearAddons() {
        addons.clear()
    }


    protected val inventory: Inventory
        get() = this.minecraft!!.player!!.inventory


    fun rebuild() {
        val inv = this.menu.container

        inv.clearContent()
        fillBorder(inv)

        allItems.clear()
        build()

        val displayList = ArrayList(allItems)

        for (addon in addons) {
            addon.processItems(this, displayList)
        }

        renderList(inv, displayList)

        for (addon in addons) {
            addon.drawDecoration(this, inv)
        }
    }


    protected abstract fun build()


    private fun renderList(inv: net.minecraft.world.Container, items: MutableList<ItemStack>) {
        var slotPtr = 0

        for (stack in items) {
            while (slotPtr < inv.containerSize && isBorderSlot(slotPtr)) {
                slotPtr++
            }

            if (slotPtr >= inv.containerSize) {
                break
            }

            inv.setItem(slotPtr++, stack)
        }
    }


    protected fun fillBorder(inventory: net.minecraft.world.Container) {
        for (i in 0 until inventory.containerSize) {
            if (isBorderSlot(i)) {
                inventory.setItem(
                    i,
                    ItemStack(Items.GRAY_STAINED_GLASS_PANE)
                )
            }
        }
    }


    protected fun isBorderSlot(slotIndex: Int): Boolean {
        return slotIndex < 9 ||
                slotIndex >= INV_SIZE - 9 ||
                slotIndex % 9 == 0 ||
                slotIndex % 9 == 8
    }


    protected open fun onItemClick(stack: ItemStack?, button: Int) {
    }

    override fun slotClicked(
        slot: Slot, slotId: Int, buttonNum: Int, containerInput: ContainerInput
    ) {
        if (!slot.hasItem()) return

        val now = System.currentTimeMillis()

        if (now - lastClickTimestamp < CLICK_COOLDOWN_MS) {
            return
        }

        lastClickTimestamp = now

        val stack = slot.item
        val name = stack.plainCustomName

        for (addon in addons) {
            if (addon.onClick(this, stack, name, buttonNum)) {
                return
            }
        }

        if (name == "Close") {
            super.onClose()
            return
        }

        onItemClick(stack, buttonNum)
    }


    protected fun addItem(stack: ItemStack) {
        allItems.add(stack)
    }


    protected open fun onReturn() {
        rebuild()
    }


    override fun onClose() {
        if (minecraft?.player != null) {
            minecraft!!.player!!.closeContainer()
        }

        previousScreen?.let {
            it.onReturn()
            GuiUtils.setScreen(it)
        }
    }


    companion object {

        const val INV_SIZE = 9 * 6
        private const val CLICK_COOLDOWN_MS = 50L


        fun getHandler(invSize: Int): ChestMenu {
            val syncId = 0

            val container = UIInventory(invSize)

            return ChestMenu.sixRows(
                syncId,
                mc.player!!.inventory,
                container
            )
        }
    }
}