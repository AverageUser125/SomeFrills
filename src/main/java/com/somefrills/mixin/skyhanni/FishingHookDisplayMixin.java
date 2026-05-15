package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.features.fishing.FishingHookDisplay;
import at.hannibal2.skyhanni.utils.DelayedRun;
import com.somefrills.config.FrillsConfig;
import kotlin.Unit;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.mc;

@Mixin(FishingHookDisplay.class)
public class FishingHookDisplayMixin {
    @Shadow
    private static ArmorStandEntity armorStand;

    @Unique
    private static boolean isReelingIn = false;

    @Inject(method = "onTick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (!FishingHookDisplay.INSTANCE.isEnabled()) return;
        var autoFish = FrillsConfig.instance.fishing.autoFish;
        if (!autoFish.enabled) return;
        if (armorStand == null) return;
        if (isReelingIn) return;
        boolean shouldCatch = armorStand.getName().getString().contains("!!!");
        if (!shouldCatch) return;
        isReelingIn = true;
        DelayedRun.INSTANCE.runNextTick(() -> {
                    DelayedRun.INSTANCE.runNextTick(() -> {
                        isReelingIn = false;
                        mc.options.leftKey.setPressed(true);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                }
        );
    }
}



