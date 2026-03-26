package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.Main;
import com.somefrills.config.FeatureRegistry;
import com.somefrills.hud.SettingsScreen;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;

import static com.somefrills.Main.mc;
import net.minecraft.text.Text;

public class FeatureWidget extends ButtonWidget implements IWidget {
    private final FeatureRegistry.FeatureInfo info;

    public FeatureWidget(int x, int y, int width, int height, FeatureRegistry.FeatureInfo info) {
        super(x, y, width, height, net.minecraft.text.Text.literal(info.name), button -> {
            var fw = (FeatureWidget) button;
            fw.toggleState();
        });
        this.info = info;
        setTooltip(Tooltip.of(net.minecraft.text.Text.literal(info.description).formatted(Formatting.GRAY)));
        updateState();
    }

    private void updateState() {
        if (info.featureInstance.isActive()) {
            setMessage(net.minecraft.text.Text.literal(info.name).formatted(Formatting.GREEN));
        } else {
            setMessage(net.minecraft.text.Text.literal(info.name).formatted(Formatting.RED));
        }
    }

    private void toggleState() {
        info.featureInstance.setActive(!info.featureInstance.isActive());
        if (info.featureInstance.isActive()) {
            setMessage(net.minecraft.text.Text.literal(info.name).formatted(Formatting.GREEN));
            Main.eventBus.subscribe(info.featureInstance);
        } else {
            setMessage(net.minecraft.text.Text.literal(info.name).formatted(Formatting.RED));
            Main.eventBus.unsubscribe(info.featureInstance);
        }
    }

    @Override
    public boolean mouseClicked(Click mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.button() == 1) {
            if (!info.settings.isEmpty()) {
                mc.setScreen(new SettingsScreen(info));
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }
}
