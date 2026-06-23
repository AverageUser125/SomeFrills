package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.somefrills.features.mining.GhostVision;
import com.somefrills.features.misc.Freecam;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.SkyblockData;
import com.somefrills.mixininterface.EntityRendering;
import com.somefrills.utils.NumberUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.somefrills.Main.mc;

@Mixin(value = Entity.class, priority = 500)
public class EntityMixin implements EntityRendering {
    @Unique
    private boolean glowRender = false;
    @Unique
    private RenderColor glowColor = null;

    @Override
    public void somefrills$setGlowingColored(boolean glowing, RenderColor color) {
        glowRender = glowing;
        glowColor = color;
    }

    @Override
    public boolean somefrills$getGlowing() {
        return glowRender;
    }

    @ModifyReturnValue(method = "isCurrentlyGlowing", at = @At("RETURN"))
    private boolean isGlowing(boolean original) {
        if (glowRender) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int getTeamColorValue(int original) {
        if (glowRender) {
            return glowColor.hex;
        }
        return original;
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(Player player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) info.setReturnValue(false);
    }

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean makeCreeperVisible(boolean original) {
        // Make invisible creepers fully visible (client-side) if config enabled
        if ((Object) this instanceof Creeper) {
            var cfg = GhostVision.getConfig();

            // Make all creepers visible if config enabled
            if (cfg.makeAllCreepersVisible) {
                return false;
            }

            // Make ghost creepers visible if config enabled
            if (cfg.enabled.get() && cfg.makeCreepersVisible && SkyblockData.getLocation().contains("MIST")) {
                return false;
            }
        }

        return original;
    }

    @Inject(method = "isCustomNameVisible", at = @At("HEAD"), cancellable = true)
    private void makeCreeperNameVisible(CallbackInfoReturnable<Boolean> cir) {
        var cfg = GhostVision.getConfig();
        if (!cfg.enabled.get() && !cfg.creeperShowHP) {
            return;
        }
        if ((Object) this instanceof Creeper) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasCustomName", at = @At("HEAD"), cancellable = true)
    private void makeCreeperHaveName(CallbackInfoReturnable<Boolean> cir) {
        var cfg = GhostVision.getConfig();
        if (!cfg.enabled.get() && !cfg.creeperShowHP) {
            return;
        }
        if ((Object) this instanceof Creeper) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getCustomName", at = @At("HEAD"), cancellable = true)
    private void giveCreeperName(CallbackInfoReturnable<Component> cir) {
        var cfg = GhostVision.getConfig();
        if (!cfg.enabled.get() || !cfg.creeperShowHP) {
            return;
        }
        if ((Object) this instanceof Creeper creeper) {
            // Only show HP if creeper is not invisible and config enabled
            int currentHealth = (int) creeper.getHealth();
            int maxHealth = (int) creeper.getMaxHealth();

            String currentHealthText = NumberUtils.formatCompact(currentHealth);
            String maxHealthText = NumberUtils.formatCompact(maxHealth);
            // FIXME: this is a hack, since it should be 1m but hypixel says 1024 for some reason.
            // Skyhanni, EntityCompact has an if(entityHealth == 1024f), so maybe this is intentional?
            if (currentHealthText.equals("1.0k")) {
                currentHealthText = "1.0m";
            }
            if (maxHealthText.equals("1.0k")) {
                maxHealthText = "1.0m";
            }

            Component healthDisplay = Component.literal(currentHealthText)
                    .styled(style -> style.withColor(ChatFormatting.GREEN))
                    .append(Component.literal("/").styled(style -> style.withColor(ChatFormatting.WHITE)))
                    .append(Component.literal(maxHealthText).styled(style -> style.withColor(ChatFormatting.GREEN)))
                    .append(Component.literal("❤").styled(style -> style.withColor(ChatFormatting.RED)));

            cir.setReturnValue(healthDisplay);
        }
    }


    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object) this != mc.player) return;

        var freecam = Freecam.INSTANCE;
        if (freecam.isActive()) {
            freecam.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        }
    }
}
