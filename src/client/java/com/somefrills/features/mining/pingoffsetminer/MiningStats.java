package com.somefrills.features.mining.pingoffsetminer;


import com.somefrills.misc.Utils;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MiningStats {
    private ItemStack item = ItemStack.EMPTY;
    private double speed = -1;
    private boolean boost = false;
    private int cooldown = -1;

    private boolean isTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        List<String> tooltip = Utils.getToolTip(stack);
        for (String text : tooltip.reversed()) {
            if (text.contains(" DRILL ") || text.contains(" GAUNTLET ") || text.contains(" PICKAXE ")) {
                return true;
            }
        }
        return false;
    }

    private int getCooldown(ItemStack stack) {
        List<String> tooltip = Utils.getToolTip(stack);
        boolean found = false;
        for (String line : tooltip) {
            String text = Utils.toPlain(line);
            if (!found && text.contains("Ability: Mining Speed")) {
                found = true;
                continue;
            }
            if (found) {
                try {
                    String[] parts = text.split("\\s+");
                    for (String part : parts) {
                        if (part.matches("[0-9.]+s")) {
                            return (int) Double.parseDouble(part.replace("s", ""));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return -1;
    }

    public void setItem(ItemStack newItem) {
        if (isTool(newItem)) {
            this.item = newItem;
            this.cooldown = getCooldown(newItem);
        } else {
            this.item = ItemStack.EMPTY;
            this.cooldown = -1;
        }
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setBoost(boolean boost) {
        this.boost = boost;
    }

    public void reset() {
        this.item = ItemStack.EMPTY;
        this.speed = -1;
        this.boost = false;
        this.cooldown = -1;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean getBoost() {
        return boost;
    }

    public int getCooldown() {
        return cooldown;
    }
}