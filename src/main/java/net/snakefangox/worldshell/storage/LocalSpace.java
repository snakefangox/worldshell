package net.snakefangox.worldshell.storage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.collision.Matrix3d;
import net.snakefangox.worldshell.collision.ShellCollisionHull;

/**
 * Indicates that a given object contains a local coordinate space that differs from
 * the world space. Contains default methods to translate <i>most</i> of minecraft's many coordinate
 * types between the world space and this space or between this space and another local space.
 * <p>
 * The single coordinate methods exist to avoid allocation if you don't need it,
 * they require all three coordinates so they can rotate properly.
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
    static LocalSpace of(double x, double y, double z, Matrix3d rotation, Matrix3d invRotation) {
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
            public Matrix3d getRotationMatrix() {
                return rotation;
            }

            @Override
            public Matrix3d getInverseRotationMatrix() {
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
    default Matrix3d getRotationMatrix() {
        return Matrix3d.IDENTITY;
    }

    /** Returns the inverse of the matrix the local world is rotated by */
    default Matrix3d getInverseRotationMatrix() {
        return Matrix3d.IDENTITY;
    }

    /* -- Below this line lies only madness and a lot of matrix transforms -- */

    default BlockPos toLocal(BlockPos pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        return new BlockPos(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default BlockPos toGlobal(BlockPos pos) {
        double newX = getRotationMatrix().transformX(pos.getX(), pos.getY(), pos.getZ());
        double newY = getRotationMatrix().transformY(pos.getX(), pos.getY(), pos.getZ());
        double newZ = getRotationMatrix().transformZ(pos.getX(), pos.getY(), pos.getZ());
        return new BlockPos(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default Vec3d toLocal(Vec3d pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        return new Vec3d(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default Vec3d toGlobal(Vec3d pos) {
        double newX = getRotationMatrix().transformX(pos.getX(), pos.getY(), pos.getZ());
        double newY = getRotationMatrix().transformY(pos.getX(), pos.getY(), pos.getZ());
        double newZ = getRotationMatrix().transformZ(pos.getX(), pos.getY(), pos.getZ());
        return new Vec3d(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default Vec3d toLocal(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        return new Vec3d(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default Vec3d toGlobal(double x, double y, double z) {
        double newX = getRotationMatrix().transformX(x, y, z);
        double newY = getRotationMatrix().transformY(x, y, z);
        double newZ = getRotationMatrix().transformZ(x, y, z);
        return new Vec3d(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default BlockPos toLocal(int x, int y, int z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        return new BlockPos(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default BlockPos toGlobal(int x, int y, int z) {
        double newX = getRotationMatrix().transformX(x, y, z);
        double newY = getRotationMatrix().transformY(x, y, z);
        double newZ = getRotationMatrix().transformZ(x, y, z);
        return new BlockPos(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default BlockPos.Mutable toLocal(BlockPos.Mutable pos) {
        double newX = pos.getX() - getLocalX();
        double newY = pos.getY() - getLocalY();
        double newZ = pos.getZ() - getLocalZ();
        return pos.set(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default BlockPos.Mutable toGlobal(BlockPos.Mutable pos) {
        double newX = getRotationMatrix().transformX(pos.getX(), pos.getY(), pos.getZ());
        double newY = getRotationMatrix().transformY(pos.getX(), pos.getY(), pos.getZ());
        double newZ = getRotationMatrix().transformZ(pos.getX(), pos.getY(), pos.getZ());
        return pos.set(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default void toLocal(ShellCollisionHull.Vec3dM pos) {
        double newX = pos.x - getLocalX();
        double newY = pos.y - getLocalY();
        double newZ = pos.z - getLocalZ();
        pos.setAll(getInverseRotationMatrix().transformX(newX, newY, newZ),
                getInverseRotationMatrix().transformY(newX, newY, newZ),
                getInverseRotationMatrix().transformZ(newX, newY, newZ));
    }

    default void toGlobal(ShellCollisionHull.Vec3dM pos) {
        double newX = getRotationMatrix().transformX(pos.x, pos.y, pos.z);
        double newY = getRotationMatrix().transformY(pos.x, pos.y, pos.z);
        double newZ = getRotationMatrix().transformZ(pos.x, pos.y, pos.z);
        pos.setAll(newX + getLocalX(), newY + getLocalY(), newZ + getLocalZ());
    }

    default double toLocalX(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        return getInverseRotationMatrix().transformX(newX, newY, newZ);
    }

    default double toLocalY(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        return getInverseRotationMatrix().transformY(newX, newY, newZ);
    }

    default double toLocalZ(double x, double y, double z) {
        double newX = x - getLocalX();
        double newY = y - getLocalY();
        double newZ = z - getLocalZ();
        return getInverseRotationMatrix().transformZ(newX, newY, newZ);
    }

    default double toGlobalX(double x, double y, double z) {
        double newX = getRotationMatrix().transformX(x, y, z);
        return newX + getLocalX();
    }

    default double toGlobalY(double x, double y, double z) {
        double newY = getRotationMatrix().transformY(x, y, z);
        return newY + getLocalY();
    }

    default double toGlobalZ(double x, double y, double z) {
        double newZ = getRotationMatrix().transformZ(x, y, z);
        return newZ + getLocalZ();
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
