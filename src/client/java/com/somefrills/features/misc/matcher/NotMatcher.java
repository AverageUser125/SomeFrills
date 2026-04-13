package com.somefrills.features.misc.matcher;

import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Logical NOT matcher - inverts the result of another matcher using != operator.
 * Example: TYPE!=zombie (matches everything except zombies)
 */
public record NotMatcher(Matcher inner) implements Matcher {

    @Override
    public Predicate<LivingEntity> compile() {
        Predicate<LivingEntity> innerPred = inner.compile();
        return entity -> !innerPred.test(entity);
    }

    @Override
    public int getPriority() {
        return inner.getPriority();
    }

    @Override
    public String toString() {
        return "!" + inner;
    }
}

