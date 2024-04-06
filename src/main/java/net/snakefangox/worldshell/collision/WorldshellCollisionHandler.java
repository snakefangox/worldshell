package net.snakefangox.worldshell.collision;

import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;

/**
 * Links up with some rust code in `worldshell-collision` to handle all the
 * fancy collision math.
 * 
 * NOTE: All coords given are expected to be local to the shell. All rotation is
 * the rotation of the object reletive to the shell, i.e. the shell's rotation
 * inversed for normal entities.
 */
public class WorldshellCollisionHandler {
    public native boolean intersects(Vector3d pos, Quaternion rotation, Vector3d halfExtents, Vector3d blockPos,
            Vector3d blockHalfExtents);

    public native double calculateMaxDistance(byte axis, Vector3d pos, Quaternion rotation, Vector3d halfExtents,
            Vector3d blockPos, Vector3d blockHalfExtents, double maxDist);
}
