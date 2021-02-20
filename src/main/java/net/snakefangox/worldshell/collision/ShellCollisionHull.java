package net.snakefangox.worldshell.collision;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;
import net.snakefangox.worldshell.util.CoordUtil;

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
	private final Vec3dM pos = new Vec3dM();
	private final Vec3dM temp = new Vec3dM();
	private final Finished finished = new Finished();

	public ShellCollisionHull(WorldLinkEntity entity) {
		super(0, 0, 0, 0, 0, 0);
		this.entity = entity;
		m = new Matrix3D();
		for (int i = 0; i < 15; ++i) collisionAxis[i] = new Vec3dM();
		collisionAxis[0].setAll(1, 0, 0);
		collisionAxis[1].setAll(0, 1, 0);
		collisionAxis[2].setAll(0, 0, 1);
		setRotation(Quaternion.IDENTITY.copy());
		calcAllAxis();
	}

	public void setRotation(Quaternion quaternion) {
		quaternion.conjugate();
		m.fromQuaternion(quaternion);
		quaternion.conjugate();
		calcAllAxis();
	}

	public void calculateCrudeBounds() {
		EntityDimensions entityDimensions = entity.getDimensions(null);
		minX = entity.getX() - entityDimensions.width;
		minY = entity.getY() - entityDimensions.height;
		minZ = entity.getZ() - entityDimensions.width;
		maxX = entity.getX() + entityDimensions.width;
		maxY = entity.getY() + entityDimensions.height;
		maxZ = entity.getZ() + entityDimensions.width;
	}


	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (super.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
			aabbMin.setAll(minX, minY, minZ);
			aabbMax.setAll(maxX, maxY, maxZ);
			CoordUtil.worldToLinkEntity(entity, aabbMin);
			CoordUtil.worldToLinkEntity(entity, aabbMax);
			double xSize = (aabbMax.x - aabbMin.x) / 2.0;
			double ySize = (aabbMax.y - aabbMin.y) / 2.0;
			double zSize = (aabbMax.z - aabbMin.z) / 2.0;
			double xPos = aabbMin.x + xSize;
			double yPos = aabbMin.y + ySize;
			double zPos = aabbMin.z + zSize;
			m.rotate(xPos, yPos, zPos, pos);
			m.rotate(aabbMin);
			m.rotate(aabbMax);
			calcNewAABB(aabbMin.x, aabbMin.y, aabbMin.z, aabbMax.x, aabbMax.y, aabbMax.z);

			WorldShell shell = entity.getWorldShell();
			/*BlockPos.stream(MathHelper.floor(aabbMin.x), MathHelper.floor(aabbMin.y), MathHelper.floor(aabbMin.z),
					MathHelper.ceil(aabbMax.x), MathHelper.ceil(aabbMax.y), MathHelper.ceil(aabbMax.z))*/
			shell.getBlocks().forEach(entry -> {
				BlockPos bp = entry.getKey();
				if (!finished.f) {
					BlockState state = shell.getBlockState(bp);
					if (!state.isAir())
						state.getCollisionShape(shell, bp).forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) ->
								finished.f = finished.f || checkAllAxisForOverlap(minX1 + bp.getX(), minY1 + bp.getY(), minZ1 + bp.getZ(),
										maxX1 + bp.getX(), maxY1 + bp.getY(), maxZ1 + bp.getZ(), pos.x, pos.y, pos.z, xSize, ySize, zSize));
				}
			});
			if (finished.f) {
				finished.f = false;
				return true;
			}
		}
		return false;
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
		for (int i = 0; i < 3; ++i) {
			cross(collisionAxis[i], collisionAxis[3], collisionAxis[6 + i * 3]);
			cross(collisionAxis[i], collisionAxis[4], collisionAxis[7 + i * 3]);
			cross(collisionAxis[i], collisionAxis[5], collisionAxis[8 + i * 3]);
		}
	}

	private boolean doesOverlap(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
								double xP, double yP, double zP, double xS, double yS, double zS, Vec3dM axis) {
		setInterval(minX, minY, minZ, maxX, maxY, maxZ, axis);
		setRInterval(xP, yP, zP, xS, yS, zS, axis);
		return (rInterval.min <= interval.max) && (interval.min <= rInterval.max);
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
		double x0 = m.m00 * xS;
		double y0 = m.m10 * xS;
		double z0 = m.m20 * xS;
		double x1 = m.m01 * yS;
		double y1 = m.m11 * yS;
		double z1 = m.m21 * yS;
		double x2 = m.m02 * zS;
		double y2 = m.m12 * zS;
		double z2 = m.m22 * zS;
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
		return (x * axis.x) + (y * axis.y) + (z * axis.z);
	}

	public void cross(Vec3dM l, Vec3dM r, Vec3dM dest) {
		dest.x = l.y * r.z - l.z * r.y;
		dest.y = l.z * r.x - l.x * r.z;
		dest.z = l.x * r.y - l.y * r.x;
	}

	@Override
	public Box shrink(double x, double y, double z) {
		return new HullDelegate(super.shrink(x, y, z), this);
	}

	@Override
	public Box stretch(double x, double y, double z) {
		return new HullDelegate(super.stretch(x, y, z), this);
	}

	@Override
	public Box expand(double x, double y, double z) {
		return new HullDelegate(super.expand(x, y, z), this);
	}

	@Override
	public Box intersection(Box box) {
		return new HullDelegate(super.intersection(box), this);
	}

	@Override
	public Box union(Box box) {
		return new HullDelegate(super.union(box), this);
	}

	@Override
	public Box offset(double x, double y, double z) {
		return new HullDelegate(super.offset(x, y, z), this);
	}

	@Override
	public Box offset(BlockPos blockPos) {
		return new HullDelegate(super.offset(blockPos), this);
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

		@Override
		public String toString() {
			return "Interval{" +
					"max=" + max +
					", min=" + min +
					'}';
		}
	}

	public static class Vec3dM {
		public double x, y, z;

		public void setAll(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String toString() {
			return "Vec3dM{" +
					"x=" + x +
					", y=" + y +
					", z=" + z +
					'}';
		}
	}

	public static class Finished {
		boolean f = false;

		@Override
		public String toString() {
			return "Finished{" +
					"f=" + f +
					'}';
		}
	}
}
