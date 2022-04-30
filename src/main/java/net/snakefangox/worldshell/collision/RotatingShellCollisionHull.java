package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A {@link ShellCollisionHull} that handles rotated collisions correctly but is quote a bit slower.
 * Also currently broken
 */
public class RotatingShellCollisionHull extends ShellCollisionHull {

	private static final Vector3d[] IDENTITY_BASIS = new Vector3d[3];

	private final Vector3d oBoxPos = new Vector3d();
	private final Vector3d oBoxHalfEx = new Vector3d();
	private final Vector3d[] oBasis = new Vector3d[3];
	private final Vector3d[] oBoxVertices = new Vector3d[8];
	private final Vector3d[] boxVertices = new Vector3d[8];

	public RotatingShellCollisionHull(WorldShellEntity entity) {
		super(entity);
		for (int i = 0; i < oBoxVertices.length; i++) oBoxVertices[i] = new Vector3d();
		for (int i = 0; i < boxVertices.length; i++) boxVertices[i] = new Vector3d();
		for (int i = 0; i < oBasis.length; i++) oBasis[i] = new Vector3d();
	}

	@Override
	public void onWorldshellRotate() {
		super.onWorldshellRotate();
		getInverseRotation().multLocal(oBasis[0].set(1, 0, 0));
		getInverseRotation().multLocal(oBasis[1].set(0, 1, 0));
		getInverseRotation().multLocal(oBasis[2].set(0, 0, 1));
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		Box lBox = boxToLocal(minX, minY, minZ, maxX, maxY, maxZ);
		setOrientedBoxVars(minX, minY, minZ, maxX, maxY, maxZ);
		Iterable<VoxelShape> shapeStream = entity.getMicrocosm().getBlockCollisions(null, lBox);
		return StreamSupport.stream(shapeStream.spliterator(), false).anyMatch(this::oBoxIntersects);
	}

	private boolean oBoxIntersects(VoxelShape shape) {
		boolean[] result = new boolean[1];
		shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
			if (!result[0]) {
				setBoxVertices(minX, minY, minZ, maxX, maxY, maxZ);
				if (allAxisSat()) result[0] = true;
			}
		});
		return result[0];
	}

	@Override
	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		double absMaxDist = Math.abs(maxDist);
		if (absMaxDist < SMOL) return 0;
		Vector3d f = oBasis[axis.ordinal()];
		Vector3d s = oBasis[(axis.ordinal() + 1) % 3];
		Vector3d u = oBasis[(axis.ordinal() + 2) % 3];
		double x = axis.choose(maxDist, 0, 0);
		double y = axis.choose(0, maxDist, 0);
		double z = axis.choose(0, 0, maxDist);
		double distSign = Math.signum(maxDist);
		setOrientedBoxVars(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		Box lBox = boxToLocal(box);
		Stream<VoxelShape> shapeStream = StreamSupport.stream(entity.getMicrocosm().getBlockCollisions(null, lBox.stretch(x, y, z)).spliterator(), false);
		Stream<Double> distStream = shapeStream.map(vs -> distanceFromOBoxToShape(vs, f, s, u, distSign));
		double nDist = Math.min(absMaxDist, distStream.min(Double::compareTo).orElse(absMaxDist)) * distSign;
		return nDist;
	}

	private double distanceFromOBoxToShape(VoxelShape shape, Vector3d forward, Vector3d side, Vector3d up, double sign) {
		double[] dist = new double[] {Double.MAX_VALUE};
		shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
			setBoxVertices(minX, minY, minZ, maxX, maxY, maxZ);
			if (!sat(side) || !sat(up) || allAxisSat()) return;
			double lDist = clippedSatDistance(localVector.set(forward), sign);
			dist[0] = Math.min(dist[0], lDist);
		});
		return dist[0];
	}

	private boolean allAxisSat() {
		for (Vector3d norm : IDENTITY_BASIS)
			if (!sat(norm)) return false;
		for (Vector3d norm : oBasis)
			if (!sat(norm)) return false;

		for (Vector3d norm1 : IDENTITY_BASIS) {
			for (Vector3d norm2 : oBasis)
				if (!sat(norm1.cross(norm2, localVector))) return false;
		}

		return true;
	}

	private boolean sat(Vector3d normal) {
		double oMin = Double.MAX_VALUE;
		double oMax = -Double.MAX_VALUE;
		for (Vector3d vert : oBoxVertices) {
			double d = vert.dot(normal);
			oMin = Math.min(oMin, d);
			oMax = Math.max(oMax, d);
		}

		double bMin = Double.MAX_VALUE;
		double bMax = -Double.MAX_VALUE;
		for (Vector3d vert : boxVertices) {
			double d = vert.dot(normal);
			bMin = Math.min(bMin, d);
			bMax = Math.max(bMax, d);
		}

		return oMin <= bMax && bMin <= oMax;
	}

	private double clippedSatDistance(Vector3d normal, double sign) {
		double oMin = Double.MAX_VALUE;
		double oMax = -Double.MAX_VALUE;
		for (Vector3d vert : oBoxVertices) {
			double d = vert.dot(normal);
			oMin = Math.min(oMin, d - SMOL);
			oMax = Math.max(oMax, d + SMOL);
		}

		double bMin = Double.MAX_VALUE;
		double bMax = -Double.MAX_VALUE;
		for (Vector3d vert : boxVertices) {
			double d = vert.dot(normal);
			bMin = Math.min(bMin, d);
			bMax = Math.max(bMax, d);
		}

		if (sign > 0) {
			return bMin - oMax;
		} else {
			return oMin - bMax;
		}
	}

	private void setOrientedBoxVars(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		localVector.set(minX, minY, minZ);
		localVector2.set(maxX, maxY, maxZ);
		double xSize = (localVector2.x - localVector.x) / 2.0;
		double ySize = (localVector2.y - localVector.y) / 2.0;
		double zSize = (localVector2.z - localVector.z) / 2.0;
		toLocal(localVector.addLocal(xSize, ySize, zSize));
		Quaternion rotation = getRotation();
		oBoxPos.set(localVector);
		oBoxHalfEx.set(xSize, ySize, zSize);
		int i = 0;
		for (int x = -1; x <= 1; x += 2) {
			for (int y = -1; y <= 1; y += 2) {
				for (int z = -1; z <= 1; z += 2) {
					localVector.set(oBoxHalfEx.x * x, oBoxHalfEx.y * y, oBoxHalfEx.z * z);
					rotation.multLocal(localVector);
					oBoxVertices[i++].set(oBoxPos).addLocal(localVector);
				}
			}
		}
	}

	private void setBoxVertices(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		boxVertices[0].set(minX, minY, minZ);
		boxVertices[1].set(minX, minY, maxZ);
		boxVertices[2].set(minX, maxY, minZ);
		boxVertices[3].set(minX, maxY, maxZ);
		boxVertices[4].set(maxX, minY, minZ);
		boxVertices[5].set(maxX, minY, maxZ);
		boxVertices[6].set(maxX, maxY, minZ);
		boxVertices[7].set(maxX, maxY, maxZ);
	}

	static {
		IDENTITY_BASIS[0] = Vector3d.UNIT_X;
		IDENTITY_BASIS[1] = Vector3d.UNIT_Y;
		IDENTITY_BASIS[2] = Vector3d.UNIT_Z;
	}
}
