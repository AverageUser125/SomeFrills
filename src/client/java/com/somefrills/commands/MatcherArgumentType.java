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
import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.somefrills.features.misc.matcher.MatcherTypes.*;

public class MatcherArgumentType implements ArgumentType<Matcher> {

    public static MatcherArgumentType matcher() {
        return new MatcherArgumentType();
    }

    public static Matcher getMatcher(CommandContext<FabricClientCommandSource> ctx, String name) {
        return ctx.getArgument(name, Matcher.class);
    }

    @Override
    public Matcher parse(StringReader reader) throws CommandSyntaxException {
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
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {

        String input = builder.getRemaining();
        String trimmed = input.trim();

        String[] parts = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        String last = parts.length == 0 ? "" : parts[parts.length - 1];
        boolean endsWithSpace = input.endsWith(" ");

        // Build prefix from all parts EXCEPT the last one (which is what we're currently typing)
        String prefix = "";
        if (parts.length > 1) {
            prefix = String.join(" ", java.util.Arrays.copyOfRange(parts, 0, parts.length - 1)) + " ";
        } else if (parts.length == 1 && endsWithSpace) {
            // If only one part and it ends with space, that part is complete
            prefix = parts[0] + " ";
            last = "";
        }

        // If we have a complete matcher followed by space, clear last
        if (endsWithSpace && isCompleteMatcher(last)) {
            prefix = String.join(" ", java.util.Arrays.copyOfRange(parts, 0, parts.length)) + " ";
            last = "";
        }

        // =========================
        // HARD SAFETY GATE (CRASH FIX)
        // =========================
        if (last.equalsIgnoreCase(AND) || last.equalsIgnoreCase(OR)) {
            suggestMatcherKeys(builder, prefix, "");
            return builder.buildFuture();
        }

        State state = determineState(parts, endsWithSpace);
        switch (state) {

            case EXPECT_TERM -> {
                suggestMatcherKeys(builder, prefix, "");
            }

            case EXPECT_NEW_TERM -> {
                suggestMatchers(builder, prefix);
                builder.suggest(prefix + "(");
            }

            case EXPECT_OPERATOR -> {
                suggestOperatorOnly(builder, prefix, last);
            }

            case COMPLETE_MATCHER -> {
                suggestOperatorOnly(builder, prefix, last);
            }

            case IN_MATCHER_KEY -> {
                suggestMatcherKeys(builder, prefix, last);
            }

            case IN_MATCHER_VALUE -> {
                suggestValues(builder, prefix, last);
            }
        }

        return builder.buildFuture();
    }

    // =========================
    // STATE MACHINE
    // =========================

    private enum State {
        EXPECT_TERM,
        EXPECT_OPERATOR,
        EXPECT_NEW_TERM,
        IN_MATCHER_KEY,
        IN_MATCHER_VALUE,
        COMPLETE_MATCHER
    }

    private State determineState(String[] parts, boolean endsWithSpace) {
        if (parts.length == 0) return State.EXPECT_TERM;

        String last = parts[parts.length - 1];

        if (isLogicalOperator(last)) {
            return State.EXPECT_NEW_TERM;
        }

        // Check if this is a partial operator, BUT NOT after a logical operator
        if (isPartialOperator(last)) {
            // After AND/OR, "A" or "O" should be treated as matcher key start, not operator continuation
            if (parts.length >= 2 && isLogicalOperator(parts[parts.length - 2])) {
                return State.IN_MATCHER_KEY;
            }
            return State.EXPECT_OPERATOR;
        }

        // Complete matcher (with or without space)
        if (endsWithSpace && isCompleteMatcher(last)) {
            return State.COMPLETE_MATCHER;
        }

        // Completed matcher without space on previous token
        if (parts.length >= 2 && isCompleteMatcher(parts[parts.length - 2])) {
            return State.EXPECT_OPERATOR;
        }

        if (endsWithSpace) {
            if (isLogicalOperator(last)) return State.EXPECT_NEW_TERM;
            return State.IN_MATCHER_KEY;
        }

        if (last.contains("=")) return State.IN_MATCHER_VALUE;


        return State.IN_MATCHER_KEY;
    }

    // =========================
    // HELPERS
    // =========================

    private boolean isCompleteMatcher(String s) {
        // Check for complete matchers like NAKED
        for (String complete : MatcherTypes.getCompleteMatchers()) {
            if (s.equalsIgnoreCase(complete)) {
                return true;
            }
        }
        return s.contains("=") && !s.endsWith("=") && !s.endsWith("!=");
    }

    private boolean isLogicalOperator(String s) {
        return s.equalsIgnoreCase(AND) || s.equalsIgnoreCase(OR);
    }

    private boolean isPartialOperator(String s) {
        return AND.toUpperCase().startsWith(s.toUpperCase())
                || OR.toUpperCase().startsWith(s.toUpperCase());
    }

    // =========================
    // SUGGESTIONS
    // =========================
    private void suggestMatcherKeys(SuggestionsBuilder builder, String prefix, String last) {

        // NEVER suggest full matcher list after operator transition
        if (last.equalsIgnoreCase(AND) || last.equalsIgnoreCase(OR)) {
            return;
        }

        for (String m : getAllMatchers()) {
            if (last.isEmpty() || m.toLowerCase().startsWith(last.toLowerCase())) {
                builder.suggest(prefix + m);
            }
        }

        for (String m : getAllMatchers()) {
            if (m.equalsIgnoreCase(last)) {
                builder.suggest(prefix + m + "=");
                builder.suggest(prefix + m + "!=");
                return;
            }
        }

        // Check complete matchers (like NAKED)
        for (String complete : MatcherTypes.getCompleteMatchers()) {
            if (last.isEmpty() || complete.toLowerCase().startsWith(last.toLowerCase())) {
                builder.suggest(prefix + complete);
            }
            if (complete.equalsIgnoreCase(last)) {
                return;
            }
        }
    }

    private void suggestOperatorOnly(SuggestionsBuilder builder, String prefix, String last) {

        if (!last.isEmpty() && isPartialOperator(last)) {
            if (AND.startsWith(last.toUpperCase())) {
                builder.suggest(prefix + AND);
            }
            if (OR.startsWith(last.toUpperCase())) {
                builder.suggest(prefix + OR);
            }
            return;
        }

        builder.suggest(prefix + AND);
        builder.suggest(prefix + OR);
    }

    private void suggestValues(SuggestionsBuilder builder, String prefix, String last) {

        int eq = last.indexOf('=');
        String key = last.substring(0, eq).replace("!", "").toUpperCase();
        String value = eq < last.length() - 1 ? last.substring(eq + 1).toLowerCase() : "";

        String valuePrefix = prefix + last.substring(0, eq + 1);

        switch (key) {

            case TYPE -> Registries.ENTITY_TYPE.forEach(type -> {
                String id = Utils.stripPrefix(
                        Registries.ENTITY_TYPE.getId(type).toString(),
                        MINECRAFT_PREFIX
                ).toLowerCase();

                if (id.startsWith(value)) {
                    builder.suggest(valuePrefix + id);
                }
            });

            case NAME -> {
                // free text
            }

            case AREA -> {
                for (String area : Area.getAllDisplayNames()) {
                    if (area.toLowerCase().startsWith(value)) {
                        builder.suggest(valuePrefix +
                                (area.contains(" ") ? "\"" + area + "\"" : area));
                    }
                }
            }

            default -> {
                if (MatcherTypes.isEquipmentSlot(key)) {

                    for (String opt : getEmptySlotOptions()) {
                        if (opt.startsWith(value)) {
                            builder.suggest(valuePrefix + opt);
                        }
                    }

                    Registries.ITEM.forEach(item -> {
                        String id = Utils.stripPrefix(
                                Registries.ITEM.getId(item).toString(),
                                MINECRAFT_PREFIX
                        ).toLowerCase();

                        if (id.startsWith(value)) {
                            builder.suggest(valuePrefix + id);
                        }
                    });
                }
            }
        }
    }

    // =========================
    // PARSER SUPPORT
    // =========================

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
                    sb.append(reader.read()); // consume the space
                    sb.append(word); // append the word we already read
                }
            } else {
                sb.append(reader.read());
            }
        }

        return sb.toString();
    }

    private String readWord(StringReader reader) {
        StringBuilder word = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ') {
            word.append(reader.read());
        }
        return word.toString();
    }

    private void suggestMatchers(SuggestionsBuilder builder, String prefix) {
        for (String matcher : getAllMatchers()) {
            builder.suggest(prefix + matcher);
        }
        for (String complete : MatcherTypes.getCompleteMatchers()) {
            builder.suggest(prefix + complete);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return List.of(
                TYPE + "=zombie",
                NAME + "=Boss",
                AREA + "=Private Island",
                HELMET + "=diamond_helmet",
                TYPE + "!=zombie",
                NAKED,
                TYPE + "=zombie " + AND + " " + NAKED
        );
    }
}