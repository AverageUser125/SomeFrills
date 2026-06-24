package com.somefrills.misc

import com.somefrills.events.ScreenOpenEvent
import com.somefrills.utils.Symbols
import com.somefrills.events.core.EventHandle
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.concurrent.ConcurrentHashMap

object SlotOptions {
    val BACKGROUND: ItemStack = stackWithName(Items.BLACK_STAINED_GLASS_PANE.defaultInstance, " ")
    val SOLID_BACKGROUND: ItemStack = stackWithName(Items.GRAY_CONCRETE.defaultInstance, " ")
    val FIRST: ItemStack = stackWithName(Items.LIME_CONCRETE.defaultInstance, Symbols.format + "aClick here!")
    val SECOND: ItemStack =
        stackWithName(Items.ORANGE_CONCRETE.defaultInstance, Symbols.format + "9Click next.")
    val THIRD: ItemStack = stackWithName(Items.RED_CONCRETE.defaultInstance, Symbols.format + "cClick after.")
    val slotFlags: ConcurrentHashMap<Slot, Flags> = ConcurrentHashMap<Slot, Flags>()

    fun stackWithName(stack: ItemStack, name: String): ItemStack {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name))
        return stack
    }

    fun stackWithName(stack: ItemStack, name: Component): ItemStack {
        stack.set(DataComponents.CUSTOM_NAME, name)
        return stack
    }

    fun stackWithCount(stack: ItemStack, quantity: Int): ItemStack? {
        return stack.copyWithCount(quantity)
    }

    fun getOrInit(slot: Slot?): Flags? {
        if (slot == null) {
            return Flags();
        }
        if (!slotFlags.containsKey(slot)) {
            slotFlags[slot] = Flags()
        }
        return this.slotFlags[slot]
    }

    fun isDisabled(slot: Slot?): Boolean {
        return slot != null && slotFlags.containsKey(slot) && slotFlags[slot]!!.disabled
    }

    fun setDisabled(slot: Slot, disabled: Boolean) {
        getOrInit(slot)!!.setDisabled(disabled)
    }

    fun clearDisabled() {
        for (entry in slotFlags.entries) {
            val value: Flags = entry.value
            if (value.disabled) {
                entry.setValue(value.setDisabled(false))
            }
        }
    }

    @JvmStatic
    fun isSpoofed(slot: Slot?): Boolean {
        if (slot == null) return false
        return slotFlags.containsKey(slot) && slotFlags[slot]!!.spoofed
    }

    @JvmStatic
    fun getSpoofed(slot: Slot?): ItemStack {
        if (isSpoofed(slot)) {
            return slotFlags[slot]!!.replacement
        }
        return ItemStack.EMPTY
    }

    fun setSpoofed(slot: Slot, replacement: ItemStack) {
        getOrInit(slot)!!.setSpoofed(true).setReplacement(replacement)
    }

    fun clearSpoofed(slot: Slot) {
        if (slotFlags.containsKey(slot)) {
            slotFlags[slot]!!.setSpoofed(false)
        }
    }

    fun clearSpoofed() {
        for (entry in slotFlags.entries) {
            val value: Flags = entry.value
            if (value.spoofed) {
                entry.setValue(value.setSpoofed(false))
            }
        }
    }

    @JvmStatic
    fun hasBackground(slot: Slot): Boolean {
        return slotFlags.containsKey(slot) && slotFlags[slot]!!.background
    }

    @JvmStatic
    fun getBackground(slot: Slot): RenderColor {
        if (hasBackground(slot)) {
            return slotFlags[slot]!!.color
        }
        return RenderColor.fromHex(0xffffff)
    }

    fun setBackground(slot: Slot, color: RenderColor) {
        getOrInit(slot)!!.setBackground(true).setBackgroundColor(color)
    }

    fun clearBackground(slot: Slot) {
        if (slotFlags.containsKey(slot)) {
            slotFlags[slot]!!.setBackground(false)
        }
    }

    fun clearBackground() {
        for (entry in slotFlags.entries) {
            val value: Flags = entry.value
            if (value.background) {
                entry.setValue(value.setBackground(false))
            }
        }
    }

    @JvmStatic
    fun hasCount(slot: Slot): Boolean {
        return slotFlags.containsKey(slot) && slotFlags[slot]!!.count != null
    }

    @JvmStatic
    fun getCount(slot: Slot): String? {
        if (hasCount(slot)) {
            return slotFlags[slot]!!.count
        }
        return ""
    }

    fun setCount(slot: Slot, count: String) {
        getOrInit(slot)!!.setCount(count)
    }

    fun clearCount(slot: Slot) {
        if (slotFlags.containsKey(slot)) {
            slotFlags[slot]!!.setCount(null)
        }
    }

    fun clearCount() {
        for (entry in slotFlags.entries) {
            val value: Flags = entry.value
            entry.setValue(value.setCount(null))
        }
    }

    @EventHandle
    private fun onScreen(event: ScreenOpenEvent) {
        slotFlags.clear()
    }

    class Flags {
        var disabled: Boolean = false
        var spoofed: Boolean = false
        var replacement: ItemStack = ItemStack.EMPTY
        var background: Boolean = false
        var color: RenderColor = RenderColor.fromHex(0xffffff)
        var count: String? = null

        fun setDisabled(toggle: Boolean): Flags {
            this.disabled = toggle
            return this
        }

        fun setSpoofed(toggle: Boolean): Flags {
            this.spoofed = toggle
            return this
        }

        fun setReplacement(replacement: ItemStack): Flags {
            this.replacement = replacement
            return this
        }

        fun setBackground(toggle: Boolean): Flags {
            this.background = toggle
            return this
        }

        fun setBackgroundColor(color: RenderColor): Flags {
            this.color = color
            return this
        }

        fun setCount(count: String?): Flags {
            this.count = count
            return this
        }
    }
}