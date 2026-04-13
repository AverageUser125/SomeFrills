package com.somefrills.features.misc.matcher;

import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

import static com.somefrills.features.misc.matcher.MatcherTypes.TYPE;

/**
 * Matches entities by their type/entity class name.
 * Example: TYPE=zombie
 * Priority: 10 (default)
 */
public class TypeMatcher implements Matcher {
    private final String entityType;

    public TypeMatcher(String entityType) {
        this.entityType = Utils.stripPrefix(entityType, "minecraft:").toLowerCase();
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> {
            String entityTypeStr = entity.getType().toString();
            entityTypeStr = Utils.stripPrefix(entityTypeStr, "entity.minecraft.");
            return entityTypeStr.equals(this.entityType);
        };
    }

    @Override
    public String toString() {
        return TYPE + "=" + entityType;
    }
}

