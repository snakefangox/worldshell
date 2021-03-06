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
import net.snakefangox.worldshell.storage.Microcosm;
import net.snakefangox.worldshell.util.CoordUtil;

import java.util.Optional;

/**
 * A custom {@link Box} implementation that takes a worldshell and handles rotated collision.<p>
 * Some <s>slightly</s> <s><i><b>very<b/></i></s> <p> <h3>INCREDIBLY</h3> questionable decisions are made
 * to optimise the collision calculations, you peer inside at your own risk.
 */
public class ShellCollisionHull extends Box implements SpecialBox {

	private static final float PADDING = 1F;
	private static final double SMOL = 0.0000001;
	private final WorldShellEntity entity;
	// These are here to prevent some high volume functions from requiring as many allocations
	// They're never given to anything outside this class and each function that takes them sets them beforehand
	private final Vec3dM aabbMax = new Vec3dM();
	private final Vec3dM aabbMin = new Vec3dM();
	private final Vec3dM pos = new Vec3dM();
	private QuaternionD inverseRotation;
	private Matrix3d matrix;
	private Matrix3d inverseMatrix;

	public ShellCollisionHull(WorldShellEntity entity) {
		super(0, 0, 0, 0, 0, 0);
		this.entity = entity;
		setRotation(QuaternionD.IDENTITY);
	}

	public void setRotation(QuaternionD q) {
		matrix = new Matrix3d(q);
		inverseRotation = new QuaternionD(-q.getX(), -q.getY(), -q.getZ(), q.getW());
		inverseMatrix = new Matrix3d(inverseRotation);
	}

	public void sizeUpdate() {
		calculateCrudeBounds();
	}

	public void calculateCrudeBounds() {
		EntityBounds ed = entity.getDimensions();
		float len = ed.length / 2F;
		float width = ed.width / 2F;
		Vec3d bo = entity.getBlockOffset();
		Vec3d min = matrix.transform(-len - bo.x, -bo.y, -width - bo.z);
		Vec3d max = matrix.transform(len - bo.x, ed.height - bo.y, width - bo.z);
		calcNewAABB(min.x, min.y, min.z, max.x, max.y, max.z);
		minX = aabbMin.x + entity.getX() + bo.x - PADDING;
		minY = aabbMin.y + entity.getY() + bo.y - PADDING;
		minZ = aabbMin.z + entity.getZ() + bo.z - PADDING;
		maxX = aabbMax.x + entity.getX() + bo.x + PADDING;
		maxY = aabbMax.y + entity.getY() + bo.y + PADDING;
		maxZ = aabbMax.z + entity.getZ() + bo.z + PADDING;
	}

