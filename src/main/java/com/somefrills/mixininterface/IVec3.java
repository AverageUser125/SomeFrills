package com.somefrills.mixininterface;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3 {
    Vec3 somefrills$set(double x, double y, double z);

    default Vec3 somefrills$set(Vec3i vec) {
        return somefrills$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3 somefrills$set(Vector3d vec) {
        return somefrills$set(vec.x, vec.y, vec.z);
    }

    default Vec3 somefrills$set(Vec3 pos) {
        return somefrills$set(pos.x, pos.y, pos.z);
    }

    Vec3 somefrills$setXZ(double x, double z);

    Vec3 somefrills$setY(double y);
}