package com.somefrills.commands;

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
        StringBuilder token = new StringBuilder();

        // Read comma-separated matcher arguments
        while (reader.canRead()) {
            char c = reader.peek();

            if (c == '"') {
                token.append(c);
                reader.skip();
                while (reader.canRead() && reader.peek() != '"') {
                    token.append(reader.read());
                }
                if (reader.canRead() && reader.peek() == '"') {
                    token.append(reader.read());
                }
            } else if (Character.isWhitespace(c)) {
                break;
            } else {
                token.append(reader.read());
            }
        }

        String input = token.toString().trim();

        try {
            return MatchInfo.fromString(input);
        } catch (MatchInfo.MatcherParseException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Invalid matcher format: " + e.getMessage());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return builder.buildFuture();
    }
}