	private void calcNewAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		aabbMax.setAll(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		aabbMin.setAll(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		pos.setAll(minX, maxY, maxZ);
		checkExtents(pos);
		pos.setAll(minX, maxY, minZ);
		checkExtents(pos);
		pos.setAll(minX, minY, maxZ);
		checkExtents(pos);
		pos.setAll(minX, minY, minZ);
		checkExtents(pos);
		pos.setAll(maxX, maxY, maxZ);
		checkExtents(pos);
		pos.setAll(maxX, maxY, minZ);
		checkExtents(pos);
		pos.setAll(maxX, minY, maxZ);
		checkExtents(pos);
		pos.setAll(maxX, minY, minZ);
		checkExtents(pos);
	}

	private void checkExtents(Vec3dM vec) {
		if (vec.x > aabbMax.x) aabbMax.x = vec.x;
		if (vec.y > aabbMax.y) aabbMax.y = vec.y;
		if (vec.z > aabbMax.z) aabbMax.z = vec.z;
		if (vec.x < aabbMin.x) aabbMin.x = vec.x;
		if (vec.y < aabbMin.y) aabbMin.y = vec.y;
		if (vec.z < aabbMin.z) aabbMin.z = vec.z;
	}

	@Override
	public boolean intersects(Box box) {
		return box.intersects(minX, minY, minZ, maxX, maxY, maxZ) && this.intersects(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		if (super.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
			OrientedBox collidingBox = calcRotatedBox(minX, minY, minZ, maxX, maxY, maxZ);
			Box box = new Box(aabbMin.x, aabbMin.y, aabbMin.z, aabbMax.x, aabbMax.y, aabbMax.z);
			boolean[] collides = new boolean[]{false};
			int xLimit = (int) Math.ceil(aabbMax.x);
			int yLimit = (int) Math.ceil(aabbMax.y);
			int zLimit = (int) Math.ceil(aabbMax.z);
			Microcosm shell = entity.getMicrocosm();
			BlockPos.Mutable bp = new BlockPos.Mutable();
			for (int x = (int) aabbMin.x; x < xLimit; ++x) {
				for (int y = (int) aabbMin.y; y < yLimit; ++y) {
					for (int z = (int) aabbMin.z; z < zLimit; ++z) {
						bp.set(x, y, z);
						VoxelShape shape = shell.getBlockState(bp).getCollisionShape(shell, bp);
						if (shape.isEmpty()) continue;
						shape.forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) -> {
							setBox(box, minX1 + bp.getX(), minY1 + bp.getY(), minZ1 + bp.getZ(),
									maxX1 + bp.getX(), maxY1 + bp.getY(), maxZ1 + bp.getZ());
							collides[0] = (collides[0] || collidingBox.intersects(box));
						});
						if (collides[0]) return true;
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
		Vec3d vec = inverseMatrix.transform(pos.x, pos.y, pos.z);
		BlockPos bp = new BlockPos(vec);
		VoxelShape shape = entity.getMicrocosm().getBlockState(bp).getCollisionShape(entity.getMicrocosm(), bp);
		if (shape.isEmpty()) return false;
		return shape.getBoundingBox().contains(vec.x, vec.y, vec.z);
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		Vec3d nMin = inverseMatrix.transform(CoordUtil.worldToLinkEntity(entity, min));
		Vec3d nMax = inverseMatrix.transform(CoordUtil.worldToLinkEntity(entity, max));
		RaycastContext ctx = new RaycastContext(nMin, nMax, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
		BlockHitResult hit = entity.getMicrocosm().raycast(ctx);
		return hit.getType() == HitResult.Type.MISS ? Optional.empty() : Optional.of(CoordUtil.linkEntityToWorld(CoordUtil.BP_ZERO, entity, hit.getPos()));
	}

	/**
	 * Calculates the oriented box version of the AABB given and sets the aabb vars to an AABB that encloses the rotated box
	 */
	private OrientedBox calcRotatedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		aabbMin.setAll(minX, minY, minZ);
		aabbMax.setAll(maxX, maxY, maxZ);
		double xSize = (aabbMax.x - aabbMin.x) / 2.0;
		double ySize = (aabbMax.y - aabbMin.y) / 2.0;
		double zSize = (aabbMax.z - aabbMin.z) / 2.0;
		CoordUtil.worldToLinkEntity(entity, aabbMin);
		CoordUtil.worldToLinkEntity(entity, aabbMax);
		OrientedBox orientedBox = new OrientedBox(inverseMatrix.transform(aabbMin.x + xSize, aabbMin.y + ySize, aabbMin.z + zSize),
				new Vec3d(xSize, ySize, zSize), inverseRotation);
		Vec3d min = inverseMatrix.transform(aabbMin.x, aabbMin.y, aabbMin.z);
		Vec3d max = inverseMatrix.transform(aabbMax.x, aabbMax.y, aabbMax.z);
		calcNewAABB(min.x, min.y, min.z, max.x, max.y, max.z);
		return orientedBox;
	}

	private void setBox(Box box, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		box.minX = minX;
		box.minY = minY;
		box.minZ = minZ;
		box.maxX = maxX;
		box.maxY = maxY;
		box.maxZ = maxZ;
	}

	public VoxelShape toVoxelShape() {
		return new HullVoxelDelegate(this);
	}

	/*TODO the story so far
	  This doesn't properly handle being inside of blocks, if a player is inside a block
	  and the game checks the axis that isn't pointing towards the block this will say there is 0 distance
	  even though there is actually some distance.
	  This is likely causing issues with bouncing around as calculating push force to remove the player from the block returns 0 push
	  Try modifying this so we don't end up inside blocks. Then try offsetting and ray casting if we are.
	 */
	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		if (Math.abs(maxDist) < SMOL) return 0;
		OrientedBox orientedBox = calcRotatedBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		Vec3d[] basis = orientedBox.getBasis();
		Box localBox = new Box(aabbMin.x, aabbMin.y, aabbMin.z, aabbMax.x, aabbMax.y, aabbMax.z);
		double[] maxDistRef = new double[]{maxDist};
		int index = axis.choose(0, 1, 2);
		int forward = axis.choose(1, 2, 0);
		int back = axis.choose(2, 0, 1);
		int xLimit = (int) Math.ceil(aabbMax.x) + 1;
		int yLimit = (int) Math.ceil(aabbMax.y) + 1;
		int zLimit = (int) Math.ceil(aabbMax.z) + 1;
		Microcosm shell = entity.getMicrocosm();
		BlockPos.Mutable bp = new BlockPos.Mutable();
		for (int x = (int) aabbMin.x - 1; x < xLimit; ++x) {
			for (int y = (int) aabbMin.y - 1; y < yLimit; ++y) {
				for (int z = (int) aabbMin.z - 1; z < zLimit; ++z) {
					bp.set(x, y, z);
					VoxelShape shape = shell.getBlockState(bp).getCollisionShape(shell, bp);
					if (shape.isEmpty()) continue;
					shape.forEachBox((minX1, minY1, minZ1, maxX1, maxY1, maxZ1) -> {
						setBox(localBox, minX1 + bp.getX(), minY1 + bp.getY(), minZ1 + bp.getZ(),
								maxX1 + bp.getX(), maxY1 + bp.getY(), maxZ1 + bp.getZ());
						Vec3d[] vertices = getVertices(localBox);
						if (orientedBox.sat(basis[forward], vertices) && orientedBox.sat(basis[back], vertices))
							maxDistRef[0] = castForDistance(orientedBox, localBox, vertices, basis[index], maxDistRef[0]);
					});
					if (Math.abs(maxDistRef[0]) < SMOL) return 0.0D;
				}
			}
		}
		return maxDistRef[0];
	}

	private Vec3d[] getVertices(Box box) {
		Vec3d[] vertexList = new Vec3d[8];
		int i = 0;
		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					double pX = getVertVal(box, Direction.Axis.X, x);
					double pY = getVertVal(box, Direction.Axis.Y, y);
					double pZ = getVertVal(box, Direction.Axis.Z, z);
					vertexList[i++] = new Vec3d(pX, pY, pZ);
				}
			}
		}
		return vertexList;
	}

