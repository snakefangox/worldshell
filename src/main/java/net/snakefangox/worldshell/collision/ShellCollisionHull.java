package net.snakefangox.worldshell.collision;

import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.LocalSpace;
import net.snakefangox.worldshell.util.VectorPool;
import oimo.collision.geometry.Aabb;
import oimo.collision.geometry.BoxGeometry;
import oimo.collision.geometry.RayCastHit;
import oimo.common.Mat3;
import oimo.common.Transform;
import oimo.common.Vec3;
import oimo.dynamics.World;
import oimo.dynamics.callback.AabbTestCallback;
import oimo.dynamics.callback.RayCastCallback;
import oimo.dynamics.rigidbody.*;

import java.util.Optional;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.
 */
public class ShellCollisionHull extends Box implements SpecialBox, LocalSpace {

	private static final RigidBodyConfig BODY_CONFIG = new RigidBodyConfig();
	private static final float PADDING = 0.5F;
	private final WorldShellEntity entity;
	private final World physicsWorld;
	private final RigidBody hull;
	private Mat3 matrix;
	private Mat3 inverseMatrix;

	public ShellCollisionHull(WorldShellEntity entity) {
		super(0, 0, 0, 0, 0, 0);
		this.entity = entity;
		physicsWorld = new World(null, new Vec3(null, null, null));
		hull = new RigidBody(BODY_CONFIG);
		physicsWorld.addRigidBody(hull);
		setRotation(RotationHelper.identityMat3(), RotationHelper.identityMat3());
	}

	public void setRotation(Mat3 rotationMatrix, Mat3 inverseRotationMatrix) {
		matrix = rotationMatrix;
		inverseMatrix = inverseRotationMatrix;
		hull.setRotation(rotationMatrix);
	}

	public void sizeUpdate() {
		calculateCrudeBounds();
	}

	public void calculateCrudeBounds() {
		EntityBounds ed = entity.getDimensions();
		double xBy2 = ed.length / 2.0;
		double yBy2 = ed.height / 2.0;
		double zBy2 = ed.width / 2.0;
		Box aligned = new Box(-xBy2, -yBy2, -zBy2, xBy2, yBy2, zBy2).expand(PADDING);
		Vec3 vec = VectorPool.vec3();
		Box rotated = RotationHelper.transformBox(aligned, matrix, vec);
		VectorPool.disposeVec3(vec);
		minX = rotated.minX + entity.getX();
		minY = rotated.minY + entity.getY();
		minZ = rotated.minZ + entity.getZ();
		maxX = rotated.maxX + entity.getX();
		maxY = rotated.maxY + entity.getY();
		maxZ = rotated.maxZ + entity.getZ();
	}

