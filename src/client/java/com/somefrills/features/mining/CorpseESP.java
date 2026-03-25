package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.events.AreaChangeEvent;
import com.somefrills.events.EntityUpdatedEvent;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.misc.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

import java.awt.geom.Area;
import java.util.Set;

public class CorpseESP {
    public static final Feature instance = new Feature("corpseEsp");
    private static boolean inMineshaft = false;
    private static final Set<ArmorStandEntity> armorStandEntities = new ConcurrentHashSet<>();

    @EventHandler
    private static void onWorldRender(WorldRenderEvent event) {
        if(!instance.isActive() || !inMineshaft) return;

        for(var entity : armorStandEntities) {
            var helmet = entity.getEquippedStack(EquipmentSlot.HEAD);
            if(helmet.isEmpty()) continue;

            // Determine type name and color inline
            Text typeName;
            RenderColor color;
            var n = helmet.getName().getString().toLowerCase();
            if(n.contains("lapis")) {
                typeName = Text.of("Lapis");
                color = RenderColor.fromFloat(0, 0, 1, 1);
            } else if(n.contains("mineral")) {
                typeName = Text.of("Tungsten");
                color = RenderColor.fromFloat(1, 1, 1, 1);
            } else if(n.contains("yog")) {
                typeName = Text.of("Umber");
                color = RenderColor.fromFloat(181f/255, 98f/255, 34f/255, 1);
            } else if(n.contains("vanguard")) {
                typeName = Text.of("Vanguard");
                color = RenderColor.fromFloat(242f/255, 36f/255, 184f/255, 1);
            } else continue; // skip helmets that don't match

            var box = Utils.getLerpedBox(entity, event).expand(0.1);

            // Draw the type name above the corpse
            var pos = entity.getEntityPos().add(0, entity.getHeight() + 0.5, 0);
            event.drawText(pos, typeName, 0.8f, true, color);

            // Draw ESP box
            event.drawFilled(box, true, color);
        }
    }

    @EventHandler
    private static void onScoreboardUpdate(AreaChangeEvent event) {
        String area = event.area.toLowerCase();
        inMineshaft = area.contains("glacite") && area.contains("mineshaft");
        if(!inMineshaft) {
            armorStandEntities.clear();
        }
    }

    @EventHandler
    private static void onEntity(EntityUpdatedEvent event) {
        if(!inMineshaft) return;
        if(event.entity instanceof ArmorStandEntity stand) {
            var helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
            if(!helmet.isEmpty()) {
                armorStandEntities.add(stand);
            }
        }
    }
}
