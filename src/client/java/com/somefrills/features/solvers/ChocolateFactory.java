package com.somefrills.features.solvers;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingDescription;
import com.somefrills.config.SettingInt;
import com.somefrills.events.ScreenRenderEvent;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric 1.21.10 port of the Kotlin ChocolateFactory module.
 * Automatically claims stray rabbits in the Chocolate Factory menu.
 */
public class ChocolateFactory {
    public static final Feature instance = new Feature("chocolateFactory");
    private static final String CHOCOLATE_FACTORY_TITLE = "Chocolate Factory";
    @SettingDescription("Automatically claim stray items in Chocolate Factory menu")
    public static SettingBool claimStray = new SettingBool(true);
    @SettingDescription("Delay between claim attempts in milliseconds")
    public static SettingInt claimDelay = new SettingInt(100);
    private static long lastClaimTime = 0;

    @EventHandler
    public static void onHudTick(ScreenRenderEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        // Check if auto claim is enabled
        if (!claimStray.value()) return;

        LocalPlayer player = client.player;
        AbstractContainerMenu handler = player.containerMenu;
        if (handler == null) return;

        String title = "";
        if (client.screen != null) {
            Component txt = client.screen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Check if we're in the Chocolate Factory menu
        if (!title.equals(CHOCOLATE_FACTORY_TITLE)) return;


        // Scan for claimable items
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getItem();
            if (stack == null || stack.isEmpty()) continue;

            String displayName = stack.getHoverName().getString();

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("CLICK ME!")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < claimDelay.value()) return;
                Utils.clickSlot(slot.getContainerSlot());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("Golden Rabbit")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < claimDelay.value() * 100L) return;
                Utils.clickSlot(slot.getContainerSlot());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }
        }
    }

}