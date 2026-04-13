package com.somefrills.features.misc.matcher;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

import static com.somefrills.features.misc.matcher.MatcherTypes.NAKED;

/**
 * Matches entities that have no armor equipped.
 * Shorthand for: HELMET=air AND CHEST=air AND LEGS=air AND FEET=air
 * Example: NAKED
 * Priority: 10 (default)
 */
public class NakedMatcher implements Matcher {

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty() &&
                entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty() &&
                entity.getEquippedStack(EquipmentSlot.LEGS).isEmpty() &&
                entity.getEquippedStack(EquipmentSlot.FEET).isEmpty();
    }

    @Override
    public String toString() {
        return NAKED;
    }
}



