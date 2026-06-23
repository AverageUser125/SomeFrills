package com.somefrills.config

import com.somefrills.utils.GuiUtils
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen

class ModMenuIntegration : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory<Screen> { previous ->
            GuiUtils.getGuiScreen(previous)
        }
    }
}