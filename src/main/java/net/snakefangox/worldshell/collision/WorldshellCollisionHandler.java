package net.snakefangox.worldshell.collision;

import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;

/**
 * Links up with some rust code in `worldshell-collision` to handle all the
 * fancy collision math.
 */
public class WorldshellCollisionHandler {
    public native boolean intersects(Vector3d pos, Quaternion rotation, Vector3d halfExtents, Vector3d blockPos,
            Vector3d blockHalfExtents);

    public native double calculateMaxDistance(Vector3d axis, Vector3d pos, Quaternion rotation, Vector3d halfExtents,
            Vector3d blockPos, Vector3d blockHalfExtents, double maxDist);

    public native boolean contains(Vector3d point, Quaternion rotation, Vector3d blockPos, Vector3d blockHalfExtents);

    public native Vector3d raycast(Vector3d start, Vector3d direction, Quaternion rotation, Vector3d blockPos,
            Vector3d blockHalfExtents);
}
