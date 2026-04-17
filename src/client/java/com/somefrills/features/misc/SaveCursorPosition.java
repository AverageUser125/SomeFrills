package com.somefrills.features.misc;

import com.somefrills.chestui.ChestUI;
import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.misc.MiscCategory.SaveCursorPositionConfig;
import com.somefrills.events.ScreenCloseEvent;
import com.somefrills.events.ScreenOpenEvent;
import kotlin.Pair;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;

import static com.somefrills.Main.mc;

public final class SaveCursorPosition extends Feature {
    private SaveCursorPositionConfig config;

    private SaveCursorPosition() {
        super(FrillsConfig.instance.misc.saveCursorPosition.enabled);
        config = FrillsConfig.instance.misc.saveCursorPosition;
    }

    public boolean active() {
        if(config != null && config.onlyChestUI) {
            return mc.currentScreen instanceof ChestUI;
        }
        return true;
    }

    private Pair<Double, Double> savedPositionedP1;
    private SavedPosition savedPosition;

    public static final class SavedPosition {
        public final Pair<Double, Double> middle;
        public final Pair<Double, Double> cursor;
        public final long savedAt;

        public SavedPosition(Pair<Double, Double> middle,
                             Pair<Double, Double> cursor,
                             long savedAt) {
            this.middle = middle;
            this.cursor = cursor;
            this.savedAt = savedAt;
        }
    }

    public void saveCursorOriginal(double x, double y) {
        if (!active()) return;

        savedPositionedP1 = new Pair<>(x, y);
    }

    public void saveCursorMiddle(double middleX, double middleY) {
        if (!active()) return;
        if (savedPositionedP1 == null) return;

        savedPosition = new SavedPosition(
                new Pair<>(middleX, middleY),
                savedPositionedP1,
                System.currentTimeMillis()
        );
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        loadCursor(mc.mouse.getX(), mc.mouse.getY());
    }

    @EventHandler
    public void onScreen(ScreenCloseEvent event) {
        savedPosition = null;
    }

    public Pair<Double, Double> loadCursor(double middleX, double middleY) {
        if (!active()) return null;
        if (savedPosition == null) return null;

        long now = System.currentTimeMillis();

        if (now - savedPosition.savedAt > 5000) {
            savedPosition = null;
            return null;
        }

        if (Math.abs(savedPosition.middle.getFirst() - middleX) < 1
                && Math.abs(savedPosition.middle.getSecond() - middleY) < 1) {

            MinecraftClient client = MinecraftClient.getInstance();

            InputUtil.setCursorParameters(
                    client.getWindow(),
                    InputUtil.GLFW_CURSOR_NORMAL,
                    savedPosition.cursor.getFirst(),
                    savedPosition.cursor.getSecond()
            );

            return savedPosition.cursor;
        }

        return null;
    }
}