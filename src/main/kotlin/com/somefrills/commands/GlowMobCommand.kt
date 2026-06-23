package com.somefrills.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.somefrills.features.misc.glowmob.GlowMob
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.features.misc.glowmob.chestui.GlowMobRules
import com.somefrills.misc.RenderColor
import com.somefrills.utils.GuiUtils
import com.somefrills.utils.TextUtils
import com.somefrills.utils.toPlain
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity

object GlowMobCommand {

    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("glowmob")

            .executes { ctx ->

                if (!GlowMob.isActive()) {
                    ctx.source.sendError(
                        Component.literal("GlowMob feature is not active.")
                    )

                    return@executes 0
                }

                GuiUtils.setScreen(GlowMobRules())

                1
            }

            .then(
                literal("list")
                    .executes(::listRules)
            )

            .then(
                literal("listglowing")

                    .executes(::listGlowingMobs)

                    .then(
                        argument("id", IntegerArgumentType.integer(1))

                            .executes { ctx ->
                                listGlowingMobs(
                                    ctx,
                                    IntegerArgumentType.getInteger(ctx, "id")
                                )
                            }
                    )
            )

            .then(
                literal("clear")

                    .executes { ctx ->

                        GlowMob.clearRules()

                        ctx.source.sendFeedback(
                            Component.literal("Cleared all entity highlight rules.")
                        )

                        1
                    }
            )

            .then(
                literal("add")

                    .then(
                        CommandColorUtils.buildColorArgument()

                            .then(
                                argument(
                                    "matcher",
                                    MatcherArgumentType.matcher()
                                )

                                    .executes { ctx ->

                                        val color =
                                            CommandColorUtils.getColorArgument(ctx)

                                        val matcher =
                                            MatcherArgumentType.getMatcher(
                                                ctx,
                                                "matcher"
                                            )

                                        addRuleCommand(
                                            ctx,
                                            color,
                                            matcher
                                        )
                                    }
                            )
                    )
            )

            .then(
                literal("remove")

                    .then(
                        argument(
                            "id",
                            IntegerArgumentType.integer(1)
                        )

                            .executes(::removeRuleCommand)
                    )
            )
    }

    private fun listGlowingMobs(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {
        return listGlowingMobs(ctx, -1)
    }

    private fun listGlowingMobs(
        ctx: CommandContext<FabricClientCommandSource>,
        ruleId: Int
    ): Int {

        val rules = GlowMob.rules

        if (rules.isEmpty()) {
            ctx.source.sendFeedback(
                Component.literal("No glow rules found.")
            )

            return 1
        }

        if (ruleId != -1 && (ruleId < 1 || ruleId > rules.size)) {
            ctx.source.sendFeedback(
                Component.literal("Invalid rule id.")
            )

            return 0
        }

        val sb = StringBuilder()

        val entries =
            if (ruleId == -1) {
                GlowMob.getGlowingMobs()
            } else {
                GlowMob.getGlowingMobs(
                    listOf(rules[ruleId - 1])
                )
            }

        for (entry in entries) {

            val rule = entry.rule
            val entities: List<LivingEntity> = entry.entities

            val currentRuleId = rules.indexOf(rule) + 1

            sb.append("Rule ")
                .append(currentRuleId)
                .append(" (")
                .append(TextUtils.colorToString(rule.color()))
                .append("):\n")

            if (entities.isEmpty()) {
                sb.append("  - No matching glowing mobs\n")
                continue
            }

            for (entity in entities) {

                sb.append("  - ")
                    .append(entity.name.toPlain())
                    .append(" (")
                    .append(entity.type.toShortString())
                    .append(")")
                    .append(", MaxHP: ")
                    .append(String.format("%.1f", entity.maxHealth))
                    .append(", Pos: [")
                    .append(String.format("%.1f", entity.x))
                    .append(", ")
                    .append(String.format("%.1f", entity.y))
                    .append(", ")
                    .append(String.format("%.1f", entity.z))
                    .append("]\n")
            }
        }

        ctx.source.sendFeedback(
            Component.literal(sb.toString())
        )

        return 1
    }

    private fun addRuleCommand(
        ctx: CommandContext<FabricClientCommandSource>,
        color: RenderColor,
        matcher: MatchInfo
    ): Int {

        val addIndex = GlowMob.addRule(matcher, color)

        if (addIndex != -1) {

            ctx.source.sendFeedback(
                Component.literal(
                    "Added rule $addIndex with color ${
                        TextUtils.colorToString(color)
                    } and matcher: ${matcher.serialize()}"
                )
            )

        } else {

            ctx.source.sendError(
                Component.literal("Some error")
            )
        }

        return if (addIndex == -1) 0 else 1
    }

    private fun removeRuleCommand(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {

        val id = IntegerArgumentType.getInteger(ctx, "id")

        val removed = GlowMob.removeRule(id)

        if (removed) {
            ctx.source.sendFeedback(
                Component.literal("Removed rule '$id'.")
            )
        } else {
            ctx.source.sendError(
                Component.literal("Rule '$id' not found.")
            )
        }

        return if (removed) 1 else 0
    }

    private fun listRules(
        ctx: CommandContext<FabricClientCommandSource>
    ): Int {

        val sb = StringBuilder()

        sb.append("=== Entity Highlight Rules ===\n")

        val rules = GlowMob.rules

        if (rules.isEmpty()) {

            sb.append("  (none)\n")

        } else {

            for ((idx, rule) in rules.withIndex()) {

                val color =
                    TextUtils.colorToString(rule.color())

                sb.append("  • id=")
                    .append(idx + 1)
                    .append(", color=")
                    .append(color)
                    .append("\n")
            }
        }

        ctx.source.sendFeedback(
            Component.literal(sb.toString())
        )

        return 1
    }
}