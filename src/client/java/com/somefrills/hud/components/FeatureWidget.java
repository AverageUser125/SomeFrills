package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.Main;
import com.somefrills.config.FeatureRegistry;
import com.somefrills.hud.SettingsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;

import static com.somefrills.Main.mc;

public class FeatureWidget extends ButtonWidget implements IWidget {
    private final FeatureRegistry.FeatureInfo info;

    public FeatureWidget(int x, int y, int width, int height, FeatureRegistry.FeatureInfo info) {
        super(x, y, width, height, Component.literal(info.name), button -> {
            var btn = (FeatureWidget) button;
            btn.toggleState();
        }, Button.DEFAULT_NARRATION);;
        this.info = info;
        setTooltip(Tooltip.create(Component.literal(info.description).withStyle(ChatFormatting.GRAY)));
        updateState();
    }

    private void updateState() {
        if(info.featureInstance.isActive()) {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.GREEN));
        } else {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.RED));
        }
    }

    private void toggleState() {
        info.featureInstance.setActive(!info.featureInstance.isActive());
        if(info.featureInstance.isActive()) {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.GREEN));
            Main.eventBus.subscribe(info.featureInstance);
        } else {
            setMessage(Component.literal(info.name).withStyle(ChatFormatting.RED));
            Main.eventBus.unsubscribe(info.featureInstance);
        }
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if(mouseButtonEvent.isRight()) {
            mc.setScreen(new SettingsScreen(info));
            return;
        }
        this.onPress(mouseButtonEvent);
    }
}
