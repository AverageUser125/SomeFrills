package com.example.tweaks;

import com.example.mixin.ClientPlayerInteractionManagerAccessor;
import com.example.utils.AllConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

import static com.example.Main.mc;

public class BreakResetFix {
    // https://github.com/WhatYouThing/NoFrills/blob/08b2e81e228a4e74bc5afacf9c234ce7a2e569a8/src/main/java/nofrills/features/mining/BreakResetFix.java#L10
    public static void onInventoryUpdate(ScreenHandlerSlotUpdateS2CPacket packet, ItemStack stack, int slotId) {
        if (AllConfig.breakResetFix && mc.player != null && mc.interactionManager != null) {
            if (slotId >= 36 && slotId <= 44 && mc.player.getInventory().getSelectedSlot() == slotId - 36) {
                ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setStack(stack);
            } // manually update the variable once the server updates our held item, prevents the mismatch and thus fixes the break cancel
        }
    }
}