	private double castForDistance(OrientedBox orientedBox, Box localBox, Vec3d[] bVertexList, Vec3d basis, double maxDist) {
		Vec3d oCenter = orientedBox.getCenter();
		Vec3d bCenter = localBox.getCenter();
		Vec3d[] oVertexList = orientedBox.getVertices();
		if (maxDist > 0) {
			for (Vec3d vec : oVertexList) {
				double dot = dot(vec.x - oCenter.x, vec.y - oCenter.y, vec.z - oCenter.z, basis.x, basis.y, basis.z);
				if (dot > 0) {
					if (localBox.contains(vec)) return 0;
					Optional<Vec3d> optional = localBox.raycast(vec, new Vec3d(vec.x + basis.x * maxDist,
							vec.y + basis.y * maxDist, vec.z + basis.z * maxDist));
					if (optional.isPresent()) {
						Vec3d result = optional.get();
						double dist = vec.distanceTo(result);
						if (dist >= -SMOL) maxDist = Math.min(maxDist, dist);
						if (Math.abs(maxDist) < SMOL) return 0;
					}
				}
			}
			for (Vec3d vec : bVertexList) {
				double dot = dot(vec.x - bCenter.x, vec.y - bCenter.y, vec.z - bCenter.z, basis.x, basis.y, basis.z);
				if (dot < 0) {
					if (orientedBox.contains(vec.x, vec.y, vec.z)) return 0;
					double dist = orientedBox.raycast(vec, new Vec3d(vec.x - basis.x * maxDist,
							vec.y - basis.y * maxDist, vec.z - basis.z * maxDist));
					if (dist != -1) {
						if (dist >= -SMOL) maxDist = Math.min(maxDist, dist);
						if (Math.abs(maxDist) < SMOL) return 0;
					}
				}
			}
		} else {
			for (Vec3d vec : oVertexList) {
				double dot = dot(vec.x - oCenter.x, vec.y - oCenter.y, vec.z - oCenter.z, basis.x, basis.y, basis.z);
				if (dot < 0) {
					if (localBox.contains(vec)) return 0;
					Optional<Vec3d> optional = localBox.raycast(vec, new Vec3d(vec.x + basis.x * maxDist,
							vec.y + basis.y * maxDist, vec.z + basis.z * maxDist));
					if (optional.isPresent()) {
						Vec3d result = optional.get();
						double dist = -vec.distanceTo(result);
						if (dist <= SMOL) maxDist = Math.max(maxDist, dist);
						if (Math.abs(maxDist) < SMOL) return 0;
					}
				}
			}
			for (Vec3d vec : bVertexList) {
				double dot = dot(vec.x - bCenter.x, vec.y - bCenter.y, vec.z - bCenter.z, basis.x, basis.y, basis.z);
				if (dot > 0) {
					if (orientedBox.contains(vec.x, vec.y, vec.z)) return 0;
					double dist = orientedBox.raycast(vec, new Vec3d(vec.x - basis.x * maxDist,
							vec.y - basis.y * maxDist, vec.z - basis.z * maxDist));
					if (dist != -1) {
						dist = -dist;
						if (dist <= SMOL) maxDist = Math.max(maxDist, dist);
						if (Math.abs(maxDist) < SMOL) return 0;
					}
				}
			}
		}
		return maxDist;
	}

	private double getVertVal(Box box, Direction.Axis axis, int val) {
		return val == 0 ? box.getMax(axis) : box.getMin(axis);
	}

	private double dot(double x1, double y1, double z1, double x2, double y2, double z2) {
		return x1 * x2 + y1 * y2 + z1 * z2;
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
