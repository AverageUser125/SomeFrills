package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.somefrills.Main.mc;

public abstract class ChestUI extends GenericContainerScreen {
    protected static final int INV_SIZE = 9 * 6;
    private static final long CLICK_COOLDOWN_MS = 50L;

    protected final List<UIAddon> addons = new ArrayList<>();
    protected final List<ItemStack> allItems = new ArrayList<>();
    protected final ChestUI previousScreen;
    protected long lastClickTimestamp = 0L;

    public ChestUI(String title) { this(title, null); }

    public ChestUI(String title, ChestUI previousScreen) {
        super(getHandler(INV_SIZE), Objects.requireNonNull(mc.player).getInventory(), Text.of(title));
        this.previousScreen = previousScreen;
        addAddon(new CloseAddon());
    }

    public void addAddon(UIAddon addon) {
        this.addons.add(addon);
    }

    public void clearAddons() {
        this.addons.clear();
    }

    public static GenericContainerScreenHandler getHandler(int invSize) {
        int syncId = 0;
        Inventory inventory = new UIInventory(invSize);
        return GenericContainerScreenHandler.createGeneric9x6(syncId, mc.player.getInventory(), inventory);
    }

    protected Inventory getInventory() {
        return handler.getInventory();
    }

    public final void rebuild() {
        Inventory inv = getInventory();
        inv.clear();
        fillBorder(inv);

        // 1. Let the subclass define what items exist
        allItems.clear();
        build();

        // 2. Run addons to filter or paginate the list
        List<ItemStack> displayList = new ArrayList<>(allItems);
        for (UIAddon addon : addons) {
            addon.processItems(this, displayList);
        }

        // 3. Render the processed list into the UI
        renderList(inv, displayList);

        // 4. Let addons draw their navigation buttons
        for (UIAddon addon : addons) {
            addon.drawDecoration(this, inv);
        }
    }

    protected abstract void build();

    private void renderList(Inventory inv, List<ItemStack> items) {
        int slotPtr = 0;
        for (ItemStack stack : items) {
            while (slotPtr < inv.size() && isBorderSlot(slotPtr)) {
                slotPtr++;
            }
            if (slotPtr >= inv.size()) break;
            inv.setStack(slotPtr++, stack);
        }
    }

    protected void fillBorder(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (isBorderSlot(i)) {
                inventory.setStack(i, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    protected boolean isBorderSlot(int slotIndex) {
        return slotIndex < 9 || slotIndex >= INV_SIZE - 9 || slotIndex % 9 == 0 || slotIndex % 9 == 8;
    }

    protected void onItemClick(ItemStack stack, int button) {}

    @Override
    public void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot == null || !slot.hasStack()) return;

        long now = System.currentTimeMillis();
        if (now - this.lastClickTimestamp < CLICK_COOLDOWN_MS) return;
        this.lastClickTimestamp = now;

        ItemStack stack = slot.getStack();
        String name = Utils.getPlainCustomName(stack);
        if (name == null) return;

        // Addon Interception
        for (UIAddon addon : addons) {
            if (addon.onClick(this, stack, name, button)) return;
        }

        // Hardcoded Close Logic
        if (name.equals("Close")) {
            this.close();
            return;
        }

        onItemClick(stack, button);
    }

    protected void addItem(ItemStack stack) {
        allItems.add(stack);
    }

    protected void onReturn() {
        rebuild();
    }

    @Override
    public void close() {
        if (this.client.player != null) this.client.player.closeHandledScreen();
        if (this.previousScreen != null) {
            this.previousScreen.onReturn();
            Utils.setScreen(this.previousScreen);
        }
    }

}