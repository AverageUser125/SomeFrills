package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.events.AreaChangeEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import static com.somefrills.Main.mc;

public class CorpseESP {
    public static final Feature instance = new Feature("corpseEsp");
    private static boolean inMineshaft = false;

    @EventHandler
    private static void onWorldRender(WorldRenderEvent event) {
        if (!inMineshaft) return;
        if (mc.level == null) return;

        for (var entity : mc.level.entitiesForRendering()) {
            // if (!(entity instanceof LivingEntity ArmorStandEntity)) continue;
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            var helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.isEmpty()) continue;

            // Determine type name + color
            Component typeName;
            RenderColor color;

            var n = helmet.getHoverName().getString().toLowerCase();

            if (n.contains("lapis")) {
                typeName = Component.nullToEmpty("Lapis");
                color = RenderColor.fromFloat(0, 0, 1, 1);
            } else if (n.contains("mineral")) {
                typeName = Component.nullToEmpty("Tungsten");
                color = RenderColor.fromFloat(1, 1, 1, 1);
            } else if (n.contains("yog")) {
                typeName = Component.nullToEmpty("Umber");
                color = RenderColor.fromFloat(181f / 255, 98f / 255, 34f / 255, 1);
            } else if (n.contains("vanguard")) {
                typeName = Component.nullToEmpty("Vanguard");
                color = RenderColor.fromFloat(242f / 255, 36f / 255, 184f / 255, 1);
            } else {
                typeName = Component.nullToEmpty("Unknown");
                color = RenderColor.fromFloat(1, 0, 1, 1);
            }

            var box = Utils.getLerpedBox(entity, event).inflate(0.1);

            var pos = entity.position().add(0, entity.getBbHeight() + 0.5, 0);
            event.drawText(pos, typeName, 0.8f, true, color);
            event.drawFilled(box, true, color);
        }
    }

    @EventHandler
    private static void onScoreboardUpdate(AreaChangeEvent event) {
        String area = event.area.toLowerCase();
        inMineshaft = area.contains("mineshaft");
    }
}