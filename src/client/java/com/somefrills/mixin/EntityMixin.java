package com.somefrills.mixin;

import com.somefrills.features.mining.GhostVision;
import com.somefrills.features.misc.GlowPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperVisible(CallbackInfoReturnable<Boolean> cir) {
        // Make invisible creepers fully visible (client-side) if config enabled
        if (GhostVision.instance.isActive() && GhostVision.makeCreepersVisible.value()) {
            if ((Object) this instanceof Creeper creeper) {
                if (GhostVision.isGhost(creeper)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "isCustomNameVisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperNameVisible(CallbackInfoReturnable<Boolean> cir) {
        if (GhostVision.instance.isActive() && GhostVision.creeperShowHP.value()) {
            if ((Object) this instanceof Creeper) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "hasCustomName", at = @At("HEAD"), cancellable = true)
    private void makeCreeperHaveName(CallbackInfoReturnable<Boolean> cir) {
        if (GhostVision.instance.isActive() && GhostVision.creeperShowHP.value()) {
            if ((Object) this instanceof Creeper) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getCustomName", at = @At("HEAD"), cancellable = true)
    private void giveCreeperName(CallbackInfoReturnable<Component> cir) {
        if (GhostVision.instance.isActive() && GhostVision.creeperShowHP.value()) {
            if ((Object) this instanceof Creeper creeper) {
                // Only show HP if creeper is not invisible and config enabled
                float currentHealth = creeper.getHealth();
                float maxHealth = creeper.getMaxHealth();

                String currentHealthText = formatHealth(currentHealth);
                String maxHealthText = formatHealth(maxHealth);
                if (currentHealthText.equals("1.0k")) currentHealthText = "1m";
                if (maxHealthText.equals("1.0k")) maxHealthText = "1m";

                Component healthDisplay = Component.literal(currentHealthText)
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN))
                        .append(Component.literal("/").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
                        .append(Component.literal(maxHealthText).withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                        .append(Component.literal("❤").withStyle(style -> style.withColor(ChatFormatting.RED)));

                cir.setReturnValue(healthDisplay);
            }
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (!GlowPlayer.instance.isActive()) return;
        Entity self = (Entity) (Object) this;
        if (self instanceof AbstractClientPlayer player) {
            String pure = GlowPlayer.convertToPureName(player.getName().getString());
            Integer color = GlowPlayer.getColorAsInt(pure);
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private String formatHealth(float health) {
        if (health >= 1000.0f) {
            float thousands = health / 1000.0f;
            return String.format("%.1fk", thousands);
        } else {
            return String.format("%.0f", health);
        }
    }
}
