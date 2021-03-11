package net.snakefangox.worldshell.storage;

import net.minecraft.util.math.BlockPos;

/**
 * Indicates that a given object contains a local coordinate space that differs from
 * the world space. Contains methods to translate <i>most</i> of minecraft's many coordinate
 * types between the world space and this space or between another local space and this space.
 */
public interface LocalSpace {

    /** Returns the world coordinate where this local space's X coordinate is zero */
    double getLocalX();
    /** Returns the world coordinate where this local space's Y coordinate is zero */
    double getLocalY();
    /** Returns the world coordinate where this local space's Z coordinate is zero */
    double getLocalZ();

    default BlockPos toLocal(BlockPos pos) {
        return pos.add(-getLocalX(), -getLocalY(), -getLocalZ());
    }

    default BlockPos toGlobal(BlockPos pos) {
        return pos.add(getLocalX(), getLocalY(), getLocalZ());
    }

}
