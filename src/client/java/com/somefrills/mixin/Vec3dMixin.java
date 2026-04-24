package com.somefrills.mixin;


import com.somefrills.mixininterface.IVec3d;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3d.class)
public abstract class Vec3dMixin implements IVec3d {
    @Shadow
    @Final
    @Mutable
    public double x;
    @Shadow
    @Final
    @Mutable
    public double y;
    @Shadow
    @Final
    @Mutable
    public double z;

    @Override
    public Vec3d somefrills$set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return (Vec3d) (Object) this;
    }

    @Override
    public Vec3d somefrills$setXZ(double x, double z) {
        this.x = x;
        this.z = z;

        return (Vec3d) (Object) this;
    }

    @Override
    public Vec3d somefrills$setY(double y) {
        this.y = y;

        return (Vec3d) (Object) this;
    }
}