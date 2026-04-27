package com.somefrills.mixin;

import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(DyedColorComponent.class)
public class DyedColorComponentMixin {
    /**
     * @author SomeFrills
     * @reason I don't want it.
     */
    @Overwrite
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
    }
}
