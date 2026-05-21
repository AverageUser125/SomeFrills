package com.somefrills.mixin;

import com.somefrills.events.PlaySoundEvent;
import com.somefrills.utils.TextUtils;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/SoundInstance;getVolume()F"
            ),
            cancellable = true
    )
    public void handleSound(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        boolean isCancelled = new PlaySoundEvent(
                TextUtils.stripPrefix(sound.getId().toString(), "minecraft:"),
                new Vec3d(sound.getX(), sound.getY(), sound.getZ()),
                sound.getPitch(),
                sound.getVolume()
        ).post();

        if (isCancelled) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        }
    }
}
