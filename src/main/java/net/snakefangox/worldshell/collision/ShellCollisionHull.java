package net.snakefangox.worldshell.collision;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsSweepTestResult;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsGhostObject;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.LocalSpace;
import net.snakefangox.worldshell.storage.Microcosm;

import java.util.*;

/**
 * A custom {@link Box} style implementation that takes a worldshell and handles rotated collision.
 */
public class ShellCollisionHull implements LocalSpace {

	private static final double SMOL = 0.01;
	private static final Map<Vector3f, BoxCollisionShape> SHAPE_CACHE = new HashMap<>();
	private final WorldShellEntity entity;
	private HullBoxDelegate dBox;
	private final Vector3f localVector = new Vector3f(), localVector2 = new Vector3f(), localVector3 = new Vector3f();
	private final Transform start = new Transform(), end = new Transform();
	private final List<PhysicsSweepTestResult> sweepTestResults = new ArrayList<>();
	private final BoxCollisionShape roughHullShape = new BoxCollisionShape(Vector3f.UNIT_XYZ);
	private final CollisionSpace space;
	private final PhysicsGhostObject shellBody;
	private CompoundCollisionShape compoundHull;
	private final Set<Long> loadedPositions = new HashSet<>();
	private final BoxCollisionShape collider = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
	private final PhysicsGhostObject colliderBody = new PhysicsGhostObject(collider);

	public ShellCollisionHull(WorldShellEntity entity) {
		this.entity = entity;
		space = new CollisionSpace(localVector.set(10000f, 10000f, 10000f),
				localVector2.set(-10000f, -10000f, -10000f), PhysicsSpace.BroadphaseType.SIMPLE);
		compoundHull = new CompoundCollisionShape();
		shellBody = new PhysicsGhostObject(compoundHull);
		space.add(shellBody);
		space.add(colliderBody);
		dBox = new HullBoxDelegate(new Box(BlockPos.ORIGIN), this);
	}

	public void calculateCrudeBounds() {
		EntityBounds bounds = entity.getDimensions();
		float len = bounds.length / 2f;
		float height = bounds.height / 2f;
		float width = bounds.width / 2f;
		Vec3d off = entity.getBlockOffset();
		localVector.set(len, height, width);
		roughHullShape.setScale(localVector);
		localVector.set((float) off.x + (float) entity.getX() + len,
				(float) off.y + (float) entity.getY(),
				(float) off.z + (float) entity.getZ() + width);
		BoundingBox box = new BoundingBox();
		roughHullShape.boundingBox(localVector, getRotation(), box);
		box.getMax(localVector);
		double maxX = localVector.x;
		double maxY = localVector.y;
		double maxZ = localVector.z;
		box.getMin(localVector);
		dBox = new HullBoxDelegate(new Box(localVector.x, localVector.y, localVector.z, maxX, maxY, maxZ), this);
		shellBody.setPhysicsRotation(getRotation());
	}

	public void onWorldshellUpdate() {
		compoundHull = new CompoundCollisionShape(compoundHull.countChildren() == 0 ? 1 : compoundHull.countChildren());
		shellBody.setCollisionShape(compoundHull);
		loadedPositions.clear();
	}

	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		localVector2.set((float) (maxX - minX) / 2f, (float) (maxY - minY) / 2f, (float) (maxZ - minZ) / 2f);
		localVector.set((float) minX + localVector2.x, (float) minY + localVector2.y, (float) minZ + localVector2.z);
		lazyFillCollisionSpace(toLocal(localVector), localVector2);

