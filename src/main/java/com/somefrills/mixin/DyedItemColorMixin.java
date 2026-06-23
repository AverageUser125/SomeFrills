package com.somefrills.mixin;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(DyedItemColor.class)
public class DyedItemColorMixin {
    /**
     * @author SomeFrills
     * @reason I don't want it.
     */
    @Overwrite
    public void appendTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
    }
}
