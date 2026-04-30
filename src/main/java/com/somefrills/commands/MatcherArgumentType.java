package com.somefrills.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.somefrills.features.misc.glowmob.MatchInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class MatcherArgumentType implements ArgumentType<MatchInfo> {

    public static MatcherArgumentType matcher() {
        return new MatcherArgumentType();
    }

    public static MatchInfo getMatcher(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, MatchInfo.class);
    }

    @Override
    public MatchInfo parse(StringReader reader) throws CommandSyntaxException {
        String json = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return MatchInfo.fromJson(obj);

        } catch (JsonParseException | IllegalStateException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherParseException()
                    .create("Invalid matcher JSON: " + e.getMessage());

        } catch (MatchInfo.MatcherParseException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherParseException()
                    .create("Invalid matcher format: " + e.getMessage());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context,
            SuggestionsBuilder builder
    ) {

        String input = builder.getInput();
        int cursor = builder.getStart();

        String beforeCursor = input.substring(0, Math.min(input.length(), cursor + builder.getRemaining().length()));

        // Trim safe view near cursor
        String local = builder.getRemaining();

        // Detect context
        SuggestionContext ctx = detectContext(beforeCursor);

        switch (ctx.type) {

            case EMPTY -> {
                builder.suggest("{\"type\":[\"zombie\"],\"name\":\"\",\"maxHp\":20}");
                builder.suggest("{\"type\":[\"skeleton\"],\"gear\":[\"BOW\"],\"maxHp\":30}");
            }

            case EXPECTING_KEY -> {
                suggestOnce(builder, "type");
                suggestOnce(builder, "name");
                suggestOnce(builder, "area");
                suggestOnce(builder, "gear");
                suggestOnce(builder, "maxHp");
            }

            case EXPECTING_VALUE -> {
                switch (ctx.key) {
                    case "type" -> {
                        builder.suggest("[\"zombie\"]");
                        builder.suggest("[\"zombie\",\"creeper\"]");
                        builder.suggest("[\"skeleton\"]");
                    }
                    case "gear" -> {
                        builder.suggest("[\"SWORD\"]");
                        builder.suggest("[\"BOW\"]");
                        builder.suggest("[\"ARMOR\"]");
                    }
                    case "name" -> builder.suggest("\"name\"");
                    case "maxHp" -> {
                        builder.suggest("20");
                        builder.suggest("40");
                        builder.suggest("100");
                    }
                    case "area" -> builder.suggest("\"spawn\"");
                }
            }
        }

        return builder.buildFuture();
    }

    private static void suggestOnce(SuggestionsBuilder builder, String key) {
        builder.suggest("\"" + key + "\":");
    }

    // ---------------- CONTEXT PARSER ----------------

    private static SuggestionContext detectContext(String input) {
        boolean inString = false;
        int braceDepth = 0;
        String currentKey = null;
        boolean readingKey = false;
        boolean readingValue = false;

        StringBuilder keyBuffer = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inString = !inString;

                if (inString) {
                    keyBuffer.setLength(0);
                    readingKey = true;
                } else {
                    if (readingKey && currentKey == null) {
                        currentKey = keyBuffer.toString();
                        readingKey = false;
                    }
                }
                continue;
            }

            if (inString && readingKey) {
                keyBuffer.append(c);
                continue;
            }

            if (!inString) {
                switch (c) {
                    case '{' -> braceDepth++;
                    case '}' -> braceDepth--;
                    case ':' -> readingValue = true;
                    case ',' -> {
                        readingValue = false;
                        currentKey = null;
                    }
                }
            }
        }

        if (input.trim().isEmpty()) {
            return new SuggestionContext(Type.EMPTY, null);
        }

        if (readingValue && currentKey != null) {
            return new SuggestionContext(Type.EXPECTING_VALUE, currentKey);
        }

        return new SuggestionContext(Type.EXPECTING_KEY, null);
    }

    private enum Type {
        EMPTY,
        EXPECTING_KEY,
        EXPECTING_VALUE
    }

    private record SuggestionContext(Type type, String key) {
    }
}