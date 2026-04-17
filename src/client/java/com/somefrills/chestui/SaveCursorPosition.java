package com.somefrills.chestui;

import kotlin.Pair;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;

import static com.somefrills.Main.mc;

public final class SaveCursorPosition {

    private SaveCursorPosition() {}

    public static boolean active() {
        return mc.currentScreen instanceof ChestUI;
    }

    private static Pair<Double, Double> savedPositionedP1;
    private static SavedPosition savedPosition;

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

    public static void saveCursorOriginal(double x, double y) {
        if (!active()) return;

        savedPositionedP1 = new Pair<>(x, y);
    }

    public static void saveCursorMiddle(double middleX, double middleY) {
        if (!active()) return;
        if (savedPositionedP1 == null) return;

        savedPosition = new SavedPosition(
                new Pair<>(middleX, middleY),
                savedPositionedP1,
                System.currentTimeMillis()
        );
    }

    public static Pair<Double, Double> loadCursor(double middleX, double middleY) {
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