package net.snakefangox.worldshell.collision;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Matrix3d;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;
import net.snakefangox.worldshell.storage.LocalSpace;

import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A custom {@link Box} style implementation that takes a worldshell and handles
 * complex collision.
 */
public class ShellCollisionHull implements LocalSpace {

	protected static final double SMOL = 1.0E-7D;
	protected final WorldShellEntity entity;
	protected HullBoxDelegate delegateBox;
	protected WorldshellCollisionHandler collisionHandler = new WorldshellCollisionHandler();
	protected final Vector3d localVector = new Vector3d(), localVector2 = new Vector3d(), localVector3 = new Vector3d();
	protected final Matrix3d localMatrix = new Matrix3d();
	protected final BlockPos.Mutable localBp = new BlockPos.Mutable();

	public ShellCollisionHull(WorldShellEntity entity) {
		this.entity = entity;
		delegateBox = new HullBoxDelegate(new Box(BlockPos.ORIGIN), this);
	}

	public void onWorldshellRotate() {
		calculateBounds();
	}

	public void onWorldshellUpdate() {
		calculateBounds();
	}

	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		Box lBox = boxToLocal(minX, minY, minZ, maxX, maxY, maxZ);
		Vector3d halfExtents = new Vector3d((maxX - minX) / 2.0, (maxY - minY) / 2.0, (maxZ - minZ) / 2.0);
		Vector3d pos = toLocal(new Vector3d((maxX + minX) / 2.0, (maxY + minY) / 2.0, (maxZ + minZ) / 2.0));

		Vector3d boxPos = new Vector3d();
		Vector3d boxHalfExtents = new Vector3d();

