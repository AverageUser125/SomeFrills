package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ContainerUtils {
    fun clickSlot(slotIdx: Int) {
        val screen = mc.screen as? AbstractContainerScreen<*> ?: return
        screen.slotClicked(screen.menu.getSlot(slotIdx), slotIdx, 0, ContainerInput.PICKUP)
    }

    fun getContainerSlotsInternal(handler: ChestMenu, inverse: Boolean): List<Slot> {
        return if (inverse) {
            handler.slots.filter { slot -> slot.index >= handler.rowCount * 9 }
        } else {
            handler.slots.filter { slot -> slot.index < handler.rowCount * 9 }
        }
    }

    fun clickSlot(slot: Slot) {
        clickSlot(slot.index)
    }

    fun clickSlotQuickMove(slotIdx: Int) {
        val player = mc.player ?: return
        val interactionManager = mc.gameMode ?: return
        val windowId = getWindowIdOrNull() ?: return

        interactionManager.handleContainerInput(
            windowId,
            slotIdx,
            0,
            ContainerInput.QUICK_MOVE,
            player
        )
    }

    fun getWindowIdOrNull(): Int? =
        currentlyOpenContainer?.containerId

    fun clickSlotSwap(slotIdx: Int, hotbarSlot: Int) {
        val player = mc.player ?: return
        val interactionManager = mc.gameMode ?: return
        val windowId = getWindowIdOrNull() ?: return

        interactionManager.handleContainerInput(
            windowId,
            slotIdx,
            hotbarSlot,
            ContainerInput.SWAP,
            player
        )
    }

    val currentlyOpenContainer: ChestMenu?
        get() {
            return (mc.screen as? ContainerScreen)?.menu
        }

    fun getItemsInOpenChest(): List<Slot> {
        return getItemsInOpenChestWithNull().filter { it.item.isNotEmpty() }
    }

    fun getItemsInOpenChestWithNull(): List<Slot> {
        val guiChest = mc.screen as? ContainerScreen ?: return emptyList()
        return guiChest.menu.slots
    }
}

// ========== ChestMenu Extension Functions ==========

val ChestMenu.containerSlots: List<Slot>
    get() = ContainerUtils.getContainerSlotsInternal(this, false)

val ChestMenu.playerInventorySlots: List<Slot>
    get() = ContainerUtils.getContainerSlotsInternal(this, true)

// ========== Slot Access Extension Functions ==========

fun ChestMenu.getSlot(index: Int): Slot? {
    return slots.getOrNull(index)
}

val ChestMenu.slotCount: Int
    get() = slots.size

fun ChestMenu.getContainerSlotAt(row: Int, col: Int): Slot? {
    val index = row * 9 + col
    if (index >= rowCount * 9) return null
    return getSlot(index)
}

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isNotEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotEmpty != null)
    }
    this ?: return false
    return !this.isEmpty
}

val Slot.item: ItemStack get() = this.container.getItem(this.index)