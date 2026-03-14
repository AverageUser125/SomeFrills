package com.example;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class GlowPlayerModMenuImpl implements ModMenuApi {

    private static Screen buildConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("GlowPlayer - Settings"))
                .setSavingRunnable(AllConfig::save);

        // ===== Experiment Solver Category =====
        ConfigCategory experimentsCategory =
                builder.getOrCreateCategory(Text.literal("Experiment Solver"));

        // Enable Chronomatron Solver
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Chronomatron Solver"),
                                AllConfig.enableChronomatron
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.enableChronomatron = value)
                        .setTooltip(Text.literal("Enable automatic Chronomatron solving"))
                        .build()
        );

        // Enable Ultrasequencer Solver
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Ultrasequencer Solver"),
                                AllConfig.enableUltrasequencer
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.enableUltrasequencer = value)
                        .setTooltip(Text.literal("Enable automatic Ultrasequencer solving"))
                        .build()
        );

        // Click Delay (0–1000 ms)
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startLongField(
                                Text.literal("Click Delay (ms)"),
                                AllConfig.clickDelay
                        )
                        .setDefaultValue(200L)
                        .setMin(0L)
                        .setMax(1000L)
                        .setSaveConsumer(value -> AllConfig.clickDelay = value)
                        .setTooltip(Text.literal(
                                "Time in milliseconds between automatic clicks (0–1000)"
                        ))
                        .build()
        );

        // Auto Close
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Auto Close"),
                                AllConfig.autoClose
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.autoClose = value)
                        .setTooltip(Text.literal(
                                "Automatically close the GUI after completing the experiment"
                        ))
                        .build()
        );

        // Serum Count (0–3)
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startIntField(
                                Text.literal("Serum Count"),
                                AllConfig.serumCount
                        )
                        .setDefaultValue(3)
                        .setMin(0)
                        .setMax(3)
                        .setSaveConsumer(value -> AllConfig.serumCount = value)
                        .setTooltip(Text.literal(
                                "Consumed Metaphysical Serum count (0–3)"
                        ))
                        .build()
        );

        // Get Max XP
        experimentsCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Get Max XP"),
                                AllConfig.getMaxXp
                        )
                        .setDefaultValue(false)
                        .setSaveConsumer(value -> AllConfig.getMaxXp = value)
                        .setTooltip(Text.literal(
                                "Solve Chronomatron to 15 and Ultrasequencer to 20 for max XP"
                        ))
                        .build()
        );

        // ===== Creeper Category =====
        ConfigCategory creeperCategory =
                builder.getOrCreateCategory(Text.literal("Creeper"));

        // Make Creepers Not Invisible
        creeperCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Not Invisible"),
                                AllConfig.creeperNotInvisible
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.creeperNotInvisible = value)
                        .setTooltip(Text.literal(
                                "Make invisible creepers fully visible"
                        ))
                        .build()
        );

        // Hide Charged Creeper Effect
        creeperCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Not Charged"),
                                AllConfig.creeperNotCharged
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.creeperNotCharged = value)
                        .setTooltip(Text.literal(
                                "Hide the charging effect on creepers"
                        ))
                        .build()
        );

        // Show Creeper HP
        creeperCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Show HP"),
                                AllConfig.creeperShowHP
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.creeperShowHP = value)
                        .setTooltip(Text.literal(
                                "Display creeper health when they are not invisible"
                        ))
                        .build()
        );

        // ===== Chocolate Factory Category =====
        ConfigCategory chocolateFactoryCategory =
                builder.getOrCreateCategory(Text.literal("Chocolate Factory"));

        // Claim Stray Rabbits
        chocolateFactoryCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Claim Stray"),
                                AllConfig.claimStray
                        )
                        .setDefaultValue(false)
                        .setSaveConsumer(value -> AllConfig.claimStray = value)
                        .setTooltip(Text.literal("Automatically claim stray rabbits"))
                        .build()
        );

        // Claim Delay
        chocolateFactoryCategory.addEntry(
                builder.entryBuilder()
                        .startLongField(
                                Text.literal("Claim Delay (ms)"),
                                AllConfig.claimDelay
                        )
                        .setDefaultValue(150L)
                        .setMin(0L)
                        .setMax(1500L)
                        .setSaveConsumer(value -> AllConfig.claimDelay = value)
                        .setTooltip(Text.literal(
                                "Time in milliseconds between claim actions (0–1500)"
                        ))
                        .build()
        );

        // ===== RNG Meter Display Category =====
        ConfigCategory rngMeterCategory =
                builder.getOrCreateCategory(Text.literal("RNG Meter Display"));

        // Show RNG Meter
        rngMeterCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Show RNG Meter"),
                                AllConfig.showRngMeter
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.showRngMeter = value)
                        .setTooltip(Text.literal("Display RNG meter item information and odds"))
                        .build()
        );

        // Debug RNG Display
        rngMeterCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Debug RNG Display"),
                                AllConfig.debugRngDisplay
                        )
                        .setDefaultValue(false)
                        .setSaveConsumer(value -> AllConfig.debugRngDisplay = value)
                        .setTooltip(Text.literal("Enable console output for RNG meter analysis"))
                        .build()
        );

        // ===== Fixes Category =====
        ConfigCategory fixesCategory =
                builder.getOrCreateCategory(Text.literal("Fixes"));

        // Gemstone Dsync Fix
        fixesCategory.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Text.literal("Gemstone Dsync Fix"),
                                AllConfig.gemstoneDsyncFix
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> AllConfig.gemstoneDsyncFix = value)
                        .setTooltip(Text.literal("Fix desynchronization issues with gemstone drops"))
                        .build()
        );

        return builder.build();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GlowPlayerModMenuImpl::buildConfigScreen;
    }
}
