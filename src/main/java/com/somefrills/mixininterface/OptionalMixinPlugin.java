package com.somefrills.mixininterface;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class OptionalMixinPlugin implements IMixinConfigPlugin {
    private int mixinPackageLength = 0;

    @Override
    public void onLoad(String mixinPackage) {
        mixinPackageLength = mixinPackage.length();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String modId = getModIdFromMixinClassName(mixinClassName);
        if (modId.isEmpty()) return true;
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    // com.somefrills.mixin.skyhanni.SkyHanniMixin -> skyhanni
    // com.somefrills.mixin.SomeFrillsMixin -> ""
    private String getModIdFromMixinClassName(String mixinClassName) {
        int startIndex = mixinPackageLength + 1; // character after the '.' following the mixin package
        if (startIndex >= mixinClassName.length()) return "";

        int endIndex = mixinClassName.indexOf('.', startIndex);
        if (endIndex == -1) return "";

        return mixinClassName.substring(startIndex, endIndex);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
