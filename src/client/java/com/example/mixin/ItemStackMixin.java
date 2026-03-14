package com.example.mixin;


import com.example.AllConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Main.mc;

// https://github.com/WhatYouThing/NoFrills/blob/main/src/main/java/nofrills/mixin/ItemStackMixin.java#L21
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "capCount", at = @At("HEAD"), cancellable = true)
    private void onCapCount(int maxCount, CallbackInfo ci) {
        if (!AllConfig.itemCountFix) {
            ci.cancel();
        }
    }

    @Inject(method = "applyRemainderAndCooldown", at = @At("HEAD"), cancellable = true)
    private void onApplyCooldown(LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (user.equals(mc.player) && AllConfig.noPearlCooldown) {
            if (stack.getItem().equals(Items.ENDER_PEARL)) {
                cir.setReturnValue(stack);
            }
        }
    }
}