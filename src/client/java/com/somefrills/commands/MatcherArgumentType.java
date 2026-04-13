package com.somefrills.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.misc.matcher.Matcher;
import com.somefrills.features.misc.matcher.MatcherParser;
import com.somefrills.features.misc.matcher.MatcherTypes;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.somefrills.features.misc.matcher.MatcherTypes.*;

/**
 * Custom ArgumentType for parsing Matcher expressions.
 * Parses expressions like: "TYPE=zombie", "NAME=Littlefoot", "HELMET=diamond_helmet", etc.
 * Supports complex expressions: "TYPE=zombie AND HELMET=diamond_helmet OR NAME=Boss"
 */
public class MatcherArgumentType implements ArgumentType<Matcher> {

    public static MatcherArgumentType matcher() {
        return new MatcherArgumentType();
    }

    public static Matcher getMatcher(CommandContext<FabricClientCommandSource> ctx, String name) {
        return ctx.getArgument(name, Matcher.class);
    }

    @Override
    public Matcher parse(StringReader reader) throws CommandSyntaxException {
        // Greedily read the entire remaining string as the expression
        String expression = readExpression(reader);

        if (expression.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader, "Expected matcher expression");
        }

        try {
            return MatcherParser.parse(expression);
        } catch (MatcherParser.MatcherParseException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                    .createWithContext(reader, e.getMessage());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();

        // If empty or only whitespace, suggest all matcher types and operators
        if (remaining.trim().isEmpty()) {
            builder.suggest(TYPE);
            builder.suggest(NAME);
            for (String slot : getEquipmentSlots()) {
                builder.suggest(slot);
            }
            builder.suggest(NAKED);
            builder.suggest("(");
            return builder.buildFuture();
        }

        // Check if remaining has trailing whitespace (indicating a complete word)
        boolean hasTrailingSpace = !remaining.isEmpty() && Character.isWhitespace(remaining.charAt(remaining.length() - 1));

        String[] words = remaining.trim().split("\\s+");
        String lastWord = words[words.length - 1];
        String previousWord = words.length >= 2 ? words[words.length - 2] : null;

        // Build prefix (everything before the last word)
        String prefix = "";
        if (words.length > 1) {
            prefix = String.join(" ", java.util.Arrays.copyOfRange(words, 0, words.length - 1)) + " ";
        }

        // Build matchers array (TYPE, NAME, HELMET, etc.)
        String[] allMatchers = getAllMatchers();

        // Step 1: If there's trailing space, treat lastWord as if it's complete
        if (hasTrailingSpace) {
            // If lastWord is AND or OR, only suggest matcher types
            if (isLogicalOperator(lastWord)) {
                for (String matcher : allMatchers) {
                    builder.suggest(prefix + lastWord + " " + matcher);
                }
                builder.suggest(prefix + lastWord + " " + NAKED);
                builder.suggest(prefix + lastWord + " (");
            } else {
                // After a complete matcher value, only suggest AND/OR
                builder.suggest(prefix + lastWord + " " + AND);
                builder.suggest(prefix + lastWord + " " + OR);
            }
            return builder.buildFuture();
        }

        // Step 2: Check if lastWord is a complete matcher type (no operator yet)
        boolean hasOperator = lastWord.contains("=");
        if (!hasOperator) {
            // If lastWord itself is a logical operator, suggest matchers
            if (isLogicalOperator(lastWord)) {
                for (String matcher : allMatchers) {
                    builder.suggest(prefix + lastWord + " " + matcher);
                }
                builder.suggest(prefix + lastWord + " " + NAKED);
                builder.suggest(prefix + lastWord + " (");
                return builder.buildFuture();
            }

            // Try to match against known matchers
            String matchedMatcher = null;
            for (String matcher : allMatchers) {
                if (matcher.equalsIgnoreCase(lastWord)) {
                    matchedMatcher = matcher;
                    break;
                }
            }
            // Also check NAKED
            if (lastWord.equalsIgnoreCase(NAKED)) {
                matchedMatcher = NAKED;
            }

            // If exact match, suggest operators
            if (matchedMatcher != null) {
                if (matchedMatcher.equals(NAKED)) {
                    return builder.buildFuture(); // NAKED doesn't take operators
                }
                builder.suggest(prefix + matchedMatcher + "=");
                builder.suggest(prefix + matchedMatcher + "!=");
                return builder.buildFuture();
            }

            // If partial match for a matcher type, only suggest the matching matchers
            boolean foundPartial = false;
            for (String matcher : allMatchers) {
                if (matcher.toLowerCase().startsWith(lastWord.toLowerCase())) {
                    builder.suggest(prefix + matcher);
                    foundPartial = true;
                }
            }
            // Also check NAKED
            if (NAKED.startsWith(lastWord.toUpperCase())) {
                builder.suggest(prefix + NAKED);
                foundPartial = true;
            }

            // If we found partials, don't suggest anything else
            if (foundPartial) {
                return builder.buildFuture();
            }

            // If lastWord doesn't match any matchers and has no operator
            // Check if previous word is AND or OR
            if (isLogicalOperator(previousWord)) {
                // After AND/OR, only suggest matcher types
                for (String matcher : allMatchers) {
                    builder.suggest(prefix + matcher);
                }
                builder.suggest(prefix + NAKED);
                builder.suggest(prefix + "(");
            } else {
                // Otherwise suggest AND/OR as logical next step
                builder.suggest(prefix + AND);
                builder.suggest(prefix + OR);
            }
            return builder.buildFuture();
        }

        // Step 3: We have an operator, now suggest values
        if (lastWord.startsWith(TYPE + "=") || lastWord.startsWith(TYPE + "!=")) {
            String typePrefix = prefix + (lastWord.startsWith(TYPE + "!=") ? TYPE + "!=" : TYPE + "=");
            String entityPrefix = lastWord.substring(lastWord.indexOf('=') + 1).toLowerCase();
            Registries.ENTITY_TYPE.forEach(entityType -> {
                String id = Registries.ENTITY_TYPE.getId(entityType).toString();
                id = Utils.stripPrefix(id, MINECRAFT_PREFIX).toLowerCase();
                if (id.startsWith(entityPrefix)) {
                    builder.suggest(typePrefix + id);
                }
            });
            return builder.buildFuture();
        }

        // If typing "NAME=" or "NAME!=", let user type freely
        if (lastWord.startsWith(NAME + "=") || lastWord.startsWith(NAME + "!=")) {
            return builder.buildFuture();
        }

        // If typing equipment slot (HELMET=, HELMET!=, CHEST=, etc.), suggest items, "air", "none", and "any"
        // Check if it's just slot with operator (e.g., "HELMET=" or "CHEST!=") with no value
        if (lastWord.contains("=")) {
            int eqIndex = lastWord.indexOf('=');
            boolean isNegated = eqIndex > 0 && lastWord.charAt(eqIndex - 1) == '!';
            int slotEndIndex = isNegated ? eqIndex - 1 : eqIndex;

            if (slotEndIndex > 0) {
                String slotName = lastWord.substring(0, slotEndIndex).toUpperCase();

                // If it's a slot matcher
                if (MatcherTypes.isEquipmentSlot(slotName)) {
                    String valueStart = eqIndex < lastWord.length() - 1 ? lastWord.substring(eqIndex + 1) : "";
                    String valuePrefix = valueStart.toLowerCase();
                    String equipPrefix = prefix + lastWord.substring(0, eqIndex + 1);

                    // Always suggest empty/any slot options first
                    for (String option : getEmptySlotOptions()) {
                        if (option.startsWith(valuePrefix)) {
                            builder.suggest(equipPrefix + option);
                        }
                    }

                    // Always suggest items (even if no prefix)
                    Registries.ITEM.forEach(item -> {
                        String id = Registries.ITEM.getId(item).toString();
                        id = Utils.stripPrefix(id, MINECRAFT_PREFIX).toLowerCase();
                        if (id.startsWith(valuePrefix)) {
                            builder.suggest(equipPrefix + id);
                        }
                    });
                    return builder.buildFuture();
                }
            }
        }

        return builder.buildFuture();
    }

