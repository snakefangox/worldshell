package net.snakefangox.worldshell.data;

import net.minecraft.util.math.Vec3d;

public class RelativeVec3d extends Vec3d {

	public RelativeVec3d(double x, double y, double z) {
		super(x, y, z);
	}

	public RelativeVec3d(Vec3d vec3d) {
		super(vec3d.x, vec3d.y, vec3d.z);
	}

	public Vec3d toLocal(Vec3d pos) {
		return pos.subtract(this);
	}

	public Vec3d toGlobal(Vec3d pos) { return pos.add(this); }

	public Vec3d transferCoordSpace(Vec3d target, Vec3d pos) {
		return new Vec3d(target.getX() + (pos.getX() - getX()), target.getY() + (pos.getY() - getY()),
						target.getZ() + (pos.getZ() - getZ()));
	}

	public static RelativeVec3d toRelative(Vec3d add) {
		return new RelativeVec3d(add);
	}
}
