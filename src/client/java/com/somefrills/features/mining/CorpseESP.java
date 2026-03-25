package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.events.*;
import com.somefrills.misc.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;

import static com.somefrills.Main.mc;

public class CorpseESP {
    public static final Feature instance = new Feature("corpseEsp");
    private static boolean inMineshaft = false;

    @EventHandler
    private static void onWorldRender(WorldRenderEvent event) {
        if (!inMineshaft) return;
        if (mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity stand)) continue;

            var helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
            if (helmet.isEmpty()) continue;

            // Determine type name + color
            Text typeName;
            RenderColor color;

            var n = helmet.getName().getString().toLowerCase();

            if (n.contains("lapis")) {
                typeName = Text.of("Lapis");
                color = RenderColor.fromFloat(0, 0, 1, 1);
            } else if (n.contains("mineral")) {
                typeName = Text.of("Tungsten");
                color = RenderColor.fromFloat(1, 1, 1, 1);
            } else if (n.contains("yog")) {
                typeName = Text.of("Umber");
                color = RenderColor.fromFloat(181f/255, 98f/255, 34f/255, 1);
            } else if (n.contains("vanguard")) {
                typeName = Text.of("Vanguard");
                color = RenderColor.fromFloat(242f/255, 36f/255, 184f/255, 1);
            } else {
                typeName = Text.of("Unknown");
                color = RenderColor.fromFloat(1, 0, 1, 1);
            }

            var box = Utils.getLerpedBox(stand, event).expand(0.1);

            var pos = stand.getEntityPos().add(0, stand.getHeight() + 0.5, 0);
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