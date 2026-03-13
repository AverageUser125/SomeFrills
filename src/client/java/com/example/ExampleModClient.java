package com.example;

import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {

    private GlowPlayerCommand glowPlayerCommand;
    private ExperimentIntegration experimentIntegration;
    private ChocolateFactory chocolateFactory;
    private RNGMeterDisplay rngMeterDisplay;

    @Override
    public void onInitializeClient() {
        glowPlayerCommand = new GlowPlayerCommand();
        experimentIntegration = new ExperimentIntegration();
        chocolateFactory = new ChocolateFactory();
        rngMeterDisplay = new RNGMeterDisplay();
    }
}
