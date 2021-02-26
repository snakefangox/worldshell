package net.snakefangox.worldshell.collision;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;
import net.snakefangox.worldshell.util.CoordUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.<p>
 * Some <s>slightly</s> <s><i><b>very<b/></i></s> <p> <h3>INCREDIBLY</h3> questionable decisions are made
 * to optimise the collision calculations, you peer inside at your own risk.
 */
public class ShellCollisionHull extends Box implements SpecialBox {

	private static final double RESOLUTION = 4;
	private final WorldLinkEntity entity;
	private QuaternionD rotation;
	private Matrix3d matrix;
	private Matrix3d inverseMatrix;
	private VoxelShape cachedVoxelShape;
	// Very bad not good probably evil mutable global vars
	private final Vec3dM aabbMax = new Vec3dM();
	private final Vec3dM aabbMin = new Vec3dM();
	private final Vec3dM pos = new Vec3dM();
	private final Vec3dM temp = new Vec3dM();

	public ShellCollisionHull(WorldLinkEntity entity) {
		super(0, 0, 0, 0, 0, 0);
		this.entity = entity;
		setRotation(QuaternionD.IDENTITY);
	}

	public void setRotation(QuaternionD q) {
		matrix = new Matrix3d(q);
		rotation = new QuaternionD(-q.getX(), -q.getY(), -q.getZ(), q.getW());
		inverseMatrix = new Matrix3d(rotation);
	}

	public void sizeUpdate() {
		cachedVoxelShape = null;
		calculateCrudeBounds();
	}

	public void calculateCrudeBounds() {
		EntityBounds ed = entity.getDimensions();
		float len = ed.length * 0.5F;
		float width = ed.width * 0.5F;
		Vec3d min = matrix.transform(-len, 0, -width);
		Vec3d max = matrix.transform(len, ed.height, width);
		calcNewAABB(min.x, min.y, min.z, max.x, max.y, max.z);
		minX = aabbMin.x + entity.getX();
		minY = aabbMin.y + entity.getY();
		minZ = aabbMin.z + entity.getZ();
		maxX = aabbMax.x + entity.getX();
		maxY = aabbMax.y + entity.getY();
		maxZ = aabbMax.z + entity.getZ();
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
			Vec3d min = inverseMatrix.transform(aabbMin.x, aabbMin.y, aabbMin.z);
			Vec3d max = inverseMatrix.transform(aabbMax.x, aabbMax.y, aabbMax.z);
			calcNewAABB(min.x, min.y, min.z, max.x, max.y, max.z);
			OrientedBox collidingBox = new OrientedBox(inverseMatrix.transform(xPos, yPos, zPos), new Vec3d(xSize, ySize, zSize), rotation);
			BlockPos.Mutable bp = new BlockPos.Mutable();
			Box box = new Box(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
			WorldShell shell = entity.getWorldShell();
			int xLimit = (int) Math.ceil(aabbMax.x);
			int yLimit = (int) Math.ceil(aabbMax.y);
			int zLimit = (int) Math.ceil(aabbMax.z);
			AtomicBoolean collides = new AtomicBoolean(false);
			for (int x = (int) aabbMin.x; x <= xLimit; ++x) {
				for (int y = (int) aabbMin.y; y <= yLimit; ++y) {
					for (int z = (int) aabbMin.z; z <= zLimit; ++z) {
						bp.set(x, y, z);
						shell.getBlockState(bp).getCollisionShape(shell, bp).forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) -> {
							setBox(box, minX1 + bp.getX(), minY1 + bp.getY(), minZ1 + bp.getZ(),
									maxX1 + bp.getX(), maxY1 + bp.getY(), maxZ1 + bp.getZ());
							collides.set(collides.get() || collidingBox.intersects(box));
						});
						if (collides.get()) return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		pos.setAll(x, y, z);
		CoordUtil.worldToLinkEntity(entity, pos);
		BlockPos bp = new BlockPos(inverseMatrix.transform(pos.x, pos.y, pos.z));
		VoxelShape shape = entity.getWorldShell().getBlockState(bp).getCollisionShape(entity.getWorldShell(), bp);
		if (shape.isEmpty()) return false;
		return shape.getBoundingBox().contains(pos.x, pos.y, pos.z);
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3d nMin = inverseMatrix.transform(CoordUtil.worldToLinkEntity(entity, min));
		Vec3d nMax = inverseMatrix.transform(CoordUtil.worldToLinkEntity(entity, max));
		RaycastContext ctx = new RaycastContext(nMin, nMax, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getWorldShell().raycast(ctx);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(CoordUtil.linkEntityToWorld(CoordUtil.BP_ZERO, entity, hit.getPos()));
	}

	private void setBox(Box box, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		box.minX = minX;
		box.minY = minY;
		box.minZ = minZ;
		box.maxX = maxX;
		box.maxY = maxY;
		box.maxZ = maxZ;
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

	public VoxelShape toVoxelShape() {
		return new HullVoxelDelegate(this);
	}

	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		Vec3d min = CoordUtil.worldToLinkEntity(entity, box.minX, box.minY, box.minZ);
		Vec3d max = CoordUtil.worldToLinkEntity(entity, box.maxX, box.maxY, box.maxZ);
		calcNewAABB(min.x, min.y, min.z, max.x, max.y, max.z);
		Box localBox = new Box(aabbMin.x, aabbMin.y, aabbMin.z, aabbMax.x, aabbMax.y, aabbMax.z);
		return VoxelShapes.calculateMaxOffset(axis, localBox,
				StreamSupport.stream(new ShellCollisionSpliterator(entity.getWorldShell().getProxyWorld(), localBox.expand(1)), false), maxDist);
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
}
