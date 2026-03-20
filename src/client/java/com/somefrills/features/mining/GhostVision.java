package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingColor;
import com.somefrills.config.SettingEnum;
import com.somefrills.config.SettingDescription;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.EntityCache;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.RenderStyle;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Box;

public class GhostVision {
    public static final Feature instance = new Feature("ghostVision");

    @SettingDescription("Rendering style for ghost boxes (fill/outline/both)")
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Both, RenderStyle.class);
    @SettingDescription("Fill color for ghost boxes")
    public static final SettingColor fill = new SettingColor(RenderColor.fromHex(0x00c8c8, 0.5f));
    @SettingDescription("Outline color for ghost boxes")
    public static final SettingColor outline = new SettingColor(RenderColor.fromHex(0x00c8c8, 1.0f));
    @SettingDescription("Remove creeper 'charged' visuals")
    public static final SettingBool removeCharge = new SettingBool(true);
    @SettingDescription("Make creepers visible in Dwarven Mines")
    public static final SettingBool makeCreepersVisible = new SettingBool(true);
    @SettingDescription("Show creeper HP in overlay")
    public static final SettingBool creeperShowHP = new SettingBool(true);

    private static final EntityCache cache = new EntityCache();

    public static boolean isGhost(CreeperEntity entity) {
        return instance.isActive() && cache.has(entity);
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity instanceof CreeperEntity creeper && Utils.isInArea("Dwarven Mines")) {
            if (creeper.getEntity().getY() < 100) {
                cache.add(event.entity);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInArea("Dwarven Mines")) {
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                Box box = Utils.getLerpedBox(ent, event.tickCounter.getTickProgress(true));
                event.drawStyled(box, style.value(), false, outline.value(), fill.value());
            }
        }
    }
}
