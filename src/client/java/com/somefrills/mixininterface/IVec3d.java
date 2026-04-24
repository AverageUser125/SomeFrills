package com.somefrills.mixininterface;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3d {
    Vec3d somefrills$set(double x, double y, double z);

    default Vec3d somefrills$set(Vec3i vec) {
        return somefrills$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d somefrills$set(Vector3d vec) {
        return somefrills$set(vec.x, vec.y, vec.z);
    }

    default Vec3d somefrills$set(Vec3d pos) {
        return somefrills$set(pos.x, pos.y, pos.z);
    }

    Vec3d somefrills$setXZ(double x, double z);

    Vec3d somefrills$setY(double y);
}