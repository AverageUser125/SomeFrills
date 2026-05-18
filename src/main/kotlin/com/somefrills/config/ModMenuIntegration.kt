package com.somefrills.config

import com.somefrills.utils.GuiUtils
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { previous: Screen? -> getConfigScreen(previous) }
    }

    companion object {
        private fun getConfigScreen(previous: Screen?): Screen {
            return GuiUtils.getGuiScreen(previous)
        }
    }
}