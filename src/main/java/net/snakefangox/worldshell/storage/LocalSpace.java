package net.snakefangox.worldshell.storage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;

/**
 * Indicates that a given object contains a local coordinate space that differs from
 * the world space. Contains default methods to translate <i>most</i> of minecraft's many coordinate
 * types between the world space and this space or between this space and another local space.
 * <p>
 * The single coordinate methods exist to reduce allocation if you don't need it,
 * they require all three coordinates so they can rotate properly.
 *
 * <h1>WARNING: DO NOT IMPLEMENT THE DEFAULT METHODS</h1>
 * They're happy just how they are and you'll be happier not having manually written ~25 basic
 * transform methods.
 */
public interface LocalSpace {

	/** The coordinate space of the normal world */
	LocalSpace WORLDSPACE = of(0, 0, 0);

	/** Creates a local space with no rotation and with origin at the given point */
	static LocalSpace of(double x, double y, double z) {
		return new LocalSpace() {
			@Override
			public double getLocalX() {
				return x;
			}

			@Override
			public double getLocalY() {
				return y;
			}

			@Override
			public double getLocalZ() {
				return z;
			}
		};
	}

	/** Creates a local space with the given rotation and with origin at the given point */
	static LocalSpace of(double x, double y, double z, Quaternion rotation) {
		return new LocalSpace() {
			@Override
			public double getLocalX() {
				return x;
			}

			@Override
			public double getLocalY() {
				return y;
			}

			@Override
			public double getLocalZ() {
				return z;
			}

			@Override
			public Quaternion getRotation() {
				return rotation;
			}
		};
	}

	/** Returns the world coordinate where this local space's X coordinate is zero */
	double getLocalX();

	/** Returns the world coordinate where this local space's Y coordinate is zero */
	double getLocalY();

	/** Returns the world coordinate where this local space's Z coordinate is zero */
	double getLocalZ();

	/** Returns the quaternion the local world is rotated by */
	default Quaternion getRotation() {
		return Quaternion.IDENTITY;
	}

	default Quaternion getInverseRotation() {
		return getRotation().inverse();
	}

	/** -- Below this line lies only madness and a lot of matrix transforms -- **/

	default BlockPos toLocal(BlockPos pos) {
		double newX = (pos.getX() - getLocalX());
		double newY = (pos.getY() - getLocalY());
		double newZ = (pos.getZ() - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default BlockPos toGlobal(BlockPos pos) {
		Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		getRotation().multLocal(vec);
		return new BlockPos((int) (vec.x + getLocalX()), (int) (vec.y + getLocalY()), (int) (vec.z + getLocalZ()));
	}

	default Vec3d toLocal(Vec3d pos) {
		double newX = (pos.getX() - getLocalX());
		double newY = (pos.getY() - getLocalY());
		double newZ = (pos.getZ() - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	default Vec3d toGlobal(Vec3d pos) {
		Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		getRotation().multLocal(vec);
		return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default Vec3d toLocal(double x, double y, double z) {
		double newX = (x - getLocalX());
		double newY = (y - getLocalY());
		double newZ = (z - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	default Vec3d toGlobal(double x, double y, double z) {
		Vector3d vec = new Vector3d(x, y, z);
		getRotation().multLocal(vec);
		return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default BlockPos toLocal(int x, int y, int z) {
		double newX = (x - getLocalX());
		double newY = (y - getLocalY());
		double newZ = (z - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default BlockPos toGlobal(int x, int y, int z) {
		Vector3d vec = new Vector3d(x, y, z);
		getRotation().multLocal(vec);
		return new BlockPos((int) (vec.x + getLocalX()), (int) (vec.y + getLocalY()), (int) (vec.z + getLocalZ()));
	}

	default BlockPos.Mutable toLocal(BlockPos.Mutable pos) {
		double newX = (pos.getX() - getLocalX());
		double newY = (pos.getY() - getLocalY());
		double newZ = (pos.getZ() - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return pos.set(vec.x, vec.y, vec.z);
	}

	default BlockPos.Mutable toGlobal(BlockPos.Mutable pos) {
		Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		getRotation().multLocal(vec);
		return pos.set((int) (vec.x + getLocalX()), (int) (vec.y + getLocalY()), (int) (vec.z + getLocalZ()));
	}

	default Vector3d toLocal(Vector3d pos) {
		double newX = (pos.x - getLocalX());
		double newY = (pos.y - getLocalY());
		double newZ = (pos.z - getLocalZ());
		return getInverseRotation().multLocal(pos.set(newX, newY, newZ));
	}

	default Vector3d toGlobal(Vector3d pos) {
		getRotation().multLocal(pos);
		return pos.addLocal(getLocalX(), getLocalY(), getLocalZ());
	}

	default double toLocalX(double x, double y, double z) {
		double newX = (x - getLocalX());
		double newY = (y - getLocalY());
		double newZ = (z - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).x;
	}

	default double toLocalY(double x, double y, double z) {
		double newX = (x - getLocalX());
		double newY = (y - getLocalY());
		double newZ = (z - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).y;
	}

	default double toLocalZ(double x, double y, double z) {
		double newX = (x - getLocalX());
		double newY = (y - getLocalY());
		double newZ = (z - getLocalZ());
		Vector3d vec = new Vector3d(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).z;
	}

	default double toGlobalX(double x, double y, double z) {
		return getRotation().multLocal(new Vector3d(x, y, z)).x + getLocalX();
	}

	default double toGlobalY(double x, double y, double z) {
		return getRotation().multLocal(new Vector3d(x, y, z)).y + getLocalY();
	}

	default double toGlobalZ(double x, double y, double z) {
		return getRotation().multLocal(new Vector3d(x, y, z)).z + getLocalZ();
	}

	default BlockPos globalToGlobal(LocalSpace target, BlockPos pos) {
		Vector3d vec = toLocal(new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
		return target.toGlobal((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default Vec3d globalToGlobal(LocalSpace target, Vec3d pos) {
		Vector3d vec = toLocal(new Vector3d(pos.x, pos.y, pos.z));
		return target.toGlobal(vec.x, vec.y, vec.z);
	}

	default Vec3d globalToGlobal(LocalSpace target, double x, double y, double z) {
		Vector3d vec = toLocal(new Vector3d(x, y, z));
		return target.toGlobal(vec.x, vec.y, vec.z);
	}

	default BlockPos globalToGlobal(LocalSpace target, int x, int y, int z) {
		Vector3d vec = toLocal(new Vector3d(x, y, z));
		return target.toGlobal((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default BlockPos.Mutable globalToGlobal(LocalSpace target, BlockPos.Mutable pos) {
		return target.toGlobal(toLocal(pos));
	}

	default double globalToGlobalX(LocalSpace target, double x, double y, double z) {
		Vector3d vec = toLocal(new Vector3d(x, y, z));
		return target.toGlobalX(vec.x, vec.y, vec.z);
	}

	default double globalToGlobalY(LocalSpace target, double x, double y, double z) {
		Vector3d vec = toLocal(new Vector3d(x, y, z));
		return target.toGlobalY(vec.x, vec.y, vec.z);
	}

	default double globalToGlobalZ(LocalSpace target, double x, double y, double z) {
		Vector3d vec = toLocal(new Vector3d(x, y, z));
		return target.toGlobalZ(vec.x, vec.y, vec.z);
	}
}