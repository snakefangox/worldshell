package net.snakefangox.worldshell.collision;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.*;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;
import net.snakefangox.worldshell.util.CoordUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.<p>
 * Some <s>slightly</s> <s><i><b>very<b/></i></s> <p> <h3>INCREDIBLY</h3> questionable decisions are made
 * to optimise the collision calculations, you peer inside at your own risk.
 */
public class ShellCollisionHull extends Box {

	private final WorldLinkEntity entity;
	private final Matrix3D m;
	// Very bad not good probably evil mutable global vars
	private final Interval rInterval = new Interval();
	private final Interval interval = new Interval();
	private final Vec3dM[] collisionAxis = new Vec3dM[15];
	private final Vec3dM aabbMax = new Vec3dM();
	private final Vec3dM aabbMin = new Vec3dM();
	private final Vec3dM temp = new Vec3dM();
	private final Finished finished = new Finished();

	public ShellCollisionHull(WorldLinkEntity entity) {
		super(0, 0, 0, 0, 0, 0);
		this.entity = entity;
		m = new Matrix3D();
		Arrays.fill(collisionAxis, new Vec3dM());
		collisionAxis[0].setAll(1, 0, 0);
		collisionAxis[1].setAll(0, 1, 0);
		collisionAxis[2].setAll(0, 0, 1);
	}

