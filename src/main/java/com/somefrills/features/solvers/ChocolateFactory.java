package com.somefrills.features.solvers;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.solvers.SolverCategory.ChocolateFactoryConfig;
import com.somefrills.events.ScreenRenderEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

// descriptions moved into constructors

/**
 * Fabric 1.21.10 port of the Kotlin ChocolateFactory module.
 * Automatically claims stray rabbits in the Chocolate Factory menu.
 */
public class ChocolateFactory extends Feature {
    private static final String CHOCOLATE_FACTORY_TITLE = "Chocolate Factory";
    private final ChocolateFactoryConfig config;
    private long lastClaimTime = 0;

    public ChocolateFactory() {
        super(FrillsConfig.instance.solvers.chocolateFactorySolver.enabled);
        config = FrillsConfig.instance.solvers.chocolateFactorySolver;
    }

    @EventHandler
    public void onHudTick(ScreenRenderEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        // Check if auto claim is enabled
        if (!config.claimStray) return;

        ClientPlayerEntity player = client.player;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) return;

        String title = "";
        if (client.currentScreen != null) {
            Text txt = client.currentScreen.getTitle();
            if (txt != null) title = txt.getString();
        }

        // Check if we're in the Chocolate Factory menu
        if (!title.equals(CHOCOLATE_FACTORY_TITLE)) return;


        // Scan for claimable items
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty()) continue;

            String displayName = stack.getName().getString();

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("CLICK ME!")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < config.claimDelay) return;
                Utils.clickSlot(slot.getIndex());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }

            // Check if this is a claimable item (CLICK ME! or Golden Rabbit)
            if (displayName.contains("Golden Rabbit")) {
                // Check click delay
                if (System.currentTimeMillis() - lastClaimTime < config.claimDelay * 100L) return;
                Utils.clickSlot(slot.getIndex());
                lastClaimTime = System.currentTimeMillis();
                return; // Only click one per tick
            }
        }
    }

}