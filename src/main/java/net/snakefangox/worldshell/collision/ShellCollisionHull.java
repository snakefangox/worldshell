package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.storage.WorldShell;

import java.util.Optional;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.<p>
 * Some <i><b>very<b/></i> questionable decisions are made to optimise the collision calculations, you peer inside at your own risk.
 */
public class ShellCollisionHull extends Box {

	final WorldShell shell;
	final Matrix3D m;
	final Interval rInterval = new Interval();
	final Interval interval = new Interval();

	public ShellCollisionHull(WorldShell shell, double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
		m = new Matrix3D();
		this.shell = shell;
	}

	public void setRotation(Quaternion quaternion) {
		quaternion.conjugate();
		m.fromQuaternion(quaternion);
		quaternion.conjugate();
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (super.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
			double xPos = (minX - maxX) / 2.0;
			double yPos = (minY - maxY) / 2.0;
			double zPos = (minZ - maxZ) / 2.0;
			double xSize = (maxX - minX) / 2.0;
			double ySize = (maxY - minY) / 2.0;
			double zSize = (maxZ - minZ) / 2.0;

			BlockPos.stream(rotatedBox).forEach(bp ->
					shell.getBlockState(bp).getCollisionShape(shell, bp).forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) ->
					));
		}
		return false;
	}

	private void setInterval(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vec3d axis) {
		interval.setBoth    (dot(minX, maxY, maxZ, axis));
		interval.addIfExceed(dot(minX, maxY, minZ, axis));
		interval.addIfExceed(dot(minX, minY, maxZ, axis));
		interval.addIfExceed(dot(minX, minY, minZ, axis));
		interval.addIfExceed(dot(maxX, maxY, maxZ, axis));
		interval.addIfExceed(dot(maxX, maxY, minZ, axis));
		interval.addIfExceed(dot(maxX, minY, maxZ, axis));
		interval.addIfExceed(dot(maxX, minY, minZ, axis));
	}

	private void setRInterval(double xP, double yP, double zP, double xS, double yS, double zS, Vec3d a) {
		double x00 = m.m00 * xS;
		double x10 = m.m10 * xS;
		double x20 = m.m20 * xS;
		double y01 = m.m01 * yS;
		double y11 = m.m11 * yS;
		double y21 = m.m21 * yS;
		double z02 = m.m02 * zS;
		double z12 = m.m12 * zS;
		double z22 = m.m22 * zS;
		rInterval.setBoth(dot(xP + x00 + x01 + x02, yP + y10 + y11 + y12, zP + z00 + z01 + z02));
	}

	private double dot(double x, double y, double z, Vec3d axis) {
		return x * axis.x + y * axis.y + z * axis.z;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return super.contains(x, y, z);
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		return super.raycast(min, max);
	}

	private static class Interval {
		double max, min;

		public void setBoth(double val) {
			max = val;
			min = val;
		}

		public void addIfExceed(double proj) {
			if (proj > max) max = proj;
			if (proj < min) min = proj;
		}
	}
}