	public void setRotation(Quaternion quaternion) {
		quaternion.conjugate();
		m.fromQuaternion(quaternion);
		quaternion.conjugate();
		calcAllAxis();
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (checkCrudeIntersect(minX, minY, minZ, maxX, maxY, maxZ)) {
			aabbMin.setAll(minX, minY, minZ);
			aabbMax.setAll(maxX, maxY, maxZ);
			CoordUtil.worldToLinkEntity(entity, aabbMin);
			CoordUtil.worldToLinkEntity(entity, aabbMax);
			m.rotate(aabbMin);
			m.rotate(aabbMax);
			double xPos = (aabbMin.x - aabbMax.x) / 2.0;
			double yPos = (aabbMin.y - aabbMax.y) / 2.0;
			double zPos = (aabbMin.z - aabbMax.z) / 2.0;
			double xSize = (aabbMax.x - aabbMin.x) / 2.0;
			double ySize = (aabbMax.y - aabbMin.y) / 2.0;
			double zSize = (aabbMax.z - aabbMin.z) / 2.0;
			calcNewAABB(aabbMin.x, aabbMin.y, aabbMin.z, aabbMax.x, aabbMax.y, aabbMax.z);

			WorldShell shell = entity.getWorldShell();
			BlockPos.stream(MathHelper.floor(aabbMin.x), MathHelper.floor(aabbMin.y), MathHelper.floor(aabbMin.z),
					MathHelper.ceil(aabbMax.x), MathHelper.ceil(aabbMax.y), MathHelper.ceil(aabbMax.z)).forEach(bp ->
					shell.getBlockState(bp).getCollisionShape(shell, bp).forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) -> {
						finished.f = checkAllAxisForOverlap(minX1, minY1, minZ1, maxX1, maxY1, maxZ1, xPos, yPos, zPos, xSize, ySize, zSize);
					}));
			if (finished.f) {
				finished.f = false;
				return true;
			}
		}
		return false;
	}

	public boolean checkCrudeIntersect(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		EntityDimensions entityDimensions = entity.getDimensions(null);
		double lMinX = entity.getX() - entityDimensions.width;
		double lMinY = entity.getY() - entityDimensions.height;
		double lMinZ = entity.getZ() - entityDimensions.width;
		double lMaxX = entity.getX() + entityDimensions.width;
		double lMaxY = entity.getY() + entityDimensions.height;
		double lMaxZ = entity.getZ() + entityDimensions.width;
		return lMinX < maxX && lMaxX > minX && lMinY < maxY && lMaxY > minY && lMinZ < maxZ && lMaxZ > minZ;
	}

	private boolean checkAllAxisForOverlap(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
										   double xP, double yP, double zP, double xS, double yS, double zS) {
		for (int i = 0; i < 15; ++i) {
			if (!doesOverlap(minX, minY, minZ, maxX, maxY, maxZ, xP, yP, zP, xS, yS, zS, collisionAxis[i])) {
				return false;
			}
		}
		return true;
	}

	private void calcAllAxis() {
		collisionAxis[3].setAll(m.m00, m.m10, m.m20);
		collisionAxis[4].setAll(m.m01, m.m11, m.m21);
		collisionAxis[5].setAll(m.m02, m.m12, m.m22);
		//TODO this may be wrong, check if their version works
		for (int i = 0; i < 3; ++i) {
			cross(collisionAxis[i + 3], collisionAxis[0], collisionAxis[6 + i * 3]);
			cross(collisionAxis[i + 3], collisionAxis[1], collisionAxis[6 + i * 3 + 1]);
			cross(collisionAxis[i + 3], collisionAxis[2], collisionAxis[6 + i * 3 + 2]);
		}
	}

	private boolean doesOverlap(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
								double xP, double yP, double zP, double xS, double yS, double zS, Vec3dM axis) {
		setInterval(minX, minY, minZ, maxX, maxY, maxZ, axis);
		setRInterval(xP, yP, zP, xS, yS, zS, axis);
		return rInterval.min <= interval.max && interval.min <= rInterval.max;
	}

	private void setInterval(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vec3dM axis) {
		interval.setBoth(dot(minX, maxY, maxZ, axis));
		interval.addWFit(dot(minX, maxY, minZ, axis));
		interval.addWFit(dot(minX, minY, maxZ, axis));
		interval.addWFit(dot(minX, minY, minZ, axis));
		interval.addWFit(dot(maxX, maxY, maxZ, axis));
		interval.addWFit(dot(maxX, maxY, minZ, axis));
		interval.addWFit(dot(maxX, minY, maxZ, axis));
		interval.addWFit(dot(maxX, minY, minZ, axis));
	}

	private void setRInterval(double xP, double yP, double zP, double xS, double yS, double zS, Vec3dM axis) {
		// Look I just met you, and this is crazy
		// But matrix may be the wrong way around
		// So FIXME maybe?
		double x0 = m.m00 * xS;
		double y0 = m.m10 * xS;
		double z0 = m.m20 * xS;
		double x1 = m.m01 * yS;
		double y1 = m.m11 * yS;
		double z1 = m.m21 * yS;
		double x2 = m.m02 * zS;
		double y2 = m.m12 * zS;
		double z2 = m.m22 * zS;
		// TODO Consider caching all of this
		// Also TODO don't fucking do that it's 3 additions
		rInterval.setBoth(dot(xP + x0 + x1 + x2, yP + y0 + y1 + y2, zP + z0 + z1 + z2, axis));
		rInterval.addWFit(dot(xP - x0 + x1 + x2, yP - y0 + y1 + y2, zP - z0 + z1 + z2, axis));
		rInterval.addWFit(dot(xP + x0 - x1 + x2, yP + y0 - y1 + y2, zP + z0 - z1 + z2, axis));
		rInterval.addWFit(dot(xP + x0 + x1 - x2, yP + y0 + y1 - y2, zP + z0 + z1 - z2, axis));
		rInterval.addWFit(dot(xP - x0 - x1 - x2, yP - y0 - y1 - y2, zP - z0 - z1 - z2, axis));
		rInterval.addWFit(dot(xP + x0 - x1 - x2, yP + y0 - y1 - y2, zP + z0 - z1 - z2, axis));
		rInterval.addWFit(dot(xP - x0 + x1 - x2, yP - y0 + y1 - y2, zP - z0 + z1 - z2, axis));
		rInterval.addWFit(dot(xP - x0 - x1 + x2, yP - y0 - y1 + y2, zP - z0 - z1 + z2, axis));
	}

	private void calcNewAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		aabbMax.setAll(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		aabbMin.setAll(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		temp.setAll(minX, maxY, maxZ);
		checkExtents(temp);
		temp.setAll(minX, maxY, minZ);
		checkExtents(temp);
		temp.setAll(minX, minY, maxZ);
		checkExtents(temp);
		temp.setAll(minX, minY, minZ);
		checkExtents(temp);
		temp.setAll(maxX, maxY, maxZ);
		checkExtents(temp);
		temp.setAll(maxX, maxY, minZ);
		checkExtents(temp);
		temp.setAll(maxX, minY, maxZ);
		checkExtents(temp);
		temp.setAll(maxX, minY, minZ);
		checkExtents(temp);
	}

	private void checkExtents(Vec3dM vec) {
		if (vec.x > aabbMax.x) aabbMax.x = vec.x;
		if (vec.y > aabbMax.y) aabbMax.y = vec.y;
		if (vec.z > aabbMax.z) aabbMax.z = vec.z;
		if (vec.x < aabbMin.x) aabbMin.x = vec.x;
		if (vec.y < aabbMin.y) aabbMin.y = vec.y;
		if (vec.z < aabbMin.z) aabbMin.z = vec.z;
	}

	private double dot(double x, double y, double z, Vec3dM axis) {
		return x * axis.x + y * axis.y + z * axis.z;
	}

	public void cross(Vec3dM first, Vec3dM second, Vec3dM dest) {
		dest.setAll(first.y * second.z - first.z * second.y, first.z * second.x - first.x * second.z, first.x * second.y - first.y * second.x);
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

		public void addWFit(double proj) {
			if (proj > max) max = proj;
			if (proj < min) min = proj;
		}
	}

	public static class Vec3dM {
		public double x, y, z;

		public void setAll(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static class Finished {
		boolean f = false;
	}
}