    /**
     * Check if a word is a logical operator (AND or OR).
     */
    private boolean isLogicalOperator(String word) {
        return MatcherTypes.isLogicalOperator(word);
    }

    /**
     * Read matcher expression until we hit the "color" keyword.
     * When "color" is encountered, we stop parsing and leave the cursor positioned
     * so that Brigadier can then parse the "color" literal that comes next.
     */
    private String readExpression(StringReader reader) {
        StringBuilder sb = new StringBuilder();

        while (reader.canRead()) {
            if (reader.peek() == ' ') {
                int checkPos = reader.getCursor();
                reader.skip(); // skip the space temporarily

                // Peek at the next word
                String word = readWord(reader);

                if (word.equalsIgnoreCase("color")) {
                    // Found "color" - restore cursor to before the space and stop
                    reader.setCursor(checkPos);
                    return sb.toString().trim();
                } else {
                    // Not "color", rewind and include the space in our expression
                    reader.setCursor(checkPos);
                    sb.append(reader.read());
                }
            } else {
                sb.append(reader.read());
            }
        }

        return sb.toString().trim();
    }

    /**
     * Read a word (until space or end)
     */
    private String readWord(StringReader reader) {
        StringBuilder sb = new StringBuilder();

        while (reader.canRead() && reader.peek() != ' ') {
            sb.append(reader.read());
        }

        return sb.toString();
    }

    @Override
    public Collection<String> getExamples() {
        return List.of(TYPE + "=zombie", NAME + "=Boss", HELMET + "=diamond_helmet", TYPE + "!=zombie", NAKED, TYPE + "=zombie " + AND + " " + NAKED);
    }
}




