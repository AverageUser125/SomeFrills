package com.somefrills.features.misc.matcher;

import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

import static com.somefrills.features.misc.matcher.MatcherTypes.OR;

/**
 * Logical OR combinator for matchers.
 * Either left or right matcher can match for this to match.
 * Priority is the maximum of both sides to maintain short-circuit evaluation.
 */
public record OrMatcher(Matcher left, Matcher right) implements Matcher {

    @Override
    public Predicate<LivingEntity> compile() {
        Predicate<LivingEntity> leftPred = left.compile();
        Predicate<LivingEntity> rightPred = right.compile();
        return entity -> leftPred.test(entity) || rightPred.test(entity);
    }

    @Override
    public int getPriority() {
        return Math.max(left.getPriority(), right.getPriority());
    }

    @Override
    public String toString() {
        return "(" + left + " " + OR + " " + right + ")";
    }
}