		collider.setScale(localVector2);
		colliderBody.setPhysicsLocation(localVector);
		return space.contactTest(colliderBody, null) > 0;
	}

	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		double absMax = Math.abs(maxDist);
		if (absMax < SMOL) return 0;
		int sign = (int) Math.signum(maxDist);
		double sweepVal = (0.4 * sign);
		double sweepMaxDist = maxDist + sweepVal;
		float maxX = (float) Math.abs(axis.choose(sweepMaxDist, 0, 0));
		float maxY = (float) Math.abs(axis.choose(0, sweepMaxDist, 0));
		float maxZ = (float) Math.abs(axis.choose(0, 0, sweepMaxDist));

		localVector2.set((float) (box.maxX - box.minX) / 2f, (float) (box.maxY - box.minY) / 2f, (float) (box.maxZ - box.minZ) / 2f);
		localVector.set((float) (box.minX + localVector2.x), (float) (box.minY + localVector2.y), (float) (box.minZ + localVector2.z));
		localVector2.addLocal(maxX, maxY, maxZ);
		lazyFillCollisionSpace(toLocal(localVector), localVector2);
		localVector2.subtractLocal(maxX, maxY, maxZ);

		collider.setScale(localVector2);
		start.setTranslation(localVector);
		end.setTranslation(localVector.addLocal(maxX * sign, maxY * sign, maxZ * sign));
		space.sweepTest(collider, start, end, sweepTestResults, (float) SMOL);

		if (sweepTestResults.isEmpty()) return maxDist;
		double result = sweepTestResults.get(0).getHitFraction() * sweepMaxDist;
		double absResult = Math.abs(result);
		if (absResult > absMax) return maxDist;
		if (absResult < SMOL) return 0;
		return result;
	}

	public boolean contains(double x, double y, double z) {
		localVector.set((float) x, (float) y, (float) z);
		toLocal(localVector);
		BlockPos bp = new BlockPos(localVector.x, localVector.y, localVector.z);
		VoxelShape shape = entity.getMicrocosm().getBlockState(bp).getCollisionShape(entity.getMicrocosm(), bp);
		if (shape.isEmpty()) return false;
		return shape.getBoundingBox().contains(localVector.x, localVector.y, localVector.z);
	}

	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3d nMin = toLocal(min);
		Vec3d nMax = toLocal(max);
		RaycastContext ctx = new RaycastContext(nMin, nMax, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getMicrocosm().raycast(ctx);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(entity.toGlobal(hit.getPos()));
	}

	public VoxelShape toVoxelShape() {
		return new HullVoxelDelegate(this);
	}

	public void lazyFillCollisionSpace(Vector3f c, Vector3f ex) {
		Microcosm microcosm = entity.getMicrocosm();
		BlockPos.Mutable mBp = new BlockPos.Mutable();

		for (int x = (int) (c.x - ex.x); x < Math.ceil(c.x + ex.x) + 1; ++x) {
			for (int y = (int) (c.y - ex.y); y < Math.ceil(c.y + ex.y) + 1; ++y) {
				for (int z = (int) (c.z - ex.z); z < Math.ceil(c.z + ex.z) + 1; ++z) {
					mBp.set(x, y, z);
					Long mBpL = mBp.asLong();
					if (!loadedPositions.contains(mBpL) && microcosm.hasBlock(mBp)) {
						microcosm.getBlockState(mBp).getCollisionShape(microcosm, mBp).forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
							localVector3.set((float) (maxX - minX) / 2f, (float) (maxY - minY) / 2f, (float) (maxZ - minZ) / 2f);
							compoundHull.addChildShape(getOrCacheShape(localVector3),
									(float) minX + localVector3.x + mBp.getX(),
									(float) minY + localVector3.y + mBp.getY(),
									(float) minZ + localVector3.z + mBp.getZ());
						});
						loadedPositions.add(mBpL);
					}
				}
			}
		}
	}

	public double getMin(Direction.Axis axis) {
		return dBox.getMin(axis);
	}

	public double getMax(Direction.Axis axis) {
		return dBox.getMax(axis);
	}

	public Box getDelegateBox() {
		return dBox;
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

	@Override
	public Quaternion getRotation() {
		return entity.getRotation();
	}

	@Override
	public Quaternion getInverseRotation() {
		return entity.getInverseRotation();
	}

	private static BoxCollisionShape getOrCacheShape(Vector3f vec) {
		if (SHAPE_CACHE.containsKey(vec)) return SHAPE_CACHE.get(vec);
		Vector3f newVec = vec.clone();
		BoxCollisionShape newShape = new BoxCollisionShape(newVec);
		SHAPE_CACHE.put(newVec, newShape);
		return newShape;
	}
}