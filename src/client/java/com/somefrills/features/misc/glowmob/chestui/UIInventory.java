package com.somefrills.features.misc.glowmob.chestui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class UIInventory implements Inventory {
    private final int size;
    private final DefaultedList<ItemStack> stacks;

    public UIInventory(int size) {
        this.size = size;
        this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.stacks.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.split(amount);
        if (stack.isEmpty()) {
            this.stacks.set(slot, ItemStack.EMPTY);
        }
        this.markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot < 0 || slot >= this.size) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.stacks.get(slot);
        this.stacks.set(slot, ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.size) {
            this.stacks.set(slot, stack);
            this.markDirty();
        }
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
        this.stacks.addAll(DefaultedList.ofSize(this.size, ItemStack.EMPTY));
        this.markDirty();
    }

    public DefaultedList<ItemStack> getStacks() {
        return this.stacks;
    }
}


