package com.somefrills.features.misc.glowmob.chestui

import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class UIInventory(private val size: Int) : Container {

    private val stacks: NonNullList<ItemStack> =
        NonNullList.withSize(size, ItemStack.EMPTY)

    override fun getContainerSize(): Int {
        return size
    }

    override fun isEmpty(): Boolean {
        for (stack in stacks) {
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getItem(slot: Int): ItemStack {
        if (slot !in 0..<size) {
            return ItemStack.EMPTY
        }
        return stacks[slot]
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        if (slot !in 0..<size) {
            return ItemStack.EMPTY
        }

        val stack = stacks[slot]
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }

        val result = stack.split(amount)

        if (stack.isEmpty) {
            stacks[slot] = ItemStack.EMPTY
        }

        setChanged()
        return result
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        if (slot !in 0..<size) {
            return ItemStack.EMPTY
        }

        val stack = stacks[slot]
        stacks[slot] = ItemStack.EMPTY

        if (!stack.isEmpty) {
            setChanged()
        }

        return stack
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        if (slot in 0..<size) {
            stacks[slot] = stack
            setChanged()
        }
    }

    override fun setChanged() {
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun clearContent() {
        stacks.clear()
        setChanged()
    }
}