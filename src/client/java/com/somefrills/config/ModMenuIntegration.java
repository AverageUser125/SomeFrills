package com.somefrills.config;

import com.somefrills.Main;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getScreen();
    }

    private static Screen getScreen() {
        var editor = Main.config.getEditor();
        GuiContext guiContext = new GuiContext(new GuiElementComponent(editor));
        return new MoulConfigScreenComponent(Text.empty(), guiContext, null);
    }
}
