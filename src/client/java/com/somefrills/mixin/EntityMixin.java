package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.config.FrillsConfig;
import com.somefrills.features.mining.GhostVision;
import com.somefrills.misc.EntityRendering;
import com.somefrills.misc.RenderColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityRendering {
    @Unique
    private boolean glowRender = false;
    @Unique
    private RenderColor glowColor;

    @Override
    public void somefrills_mod$setGlowingColored(boolean glowing, RenderColor color) {
        glowRender = glowing;
        glowColor = color;
    }

    @Override
    public boolean somefrills_mod$getGlowing() {
        return glowRender;
    }

    @ModifyReturnValue(method = "isGlowing", at = @At("RETURN"))
    private boolean isGlowing(boolean original) {
        if (glowRender) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method = "getTeamColorValue", at = @At("RETURN"))
    private int getTeamColorValue(int original) {
        if (glowRender) {
            return glowColor.hex;
        }
        return original;
    }

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperVisible(CallbackInfoReturnable<Boolean> cir) {
        // Make invisible creepers fully visible (client-side) if config enabled
        var cfg = FrillsConfig.instance.mining.ghostVision;
        if (cfg.enabled.get() && cfg.makeCreepersVisible) {
            if ((Object) this instanceof CreeperEntity creeper) {
                if (GhostVision.isGhost(creeper) || FrillsConfig.instance.mining.ghostVision.makeAllCreepersVisible) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "isCustomNameVisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperNameVisible(CallbackInfoReturnable<Boolean> cir) {
        var cfg = FrillsConfig.instance.mining.ghostVision;
        if (cfg.enabled.get() && cfg.creeperShowHP) {
            if ((Object) this instanceof CreeperEntity) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "hasCustomName", at = @At("HEAD"), cancellable = true)
    private void makeCreeperHaveName(CallbackInfoReturnable<Boolean> cir) {
        var cfg = FrillsConfig.instance.mining.ghostVision;
        if (cfg.enabled.get() && cfg.creeperShowHP) {
            if ((Object) this instanceof CreeperEntity) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getCustomName", at = @At("HEAD"), cancellable = true)
    private void giveCreeperName(CallbackInfoReturnable<Text> cir) {
        var cfg = FrillsConfig.instance.mining.ghostVision;
        if (cfg.enabled.get() && cfg.creeperShowHP) {
            if ((Object) this instanceof CreeperEntity creeper) {
                // Only show HP if creeper is not invisible and config enabled
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


    @Unique
    private String formatHealth(float health) {
        if (health >= 1000.0f) {
            float thousands = health / 1000.0f;
            return String.format("%.1fk", thousands);
        } else {
            return String.format("%.0f", health);
        }
    }
}
