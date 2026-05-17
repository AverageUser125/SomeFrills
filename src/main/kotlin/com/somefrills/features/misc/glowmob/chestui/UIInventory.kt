package com.somefrills.features.misc.glowmob.chestui

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

class UIInventory(private val size: Int) : Inventory {
    val stacks: DefaultedList<ItemStack> = DefaultedList.ofSize(size, ItemStack.EMPTY)

    override fun size(): Int {
        return this.size
    }

    override fun isEmpty(): Boolean {
        for (stack in this.stacks) {
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getStack(slot: Int): ItemStack? {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY
        }
        return this.stacks.get(slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack? {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY
        }
        val stack = this.stacks.get(slot)
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }
        val result = stack.split(amount)
        if (stack.isEmpty) {
            this.stacks.set(slot, ItemStack.EMPTY)
        }
        this.markDirty()
        return result
    }

    override fun removeStack(slot: Int): ItemStack {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY
        }
        val stack = this.stacks.get(slot)
        this.stacks.set(slot, ItemStack.EMPTY)
        if (!stack.isEmpty) {
            this.markDirty()
        }
        return stack
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        if (slot >= 0 && slot < this.size) {
            this.stacks.set(slot, stack)
            this.markDirty()
        }
    }

    override fun markDirty() {
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

    override fun clear() {
        this.stacks.clear()
        this.markDirty()
    }
}


