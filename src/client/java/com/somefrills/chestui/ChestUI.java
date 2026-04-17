package com.somefrills.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.somefrills.Main.mc;

public abstract class ChestUI extends GenericContainerScreen {
    protected static final int INV_SIZE = 9 * 6;

    protected int nextSlot = 0;
    protected int currentPage = 0;
    protected int totalPages = 1;

    // Click cooldown to avoid handling rapid repeated clicks (milliseconds)
    protected long lastClickTimestamp = 0L;
    private static final long CLICK_COOLDOWN_MS = 50L;

    protected final List<ItemStack> allItems = new ArrayList<>();
    protected final ChestUI previousScreen;

    public ChestUI(String title) {
        this(title, null);
    }

    public ChestUI(String title, ChestUI previousScreen) {
        super(getHandler(INV_SIZE), Objects.requireNonNull(mc.player).getInventory(), Text.of(title));
        this.previousScreen = previousScreen;
    }

    protected Inventory getInventory() {
        return handler.getInventory();
    }

    public static GenericContainerScreenHandler getHandler(int invSize) {
        if (mc.player == null) {
            throw new IllegalStateException("Player cannot be null when creating ChestContainerScreen");
        }

        int syncId = 0;
        PlayerInventory playerInventory = mc.player.getInventory();
        Inventory inventory = new UIInventory(invSize);

        return GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory);
    }

    public final void rebuild() {
        clearContent();
        fillBorder(getInventory());

        allItems.clear();
        build();

        renderPage();
        updateNavigationArrows(getInventory());
    }

    protected abstract void build();

    protected void addItem(ItemStack stack) {
        allItems.add(stack);
    }

    protected void renderPage() {
        int perPage = getUsableSlotsPerPage();
        totalPages = (int) Math.ceil((double) allItems.size() / perPage);

        int start = currentPage * perPage;
        int end = Math.min(start + perPage, allItems.size());

        nextSlot = 0;

        for (int i = start; i < end; i++) {
            placeItem(getInventory(), allItems.get(i));
        }
    }

    private void placeItem(Inventory inventory, ItemStack stack) {
        for (int i = nextSlot; i < inventory.size(); i++) {
            if (isBorderSlot(i)) continue;

            inventory.setStack(i, stack);
            nextSlot = i + 1;
            return;
        }
    }

    protected int getUsableSlotsPerPage() {
        int count = 0;
        for (int i = 0; i < INV_SIZE; i++) {
            if (!isBorderSlot(i)) count++;
        }
        return count;
    }

    protected void fillBorder(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (isBorderSlot(i)) {
                inventory.setStack(i, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    protected void updateNavigationArrows(Inventory inventory) {
        int backSlot = INV_SIZE - 9 + 3;
        int forwardSlot = INV_SIZE - 9 + 5;
        int closeSlot = INV_SIZE - 9 + 4;

        if (currentPage > 0) {
            ItemStack backArrow = new ItemStack(Items.ARROW);
            Utils.setCustomName(backArrow, Style.EMPTY, "Previous Page");
            inventory.setStack(backSlot, backArrow);
        }

        ItemStack closeButton = new ItemStack(Items.BARRIER);
        Style barrierStyle = Style.EMPTY.withColor(Formatting.GRAY);
        Utils.setCustomName(closeButton, barrierStyle, "Close");
        inventory.setStack(closeSlot, closeButton);

        if (currentPage < totalPages - 1) {
            ItemStack forwardArrow = new ItemStack(Items.ARROW);
            Utils.setCustomName(forwardArrow, Style.EMPTY, "Next Page");
            inventory.setStack(forwardSlot, forwardArrow);
        }
    }

    protected boolean isBorderSlot(int slotIndex) {
        return slotIndex < 9 ||
                slotIndex >= INV_SIZE - 9 ||
                slotIndex % 9 == 0 ||
                slotIndex % 9 == 8;
    }

    private void clearContent() {
        Inventory inventory = handler.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (!isBorderSlot(i)) {
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    protected void onItemClick(ItemStack stack, int button) {
    }

    protected void onReturn() {
        rebuild();
    }

    @Override
    public void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot == null || !slot.hasStack()) return;

        // Enforce click cooldown: ignore clicks occurring within CLICK_COOLDOWN_MS of the last handled click
        long now = System.currentTimeMillis();
        if (now - this.lastClickTimestamp < CLICK_COOLDOWN_MS) return;
        this.lastClickTimestamp = now;

        ItemStack stack = slot.getStack();
        String name = Utils.getPlainCustomName(stack);
        if (name == null || name.isEmpty()) return;
        switch (name) {
            case "Previous Page" -> {
                if (currentPage > 0) {
                    currentPage--;
                    rebuild();
                }
                return;
            }
            case "Next Page" -> {
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    rebuild();
                }
                return;
            }
            case "Close" -> {
                this.close();
                return;
            }
        }

        onItemClick(stack, button);
    }

    @Override
    public void close() {
        if (this.client.player != null) {
            this.client.player.closeHandledScreen();
        }

        if (this.previousScreen != null) {
            this.previousScreen.onReturn();
        }
        Utils.setScreen(this.previousScreen);
    }
}