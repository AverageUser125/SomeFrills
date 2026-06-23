package com.somefrills.mixin;

import com.somefrills.events.PlaySoundEvent;
import com.somefrills.utils.TextUtils;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getVolume()F"
            ),
            cancellable = true
    )
    public void handleSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        boolean isCancelled = new PlaySoundEvent(
                TextUtils.stripPrefix(sound.getId().toString(), "minecraft:"),
                new Vec3(sound.getX(), sound.getY(), sound.getZ()),
                sound.getPitch(),
                sound.getVolume()
        ).post();

        if (isCancelled) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
