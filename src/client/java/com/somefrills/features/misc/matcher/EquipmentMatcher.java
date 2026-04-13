package com.somefrills.features.misc.matcher;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.function.Predicate;

/**
 * Matches entities that have a specific item (or empty slot) in an equipment slot.
 * Example: HELMET=diamond_helmet or HELMET=air (empty slot)
 * Priority: 10 (default)
 */
public class EquipmentMatcher implements Matcher {
    private final EquipmentSlot slot;
    private final String itemId;  // null means check for empty slot
    private final boolean checkEmpty;  // true if checking for empty slot

    public EquipmentMatcher(String slotName, String itemId) {
        this.slot = MatcherTypes.parseEquipmentSlot(slotName);
        if (this.slot == null) {
            throw new IllegalArgumentException("Unknown equipment slot: " + slotName);
        }

        // Check if looking for empty or any slot
        if (MatcherTypes.isEmptySlot(itemId)) {
            this.checkEmpty = true;
            this.itemId = null;
        } else if (MatcherTypes.isAnyItem(itemId)) {
            // "any" means check for non-empty (opposite of none)
            this.checkEmpty = false;
            this.itemId = MatcherTypes.ANY_ITEM_MARKER;
        } else {
            this.checkEmpty = false;
            this.itemId = MatcherTypes.normalizeItemId(itemId);
        }
    }

    /**
     * Get all valid canonical equipment slot names for suggestions/validation.
     *
     * @return array of valid canonical equipment slot names
     */
    public static String[] getEquipmentSlotNames() {
        return MatcherTypes.getEquipmentSlots();
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> {
            ItemStack stack = entity.getEquippedStack(slot);

            if (checkEmpty) {
                return stack.isEmpty();
            }

            if (MatcherTypes.ANY_ITEM_MARKER.equals(itemId)) {
                return !stack.isEmpty();
            }

            if (stack.isEmpty()) {
                return false;
            }

            String stackId = Registries.ITEM.getId(stack.getItem()).toString();
            stackId = MatcherTypes.normalizeItemId(stackId);
            return stackId.equals(this.itemId);
        };
    }

    @Override
    public int getPriority() {
        return 10; // Uses default priority
    }

    @Override
    public String toString() {
        if (checkEmpty) {
            return slot.getName().toUpperCase() + "=air";
        }
        return slot.getName().toUpperCase() + "=" + itemId;
    }
}


