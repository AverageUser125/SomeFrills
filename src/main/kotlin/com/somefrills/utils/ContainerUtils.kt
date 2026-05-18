package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

object ContainerUtils {
    fun clickSlotInternal(slotIdx: Int) {
        val player = mc.player ?: return
        val interactionManager = mc.interactionManager ?: return

        interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            slotIdx,
            0,
            SlotActionType.PICKUP,
            player
        )
    }

    fun getContainerSlotsInternal(handler: GenericContainerScreenHandler, inverse: Boolean): List<Slot> {
        return if (inverse) {
            handler.slots.filter { slot -> slot.id >= handler.rows * 9 }
        } else {
            handler.slots.filter { slot -> slot.id < handler.rows * 9 }
        }
    }

// ========== Global Slot Management ==========

    fun clickSlot(slotIdx: Int) {
        ContainerUtils.clickSlotInternal(slotIdx)
    }

    fun clickSlotQuickMove(slotIdx: Int) {
        val player = mc.player ?: return
        val interactionManager = mc.interactionManager ?: return

        interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            slotIdx,
            0,
            SlotActionType.QUICK_MOVE,
            player
        )
    }

    fun clickSlotSwap(slotIdx: Int, hotbarSlot: Int) {
        val player = mc.player ?: return
        val interactionManager = mc.interactionManager ?: return

        interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            slotIdx,
            hotbarSlot,
            SlotActionType.SWAP,
            player
        )
    }

    val currentlyOpenContainer: GenericContainerScreenHandler?
        get() {
            val screenHandler = mc.player?.currentScreenHandler
            return screenHandler as? GenericContainerScreenHandler
        }
}

// ========== GenericContainerScreenHandler Extension Functions ==========

val GenericContainerScreenHandler.containerSlots: List<Slot>
    get() = ContainerUtils.getContainerSlotsInternal(this, false)

val GenericContainerScreenHandler.playerInventorySlots: List<Slot>
    get() = ContainerUtils.getContainerSlotsInternal(this, true)

// ========== Slot Access Extension Functions ==========

fun GenericContainerScreenHandler.getSlot(index: Int): Slot? {
    return slots.getOrNull(index)
}

val GenericContainerScreenHandler.slotCount: Int
    get() = slots.size

fun GenericContainerScreenHandler.getContainerSlotAt(row: Int, col: Int): Slot? {
    val index = row * 9 + col
    if (index >= rows * 9) return null
    return getSlot(index)
}
