package net.snakefangox.worldshell.storage;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.collision.ShellCollisionHull;

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
		float newX = (float) (pos.getX() - getLocalX());
		float newY = (float) (pos.getY() - getLocalY());
		float newZ = (float) (pos.getZ() - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new BlockPos(vec.x, vec.y, vec.z);
	}

	default BlockPos toGlobal(BlockPos pos) {
		Vector3f vec = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
		getRotation().multLocal(vec);
		return new BlockPos(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default Vec3d toLocal(Vec3d pos) {
		float newX = (float) (pos.getX() - getLocalX());
		float newY = (float) (pos.getY() - getLocalY());
		float newZ = (float) (pos.getZ() - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	default Vec3d toGlobal(Vec3d pos) {
		Vector3f vec = new Vector3f((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
		getRotation().multLocal(vec);
		return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default Vec3d toLocal(double x, double y, double z) {
		float newX = (float) (x - getLocalX());
		float newY = (float) (y - getLocalY());
		float newZ = (float) (z - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new Vec3d(vec.x, vec.y, vec.z);
	}

	default Vec3d toGlobal(double x, double y, double z) {
		Vector3f vec = new Vector3f((float) x, (float) y, (float) z);
		getRotation().multLocal(vec);
		return new Vec3d(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default BlockPos toLocal(int x, int y, int z) {
		float newX = (float) (x - getLocalX());
		float newY = (float) (y - getLocalY());
		float newZ = (float) (z - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return new BlockPos(vec.x, vec.y, vec.z);
	}

	default BlockPos toGlobal(int x, int y, int z) {
		Vector3f vec = new Vector3f((float) x, (float) y, (float) z);
		getRotation().multLocal(vec);
		return new BlockPos(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default BlockPos.Mutable toLocal(BlockPos.Mutable pos) {
		float newX = (float) (pos.getX() - getLocalX());
		float newY = (float) (pos.getY() - getLocalY());
		float newZ = (float) (pos.getZ() - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		getInverseRotation().multLocal(vec);
		return pos.set(vec.x, vec.y, vec.z);
	}

	default BlockPos.Mutable toGlobal(BlockPos.Mutable pos) {
		Vector3f vec = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
		getRotation().multLocal(vec);
		return pos.set(vec.x + getLocalX(), vec.y + getLocalY(), vec.z + getLocalZ());
	}

	default Vector3f toLocal(Vector3f pos) {
		float newX = (float) (pos.x - getLocalX());
		float newY = (float) (pos.y - getLocalY());
		float newZ = (float) (pos.z - getLocalZ());
		return getInverseRotation().multLocal(pos.set(newX, newY, newZ));
	}

	default Vector3f toGlobal(Vector3f pos) {
		getRotation().multLocal(pos);
		return pos.addLocal((float) getLocalX(), (float) getLocalY(), (float) getLocalZ());
	}

	default double toLocalX(double x, double y, double z) {
		float newX = (float) (x - getLocalX());
		float newY = (float) (y - getLocalY());
		float newZ = (float) (z - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).x;
	}

	default double toLocalY(double x, double y, double z) {
		float newX = (float) (x - getLocalX());
		float newY = (float) (y - getLocalY());
		float newZ = (float) (z - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).y;
	}

	default double toLocalZ(double x, double y, double z) {
		float newX = (float) (x - getLocalX());
		float newY = (float) (y - getLocalY());
		float newZ = (float) (z - getLocalZ());
		Vector3f vec = new Vector3f(newX, newY, newZ);
		return getInverseRotation().multLocal(vec).z;
	}

	default double toGlobalX(double x, double y, double z) {
		return getRotation().multLocal(new Vector3f((float) x, (float) y, (float) z)).x + getLocalX();
	}

	default double toGlobalY(double x, double y, double z) {
		return getRotation().multLocal(new Vector3f((float) x, (float) y, (float) z)).y + getLocalY();
	}

	default double toGlobalZ(double x, double y, double z) {
		return getRotation().multLocal(new Vector3f((float) x, (float) y, (float) z)).z + getLocalZ();
	}

	default BlockPos globalToGlobal(LocalSpace target, BlockPos pos) {
		Vector3f vec = toLocal(new Vector3f(pos.getX(), pos.getY(), pos.getZ()));
		return target.toGlobal((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default Vec3d globalToGlobal(LocalSpace target, Vec3d pos) {
		Vector3f vec = toLocal(new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
		return target.toGlobal(vec.x, vec.y, vec.z);
	}

	default Vec3d globalToGlobal(LocalSpace target, double x, double y, double z) {
		Vector3f vec = toLocal(new Vector3f((float) x, (float) y, (float) z));
		return target.toGlobal(vec.x, vec.y, vec.z);
	}

	default BlockPos globalToGlobal(LocalSpace target, int x, int y, int z) {
		Vector3f vec = toLocal(new Vector3f(x, y, z));
		return target.toGlobal((int) vec.x, (int) vec.y, (int) vec.z);
	}

	default BlockPos.Mutable globalToGlobal(LocalSpace target, BlockPos.Mutable pos) {
		return target.toGlobal(toLocal(pos));
	}

	default double globalToGlobalX(LocalSpace target, double x, double y, double z) {
		Vector3f vec = toLocal(new Vector3f((float) x, (float) y, (float) z));
		return target.toGlobalX(vec.x, vec.y, vec.z);
	}

	default double globalToGlobalY(LocalSpace target, double x, double y, double z) {
		Vector3f vec = toLocal(new Vector3f((float) x, (float) y, (float) z));
		return target.toGlobalY(vec.x, vec.y, vec.z);
	}

	default double globalToGlobalZ(LocalSpace target, double x, double y, double z) {
		Vector3f vec = toLocal(new Vector3f((float) x, (float) y, (float) z));
		return target.toGlobalZ(vec.x, vec.y, vec.z);
	}
}