		return StreamSupport
				.stream(entity.getMicrocosm().getBlockCollisions(null, lBox.expand(0.01)).spliterator(), false)
				.anyMatch((v) -> {
					boolean[] found = new boolean[1];
					v.forEachBox((bMinX, bMinY, bMinZ, bMaxX, bMaxY, bMaxZ) -> {
						if (found[0])
							return;

						boxPos.set((bMaxX + bMinX) / 2.0, (bMaxY + bMinY) / 2.0, (bMaxZ + bMinZ) / 2.0);
						boxHalfExtents.set((bMaxX - bMinX) / 2.0, (bMaxY - bMinY) / 2.0, (bMaxZ - bMinZ) / 2.0);

						if (collisionHandler.intersects(pos, getInverseRotation(), halfExtents, boxPos, boxHalfExtents))
							found[0] = true;
					});

					return found[0];
				});
	}

	/**
	 * This is complicated and the vanilla method is hell, so here goes my attempt
	 * to explain it.
	 * <p>
	 * This method returns the smallest distance separating the hull and a bounding
	 * box in a given direction (negative or positive depending on the sign of
	 * maxDist), along the given axis. This system was designed for vanilla, where
	 * everything is axis-aligned. Out here we need to do some fuckery.
	 * <p>
	 * We get the center of the colliding box and make it local to the shell.
	 * Then we rotate the box by the inverse of our rotation around it's center.
	 * This is the same as rotating the shell, but much easier to handle (if not
	 * reason about).
	 * 
	 * @param axis    The axis along which to check distance.
	 * @param box     The colliding box.
	 * @param maxDist The longest distance we should check for collision.
	 * @return The smallest seperation.
	 */
	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		if (Math.abs(maxDist) < SMOL) {
			return 0.0;
		}

		// Get blocks we could collide with
		Box lBox = boxToLocal(
				box.stretch(axis.choose(maxDist, 0, 0), axis.choose(0, maxDist, 0), axis.choose(0, 0, maxDist)));
		Spliterator<VoxelShape> shapeStream = entity.getMicrocosm().getBlockCollisions(null, lBox).spliterator();

		byte vecAxis = (byte) axis.ordinal();
		Vec3d boxCenter = box.getCenter();
		Vector3d pos = toLocal(new Vector3d(boxCenter.x, boxCenter.y, boxCenter.z));
		Vector3d halfExtents = new Vector3d(box.getXLength() / 2.0, box.getYLength() / 2.0, box.getZLength() / 2.0)
				// Pad the bounding box slightly in the direction we're testing, without this we
				// collide with the edges of the ground when we walk
				.addLocal(axis.choose(SMOL, 0, 0), axis.choose(0, SMOL, 0), axis.choose(0, SMOL, 0));

		Vector3d boxPos = new Vector3d();
		Vector3d boxHalfExtents = new Vector3d();

		Stream<Double> stream = StreamSupport.stream(shapeStream, false).map((v) -> {
			double[] minDist = new double[] { maxDist };
			v.forEachBox((bMinX, bMinY, bMinZ, bMaxX, bMaxY, bMaxZ) -> {
				boxPos.set((bMaxX + bMinX) / 2.0, (bMaxY + bMinY) / 2.0, (bMaxZ + bMinZ) / 2.0);
				boxHalfExtents.set((bMaxX - bMinX) / 2.0, (bMaxY - bMinY) / 2.0, (bMaxZ - bMinZ) / 2.0);
				double dist = collisionHandler.calculateMaxDistance(vecAxis, pos, getInverseRotation(), halfExtents,
						boxPos, boxHalfExtents, maxDist);

				if (maxDist > 0) {
					if (dist < minDist[0])
						minDist[0] = dist;
				} else {
					if (dist > minDist[0])
						minDist[0] = dist;
				}
			});

			return minDist[0];
		});

		double dist;
		if (maxDist > 0) {
			dist = stream.min(Double::compare).orElse(maxDist);
		} else {
			dist = stream.max(Double::compare).orElse(maxDist);
		}

		if (Math.abs(dist) <= SMOL)
			return 0;

		return dist;
	}

	public boolean contains(double x, double y, double z) {
		localVector.set(x, y, z);
		toLocal(localVector);
		localBp.set(localVector.x, localVector.y, localVector.z);
		VoxelShape shape = entity.getMicrocosm().getBlockState(localBp).getCollisionShape(entity.getMicrocosm(),
				localBp);
		if (shape.isEmpty())
			return false;
		return shape.getBoundingBox().contains(localVector.x, localVector.y, localVector.z);
	}

	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3d nMin = toLocal(min);
		Vec3d nMax = toLocal(max);
		RaycastContext ctx = new RaycastContext(nMin, nMax, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getMicrocosm().raycast(ctx);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(entity.toGlobal(hit.getPos()));
	}

	protected Box boxToLocal(Box box) {
		return boxToLocal(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	protected Box boxToLocal(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		localVector.set(minX - getLocalX(), minY - getLocalY(), minZ - getLocalZ());
		localVector2.set(maxX - getLocalX(), maxY - getLocalY(), maxZ - getLocalZ());
		localVector3.set(0, 0, 0);
		return transformBox(localVector, localVector2, getRotation(), localVector3);
	}

	protected Box transformBox(Vector3d min, Vector3d max, Quaternion rot, Vector3d trans) {
		rot.toRotationMatrix(localMatrix);
		double[] oMin = new double[] { min.x, min.y, min.z };
		double[] oMax = new double[] { max.x, max.y, max.z };
		double[] nMin = new double[] { trans.x, trans.y, trans.z };
		double[] nMax = new double[] { trans.x, trans.y, trans.z };

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				double a = localMatrix.get(j, i) * oMin[j];
				double b = localMatrix.get(j, i) * oMax[j];

				nMin[i] += Math.min(a, b);
				nMax[i] += Math.max(a, b);
			}
		}

		return new Box(nMin[0], nMin[1], nMin[2], nMax[0], nMax[1], nMax[2]);
	}

	protected void calculateBounds() {
		EntityBounds bounds = entity.getDimensions();
		double len = bounds.length / 2.0;
		double height = bounds.height;
		double width = bounds.width / 2.0;
		Vec3d off = entity.getBlockOffset();

		localVector.set(-len - off.x, -off.y, -width - off.z);
		localVector2.set(len - off.x, height - off.y, width - off.z);
		localVector3.set(getLocalX(), getLocalY(), getLocalZ());
		delegateBox = new HullBoxDelegate(transformBox(localVector, localVector2, getInverseRotation(), localVector3),
				this);
	}

	public VoxelShape toVoxelShape() {
		return new HullVoxelDelegate(this);
	}

	public double getMin(Direction.Axis axis) {
		return delegateBox.getMin(axis);
	}

	public double getMax(Direction.Axis axis) {
		return delegateBox.getMax(axis);
	}

	public Box getDelegateBox() {
		return delegateBox;
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
}