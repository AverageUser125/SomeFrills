package com.example.mixin;

import com.example.utils.AllConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class CreeperEntityRenderStateMixin {

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperVisible(CallbackInfoReturnable<Boolean> cir) {
        // Make invisible creepers fully visible (client-side) if config enabled
        if (AllConfig.creeperNotInvisible && (Object) this instanceof CreeperEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isCustomNameVisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperNameVisible(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof CreeperEntity creeper) {
            // Only show name if creeper is not invisible and config enabled
            if (AllConfig.creeperShowHP && !creeper.isInvisible()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "hasCustomName", at = @At("HEAD"), cancellable = true)
    private void makeCreeperHaveName(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof CreeperEntity creeper) {
            // Only show custom name if creeper is not invisible and config enabled
            if (AllConfig.creeperShowHP && !creeper.isInvisible()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getCustomName", at = @At("HEAD"), cancellable = true)
    private void giveCreeperName(CallbackInfoReturnable<Text> cir) {
        if ((Object) this instanceof CreeperEntity creeper) {
            // Only show HP if creeper is not invisible and config enabled
            if (AllConfig.creeperShowHP && !creeper.isInvisible()) {
                float currentHealth = creeper.getHealth();
                float maxHealth = creeper.getMaxHealth();

                String currentHealthText = formatHealth(currentHealth);
                String maxHealthText = formatHealth(maxHealth);
                if (currentHealthText.equals("1.0k")) currentHealthText = "1m";
                if (maxHealthText.equals("1.0k")) maxHealthText = "1m";

                Text healthDisplay = Text.literal(currentHealthText)
                        .styled(style -> style.withColor(Formatting.GREEN))
                        .append(Text.literal("/").styled(style -> style.withColor(Formatting.WHITE)))
                        .append(Text.literal(maxHealthText).styled(style -> style.withColor(Formatting.GREEN)))
                        .append(Text.literal("❤").styled(style -> style.withColor(Formatting.RED)));

                cir.setReturnValue(healthDisplay);
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

