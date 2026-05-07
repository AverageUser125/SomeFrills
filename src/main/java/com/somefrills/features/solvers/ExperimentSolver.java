package com.somefrills.features.solvers;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.solvers.SolverCategory.ExperimentSolverConfig;
import com.somefrills.events.MouseClickEvent;
import com.somefrills.events.ReceivePacketEvent;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.events.TickEventPost;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

import static com.somefrills.Main.mc;

public class ExperimentSolver extends Feature {
    private final ExperimentSolverConfig config;
    private long lastClick = 0;

    private final Random rand = new Random();

    private ExperimentState current = null;

    public ExperimentSolver() {
        super(FrillsConfig.instance.solvers.experimentSolver.enabled);
        this.config = FrillsConfig.instance.solvers.experimentSolver;
    }

    @EventHandler
    private void onScreenOpen(ScreenOpenEvent event) {
        if (!Utils.isOnPrivateIsland()) return;
        if (!(event.screen instanceof GenericContainerScreen screen)) return;
        String title = screen.getTitle().getString();

        if (title.startsWith("Chronomatron (")) {
            current = new ChronomatronState();
            current.reset();
        } else if (title.startsWith("Ultrasequencer (")) {
            current = new UltrasequencerState();
            current.reset();
        } else {
            current = null;
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (current != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPacketReceive(ReceivePacketEvent event) {
        if (current == null) return;
        if (!(event.packet instanceof ClientCommandC2SPacket)) return;
        if (mc.player == null) return;
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;
        // let the current state observe the screen/handler to update its internal state
        current.onObserve(handler);
    }

    @EventHandler
    private void onTick(TickEventPost event) {
        if (mc.player == null) return;
        if (current == null) return;
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < delay()) return;

        Integer next = current.next();
        if (next != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.interactionManager != null && client.player != null) {
                client.interactionManager.clickSlot(handler.syncId, next, 0, SlotActionType.PICKUP, client.player);
                lastClick = now;
            }
        }

        if (current.shouldClose()) {
            if (mc.player != null) mc.player.closeHandledScreen();
            current = null;
        }
    }

    private long delay() {
        int a = Math.min(config.minDelay, config.maxDelay);
        int b = Math.max(config.minDelay, config.maxDelay);
        return (a + rand.nextInt(b - a + 1));
    }

    private interface ExperimentState {
        void reset();

        void onObserve(ScreenHandler handler);

        Integer next();

        boolean shouldClose();
    }

    private class ChronomatronState implements ExperimentState {
        private final List<Integer> order = new ArrayList<>();
        private int last = -1;
        private boolean ready = false;
        private int clicks = 0;
        private boolean bool = false; // matches Kotlin naming

        @Override
        public void reset() {
            order.clear();
            last = -1;
            ready = false;
            clicks = 0;
            bool = false;
        }

        @Override
        public void onObserve(ScreenHandler handler) {
            if (handler == null || handler.slots == null) return;
            if (handler.slots.size() <= 49) return;

            ItemStack center = handler.slots.get(49).getStack();

            if (last != -1) {
                ItemStack lastStack = handler.slots.get(last).getStack();
                if (center != null && center.getItem() == Items.GLOWSTONE && (lastStack == null || !lastStack.hasGlint())) {
                    // determine close threshold based on config if desired; use config.chronomatron.closeThreshold
                    int threshold = config.chronomatron.closeThreshold;
                    // emulate Kotlin behavior: set bool based on threshold logic; keep simple: compare size
                    bool = order.size() > threshold;
                    ready = false;
                    return;
                }
            }

            if (ready) return;
            if (center == null || center.getItem() != Items.CLOCK) return;

            // find first glinted slot in 10..43
            for (int i = 10; i <= 43 && i < handler.slots.size(); i++) {
                Slot s = handler.slots.get(i);
                ItemStack stack = s.getStack();
                if (stack == null || stack.isEmpty()) continue;
                if (!stack.hasGlint() && (stack.getEnchantments() == null || stack.getEnchantments().isEmpty()))
                    continue;
                order.add(i);
                last = i;
                ready = true;
                clicks = 0;
                break;
            }
        }

        @Override
        public Integer next() {
            if (!ready) return null;
            if (clicks < order.size()) {
                return order.get(clicks++);
            }
            return null;
        }

        @Override
        public boolean shouldClose() {
            return config.chronomatron.shouldClose && bool && clicks >= order.size();
        }
    }

    private class UltrasequencerState implements ExperimentState {
        private final Map<Integer, Integer> order = new HashMap<>();
        private boolean ready = false;
        private int clicks = 0;

        @Override
        public void reset() {
            order.clear();
            ready = false;
            clicks = 0;
        }

        @Override
        public void onObserve(ScreenHandler handler) {
            if (handler == null || handler.slots == null) return;
            if (handler.slots.size() <= 49) return;

            ItemStack center = handler.slots.get(49).getStack();
            if (center != null && center.getItem() == Items.CLOCK) {
                ready = false;
                return;
            }

            if (ready) return;
            if (center == null || center.getItem() != Items.GLOWSTONE) return;

            order.clear();
            for (int i = 9; i <= 44 && i < handler.slots.size(); i++) {
                Slot s = handler.slots.get(i);
                ItemStack stack = s.getStack();
                if (stack == null || stack.isEmpty()) continue;
                String name = stack.getName().getString();
                String stripped = name.replaceAll("§.", "");
                if (stripped.matches("\\d+")) {
                    int idx = stack.getCount() - 1;
                    order.put(idx, i);
                }
            }
            ready = true;
            clicks = 0;
        }

        @Override
        public Integer next() {
            if (!ready) return null;
            Integer slot = order.get(clicks);
            clicks++;
            return slot;
        }

        @Override
        public boolean shouldClose() {
            int threshold = config.ultrasequencer.closeThreshold;
            return config.ultrasequencer.shouldClose && order.size() > threshold;
        }
    }
}