	@Override
	public boolean intersects(Box box) {
		return box.intersects(minX, minY, minZ, maxX, maxY, maxZ) && this.intersects(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (!super.intersects(minX, minY, minZ, maxX, maxY, maxZ)) return false;
		Vec3 vecMin = toLocal(new Vec3(minX, minY, minZ));
		Vec3 vecMax = toLocal(new Vec3(maxX, maxY, maxZ));
		Aabb aabb = new Aabb();
		aabb.setMax(vecMax);
		aabb.setMin(vecMin);
		AabbTest result = new AabbTest();
		fillHull(aabb.getCenter(), new Box(vecMin.x, vecMin.y, vecMin.z, vecMax.x, vecMax.y, vecMax.z));
		physicsWorld.aabbTest(aabb, result);
		clearHull();
		return result.hasCollided;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		Vec3 vec = entity.toLocal(VectorPool.vec3().init(x, y, z)).mulMat3Eq(inverseMatrix);
		BlockPos bp = new BlockPos(vec.x, vec.y, vec.z);
		VoxelShape shape = entity.getMicrocosm().getBlockState(bp).getCollisionShape(entity.getMicrocosm(), bp);
		if (shape.isEmpty()) {
			VectorPool.disposeVec3(vec);
			return false;
		} else {
			boolean r = shape.getBoundingBox().contains(vec.x, vec.y, vec.z);
			VectorPool.disposeVec3(vec);
			return r;
		}
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3 nMin = entity.toLocal(RotationHelper.of(min)).mulMat3Eq(inverseMatrix);
		Vec3 nMax = entity.toLocal(RotationHelper.of(max)).mulMat3Eq(inverseMatrix);
		RaycastContext ctx = new RaycastContext(RotationHelper.of(nMin), RotationHelper.of(nMax),
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getMicrocosm().raycast(ctx);
		VectorPool.disposeVec3(nMin);
		VectorPool.disposeVec3(nMax);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(entity.toGlobal(hit.getPos()));
	}

	public VoxelShape toVoxelShape() {
		return new HullVoxelDelegate(this);
	}

	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		Vec3d center = box.getCenter();
		Vec3 vec = RotationHelper.of(center);
		switch (axis) {
			case X -> vec.x += maxDist;
			case Y -> vec.y += maxDist;
			case Z -> vec.z += maxDist;
		}

		DistTracker hit = castHullForCollision(box, vec);
		VectorPool.disposeVec3(vec);
		return MathHelper.lerp(hit.progress, 0, maxDist);
	}

	private DistTracker castHullForCollision(Box collidingBox, Vec3 end) {
		Vec3d c = collidingBox.getCenter();
		Vec3 vec3 = VectorPool.vec3().init(-entity.getLocalX(), -entity.getLocalY(), -entity.getLocalZ());
		Box rot = RotationHelper.transformBox(collidingBox, inverseMatrix, vec3);
		BoxGeometry collider = new BoxGeometry(vec3.init(rot.getXLength() / 2.0, rot.getYLength() / 2.0, rot.getZLength() / 2.0));
		Transform collTransform = new Transform();
		collTransform.setPosition(entity.toLocal(vec3.init(c.x, c.y, c.z)));

		fillHull(vec3, rot);

		DistTracker distTracker = new DistTracker();
		physicsWorld.convexCast(collider, collTransform, end, distTracker);

		clearHull();
		return distTracker;
	}

	private void fillHull(Vec3 vec3, Box rot) {
		CuboidBlockIterator bi = new CuboidBlockIterator((int) rot.minX, (int) rot.minY, (int) rot.minZ,
				(int) rot.maxX, (int) rot.maxY, (int) rot.maxZ);
		BlockPos.Mutable mBp = new BlockPos.Mutable();
		while (bi.step()) {
			mBp.set(bi.getX(), bi.getY(), bi.getZ());
			VoxelShape vShape = entity.getMicrocosm().getBlockState(mBp).getCollisionShape(entity.getMicrocosm(), mBp);
			vShape.forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) -> {
				double hX = (maxX1 - minX1) / 2.0;
				double hY = (maxY1 - minY1) / 2.0;
				double hZ = (maxZ1 - minZ1) / 2.0;
				BoxGeometry boxGeom = new BoxGeometry(vec3.init(hX, hY, hZ));
				ShapeConfig config = new ShapeConfig();
				config.geometry = boxGeom;
				config.position.init(mBp.getX() + minX1 + hX, mBp.getY() + minY1 + hY, mBp.getZ() + minZ1 + hZ);
				Shape shape = new Shape(config);
				hull.addShape(shape);
			});
		}
	}

	private void clearHull() {
		hull._shapeList = null;
		hull._shapeListLast = null;
		hull._numShapes = 0;
	}

	static {
		BODY_CONFIG.type = RigidBodyType.STATIC;
	}

	@Override
	public double getLocalX() {
		return entity.getLocalX();
	}

	@Override
	public double getLocalY() {
		return entity.getLocalY();
	}

	@Override
	public double getLocalZ() {
		return entity.getLocalZ();
	}

	private static class DistTracker extends RayCastCallback {
		double progress;
		boolean hasCollided;

		@Override
		public void process(Shape shape, RayCastHit hit) {
			hasCollided = true;
			if (hit.fraction > progress) progress = hit.fraction;
		}
	}

	private static class AabbTest extends AabbTestCallback {
		boolean hasCollided;

		@Override
		public void process(Shape shape) {
			hasCollided = true;
		}
	}
}
