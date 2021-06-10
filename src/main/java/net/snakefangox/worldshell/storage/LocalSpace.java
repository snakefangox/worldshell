package net.snakefangox.worldshell.storage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.collision.RotationHelper;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import oimo.common.Mat3;
import oimo.common.Vec3;

/**
 * Indicates that a given object contains a local coordinate space that differs from
 * the world space. Contains default methods to translate <i>most</i> of minecraft's many coordinate
 * types between the world space and this space or between this space and another local space.
 * <p>
 *
 * <h1>WARNING: DO NOT IMPLEMENT THE DEFAULT METHODS</h1>
 * They're happy just how they are and you'll be happier not having manually written ~25 basic
 * transform methods.
 */
public interface LocalSpace {

    /** The coordinate space of the normal world */
    LocalSpace WORLDSPACE = of(0,0,0);

    /** Creates a local space with no rotation and with origin at the given point */
    static LocalSpace of(double x, double y, double z) {
		return new LocalSpace() {
            @Override
            public double getLocalX() {
                return x;
            }

            @Override
            public double getLocalY() {
                return y;
            }

            @Override
            public double getLocalZ() {
                return z;
            }
        };
    }

    /** Creates a local space with the given rotation and with origin at the given point */
    static LocalSpace of(double x, double y, double z, Mat3 rotation, Mat3 invRotation) {
        return new LocalSpace() {
            @Override
            public double getLocalX() {
                return x;
            }

            @Override
            public double getLocalY() {
                return y;
            }

            @Override
            public double getLocalZ() {
                return z;
            }

            @Override
            public Mat3 getRotationMatrix() {
                return rotation;
            }

            @Override
            public Mat3 getInverseRotationMatrix() {
                return invRotation;
            }
        };
    }

    /** Returns the world coordinate where this local space's X coordinate is zero */
    double getLocalX();

    /** Returns the world coordinate where this local space's Y coordinate is zero */
    double getLocalY();

    /** Returns the world coordinate where this local space's Z coordinate is zero */
    double getLocalZ();

    /** Returns the matrix the local world is rotated by */
    default Mat3 getRotationMatrix() {
        return RotationHelper.identityMat3();
    }

    /** Returns the inverse of the matrix the local world is rotated by */
    default Mat3 getInverseRotationMatrix() {
        return RotationHelper.identityMat3();
    }

    /** -- Below this line lies only madness and a lot of matrix transforms -- **/

    default BlockPos toLocal(BlockPos pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return new BlockPos(vec.x, vec.y, vec.z);
    }

    default BlockPos toGlobal(BlockPos pos) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), pos.getX(), pos.getY(), pos.getZ());
        return new BlockPos(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
    }

    default Vec3d toLocal(Vec3d pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return new Vec3d(vec.x, vec.y, vec.z);
    }

    default Vec3d toGlobal(Vec3d pos) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), pos.getX(), pos.getY(), pos.getZ());
        return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
    }

    default Vec3d toLocal(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return new Vec3d(vec.x, vec.y, vec.z);
    }

    default Vec3d toGlobal(double x, double y, double z) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), x, y, z);
        return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
    }

    default BlockPos toLocal(int x, int y, int z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return new BlockPos(vec.x, vec.y, vec.z);
    }

    default BlockPos toGlobal(int x, int y, int z) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), x, y, z);
        return new BlockPos(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
    }

    default BlockPos.Mutable toLocal(BlockPos.Mutable pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return pos.set(vec.x, vec.y, vec.z);
    }

    default BlockPos.Mutable toGlobal(BlockPos.Mutable pos) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), pos.getX(), pos.getY(), pos.getZ());
        return pos.set(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
    }

    default double toLocalX(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return vec.x;
    }

    default double toLocalY(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return vec.y;
    }

    default double toLocalZ(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        Vec3 vec = RotationHelper.rotatePosition(getInverseRotationMatrix(), newX, newY, newZ);
        return vec.z;
    }

    default double toGlobalX(double x, double y, double z) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), x, y, z);
        return vec.x + getLocalX();
    }

    default double toGlobalY(double x, double y, double z) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), x, y, z);
        return vec.y + getLocalY();
    }

    default double toGlobalZ(double x, double y, double z) {
        Vec3 vec = RotationHelper.rotatePosition(getRotationMatrix(), x, y, z);
        return vec.z + getLocalZ();
    }

    default BlockPos globalToGlobal(LocalSpace target, BlockPos pos) {
        return target.toGlobal((int) toLocalX(pos.getX(), pos.getY(), pos.getZ()),
                (int) toLocalY(pos.getX(), pos.getY(), pos.getZ()), (int) toLocalZ(pos.getX(), pos.getY(), pos.getZ()));
    }

    default Vec3d globalToGlobal(LocalSpace target, Vec3d pos) {
        return target.toGlobal(toLocalX(pos.x, pos.y, pos.z), toLocalY(pos.x, pos.y, pos.z), toLocalZ(pos.x, pos.y, pos.z));
    }

    default Vec3d globalToGlobal(LocalSpace target, double x, double y, double z) {
        return target.toGlobal(toLocalX(x, y, z), toLocalY(x, y, z), toLocalZ(x, y, z));
    }

    default BlockPos globalToGlobal(LocalSpace target, int x, int y, int z) {
        return target.toGlobal((int) toLocalX(x, y, z), (int) toLocalY(x, y, z), (int) toLocalZ(x, y, z));
    }

    default BlockPos.Mutable globalToGlobal(LocalSpace target, BlockPos.Mutable pos) {
        return target.toGlobal(toLocal(pos));
    }

    default double globalToGlobalX(LocalSpace target, double x, double y, double z) {
        return target.toGlobalX(toLocalX(x, y, z), toLocalY(x, y, z), toLocalZ(x, y, z));
    }

    default double globalToGlobalY(LocalSpace target, double x, double y, double z) {
        return target.toGlobalY(toLocalX(x, y, z), toLocalY(x, y, z), toLocalZ(x, y, z));
    }

    default double globalToGlobalZ(LocalSpace target, double x, double y, double z) {
        return target.toGlobalZ(toLocalX(x, y, z), toLocalY(x, y, z), toLocalZ(x, y, z));
    }
}
