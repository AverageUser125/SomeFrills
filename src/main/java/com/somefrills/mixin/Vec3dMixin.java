package com.somefrills.mixin;


import com.somefrills.mixininterface.IVec3d;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

// TODO(Ravel): can not resolve target class Vec3
// TODO(Ravel): can not resolve target class Vec3
@Mixin(Vec3.class)
public abstract class Vec3dMixin implements IVec3d {
    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    @Mutable
    public double x;
    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    @Mutable
    public double y;
    // TODO(Ravel): Could not determine a single target
// TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    @Mutable
    public double z;

    @Override
    public Vec3 somefrills$set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return (Vec3) (Object) this;
    }

    @Override
    public Vec3 somefrills$setXZ(double x, double z) {
        this.x = x;
        this.z = z;

        return (Vec3) (Object) this;
    }

    @Override
    public Vec3 somefrills$setY(double y) {
        this.y = y;

        return (Vec3) (Object) this;
    }
}