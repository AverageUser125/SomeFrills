package com.somefrills.commands

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.somefrills.features.misc.glowmob.MatchInfo
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.util.concurrent.CompletableFuture

class MatcherArgumentType : ArgumentType<MatchInfo> {

    companion object {

        fun matcher(): MatcherArgumentType {
            return MatcherArgumentType()
        }

        fun getMatcher(
            context: CommandContext<FabricClientCommandSource>,
            name: String
        ): MatchInfo {

            return context.getArgument(
                name,
                MatchInfo::class.java
            )
        }

        private fun suggestOnce(
            builder: SuggestionsBuilder,
            key: String
        ) {

            builder.suggest("\"$key\":")
        }

        private fun detectContext(
            input: String
        ): SuggestionContext {

            var inString = false
            var braceDepth = 0

            var currentKey: String? = null

            var readingKey = false
            var readingValue = false

            val keyBuffer = StringBuilder()

            for (c in input) {

                if (c == '"') {

                    inString = !inString

                    if (inString) {

                        keyBuffer.setLength(0)
                        readingKey = true

                    } else {

                        if (readingKey && currentKey == null) {

                            currentKey = keyBuffer.toString()

                            readingKey = false
                        }
                    }

                    continue
                }

                if (inString && readingKey) {

                    keyBuffer.append(c)

                    continue
                }

                if (!inString) {

                    when (c) {

                        '{' -> braceDepth++

                        '}' -> braceDepth--

                        ':' -> readingValue = true

                        ',' -> {
                            readingValue = false
                            currentKey = null
                        }
                    }
                }
            }

            if (input.trim().isEmpty()) {
                return SuggestionContext(
                    Type.EMPTY,
                    null
                )
            }

            if (readingValue && currentKey != null) {

                return SuggestionContext(
                    Type.EXPECTING_VALUE,
                    currentKey
                )
            }

            return SuggestionContext(
                Type.EXPECTING_KEY,
                null
            )
        }
    }

    override fun parse(
        reader: StringReader
    ): MatchInfo {

        val json = reader.remaining

        reader.cursor = reader.totalLength

        try {

            val obj = JsonParser
                .parseString(json)
                .asJsonObject

            return MatchInfo.fromJson(obj)

        } catch (e: JsonParseException) {

            throw CommandSyntaxException
                .BUILT_IN_EXCEPTIONS
                .dispatcherParseException()
                .create(
                    "Invalid matcher JSON: ${e.message}"
                )

        } catch (e: IllegalStateException) {

            throw CommandSyntaxException
                .BUILT_IN_EXCEPTIONS
                .dispatcherParseException()
                .create(
                    "Invalid matcher JSON: ${e.message}"
                )

        } catch (e: MatchInfo.MatcherParseException) {

            throw CommandSyntaxException
                .BUILT_IN_EXCEPTIONS
                .dispatcherParseException()
                .create(
                    "Invalid matcher format: ${e.message}"
                )
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        val input = builder.input
        val cursor = builder.start

        val beforeCursor = input.substring(
            0,
            minOf(
                input.length,
                cursor + builder.remaining.length
            )
        )

        val local = builder.remaining

        val ctx = detectContext(beforeCursor)

        when (ctx.type) {

            Type.EMPTY -> {

                builder.suggest(
                    "{\"type\":[\"zombie\"],\"name\":\"\",\"maxHp\":20}"
                )

                builder.suggest(
                    "{\"type\":[\"skeleton\"],\"gear\":[\"BOW\"],\"maxHp\":30}"
                )
            }

            Type.EXPECTING_KEY -> {

                suggestOnce(builder, "type")
                suggestOnce(builder, "name")
                suggestOnce(builder, "area")
                suggestOnce(builder, "gear")
                suggestOnce(builder, "maxHp")
            }

            Type.EXPECTING_VALUE -> {

                when (ctx.key) {

                    "type" -> {

                        builder.suggest("[\"zombie\"]")

                        builder.suggest(
                            "[\"zombie\",\"creeper\"]"
                        )

                        builder.suggest("[\"skeleton\"]")
                    }

                    "gear" -> {

                        builder.suggest("[\"SWORD\"]")

                        builder.suggest("[\"BOW\"]")

                        builder.suggest("[\"ARMOR\"]")
                    }

                    "name" -> {
                        builder.suggest("\"name\"")
                    }

                    "maxHp" -> {

                        builder.suggest("20")

                        builder.suggest("40")

                        builder.suggest("100")
                    }

                    "area" -> {
                        builder.suggest("\"spawn\"")
                    }
                }
            }
        }

        return builder.buildFuture()
    }

    private enum class Type {
        EMPTY,
        EXPECTING_KEY,
        EXPECTING_VALUE
    }

    private data class SuggestionContext(
        val type: Type,
        val key: String?
    )
}