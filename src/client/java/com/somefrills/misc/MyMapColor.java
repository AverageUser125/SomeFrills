package com.somefrills.misc;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

public enum MyMapColor {
    WHITE(16777215, Items.WHITE_DYE),
    ORANGE(14188339, Items.ORANGE_DYE),
    MAGENTA(11685080, Items.MAGENTA_DYE),
    LIGHT_BLUE(6724056, Items.LIGHT_BLUE_DYE),
    YELLOW(15066419, Items.YELLOW_DYE),
    LIME(8375321, Items.LIME_DYE),
    PINK(15892389, Items.PINK_DYE),
    GRAY(5000268, Items.GRAY_DYE),
    LIGHT_GRAY(10066329, Items.LIGHT_GRAY_DYE),
    CYAN(5013401, Items.CYAN_DYE),
    PURPLE(8339378, Items.PURPLE_DYE),
    BLUE(3361970, Items.BLUE_DYE),
    BROWN(6704179, Items.BROWN_DYE),
    GREEN(6717235, Items.GREEN_DYE),
    RED(10040115, Items.RED_DYE),
    BLACK(1644825, Items.BLACK_DYE),
    PALE_YELLOW(16247203, Items.HAY_BLOCK),
    BRIGHT_RED(16711680, Items.RED_WOOL),
    DIRT_BROWN(9923917, Items.DIRT),
    STONE_GRAY(7368816, Items.STONE),
    OAK_TAN(9402184, Items.OAK_LOG),
    GOLD(16445005, Items.GOLD_INGOT),
    DIAMOND_BLUE(6085589, Items.DIAMOND),
    EMERALD_GREEN(55610, Items.EMERALD),
    SPRUCE_BROWN(8476209, Items.SPRUCE_LOG),
    DARK_RED(7340544, Items.NETHER_WART_BLOCK),
    DARK_AQUA(3837580, Items.PRISMARINE),
    LICHEN_GREEN(8365974, Items.GLOW_LICHEN);

    private final int color;
    private final Item item;

    MyMapColor(int color, Item item) {
        this.color = color;
        this.item = item;
    }

    public static MyMapColor getClosest(RenderColor color) {
        MyMapColor bestMatch = null;
        float bestDistance = Float.MAX_VALUE;

        for (MyMapColor dyeColor : MyMapColor.values()) {
            int fmtColor = dyeColor.getColor();
            RenderColor formatColor = RenderColor.fromHex(fmtColor);

            float distance = color.distance(formatColor);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestMatch = dyeColor;
            }
        }

        return bestMatch;
    }

    public int getColor() {
        return color;
    }

    public int getHex() {
        return color;
    }

    public Item getItem() {
        return item;
    }
}