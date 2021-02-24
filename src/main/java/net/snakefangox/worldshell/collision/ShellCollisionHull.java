package net.snakefangox.worldshell.collision;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.util.CoordUtil;

import java.util.Optional;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.<p>
 * Some <s>slightly</s> <s><i><b>very<b/></i></s> <p> <h3>INCREDIBLY</h3> questionable decisions are made
 * to optimise the collision calculations, you peer inside at your own risk.
 */
public class ShellCollisionHull extends Box {

	private final WorldLinkEntity entity;
	private QuaternionD rotation;
	private Matrix3d matrix;
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
		rotation = Matrix3d.IDENTITY;
		for (int i = 0; i < 15; ++i) collisionAxis[i] = new Vec3dM();
		collisionAxis[0].setAll(1, 0, 0);
		collisionAxis[1].setAll(0, 1, 0);
		collisionAxis[2].setAll(0, 0, 1);
		setRotation(QuaternionD.IDENTITY);
		calcAllAxis();
	}

	public void setRotation(QuaternionD q) {
		rotation = new QuaternionD(-q.getX(), -q.getY(), -q.getZ(), q.getW());
		matrix = new Matrix3d(rotation);
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
			Vec3d min = matrix.transform(aabbMin.x, aabbMin.y, aabbMin.z);
			Vec3d max = matrix.transform(aabbMax.x, aabbMax.y, aabbMax.z);
			OrientedBox collidingBox = new OrientedBox(matrix.transform(xPos, yPos, zPos), new Vec3d(xSize, ySize, zSize), rotation);
		}
		return false;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		pos.setAll(x, y, z);
		CoordUtil.worldToLinkEntity(entity, pos);
		rotation.rotate(pos);
		BlockPos bp = new BlockPos(pos.x, pos.y, pos.z);
		VoxelShape shape = entity.getWorldShell().getBlockState(bp).getCollisionShape(entity.getWorldShell(), bp);
		if (shape.isEmpty()) return false;
		return shape.getBoundingBox().contains(pos.x, pos.y, pos.z);
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3d nMin = rotation.rotate(CoordUtil.worldToLinkEntity(entity, min));
		Vec3d nMax = rotation.rotate(CoordUtil.worldToLinkEntity(entity, max));
		RaycastContext ctx = new RaycastContext(nMin, nMax, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getWorldShell().raycast(ctx);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(CoordUtil.linkEntityToWorld(CoordUtil.BP_ZERO, entity, hit.getPos()));
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
		collisionAxis[3].setAll(rotation.m00, rotation.m10, rotation.m20);
		collisionAxis[4].setAll(rotation.m01, rotation.m11, rotation.m21);
		collisionAxis[5].setAll(rotation.m02, rotation.m12, rotation.m22);
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
		double x0 = rotation.m00 * xS;
		double y0 = rotation.m10 * xS;
		double z0 = rotation.m20 * xS;
		double x1 = rotation.m01 * yS;
		double y1 = rotation.m11 * yS;
		double z1 = rotation.m21 * yS;
		double x2 = rotation.m02 * zS;
		double y2 = rotation.m12 * zS;
		double z2 = rotation.m22 * zS;
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
