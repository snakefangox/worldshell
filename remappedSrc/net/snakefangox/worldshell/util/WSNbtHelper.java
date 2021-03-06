package net.snakefangox.worldshell.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.collision.QuaternionD;

/**
 * A common place for implementations of NBT serialization
 */
public class WSNbtHelper {

	public static void putVec3d(CompoundTag tag, Vec3d vec3d, String name) {
		tag.putDouble(name + "X", vec3d.x);
		tag.putDouble(name + "Y", vec3d.y);
		tag.putDouble(name + "Z", vec3d.z);
	}

	public static Vec3d getVec3d(CompoundTag tag, String name) {
		double x = tag.getDouble(name + "X");
		double y = tag.getDouble(name + "Y");
		double z = tag.getDouble(name + "Z");
		return new Vec3d(x, y, z);
	}

	public static void putQuaternion(CompoundTag tag, String name, QuaternionD rotation) {
		tag.putDouble(name + "X", rotation.getX());
		tag.putDouble(name + "Y", rotation.getY());
		tag.putDouble(name + "Z", rotation.getZ());
		tag.putDouble(name + "W", rotation.getW());
	}

	public static QuaternionD getQuaternion(CompoundTag tag, String name) {
		double x = tag.getDouble(name + "X");
		double y = tag.getDouble(name + "Y");
		double z = tag.getDouble(name + "Z");
		double w = tag.getDouble(name + "W");
		return new QuaternionD(x, y, z, w);
	}
}
