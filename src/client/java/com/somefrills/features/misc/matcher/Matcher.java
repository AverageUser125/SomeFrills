package com.somefrills.features.misc.matcher;

import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Interface for entity matching conditions.
 * Implementations can check if an entity matches certain criteria.
 */
public interface Matcher {
    /**
     * Compile this matcher into a predicate for efficient repeated evaluation.
     *
     * @return a predicate that tests if an entity matches this matcher
     */
    Predicate<LivingEntity> compile();

    /**
     * Get the evaluation priority of this matcher.
     * Higher priority matchers are evaluated first in AND chains.
     * This enables short-circuit evaluation of expensive conditions.
     * <p>
     * Default priority is 10.
     *
     * @return priority value (higher = evaluated first)
     */
    default int getPriority() {
        return 10;
    }
}
