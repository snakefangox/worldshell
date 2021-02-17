package net.snakefangox.worldshell.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

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

	public static void putQuaternion(CompoundTag tag, String name, Quaternion rotation) {
		tag.putFloat(name + "X", rotation.getX());
		tag.putFloat(name + "Y", rotation.getY());
		tag.putFloat(name + "Z", rotation.getZ());
		tag.putFloat(name + "W", rotation.getW());
	}

	public static Quaternion getQuaternion(CompoundTag tag, String name) {
		float x = tag.getFloat(name + "X");
		float y = tag.getFloat(name + "Y");
		float z = tag.getFloat(name + "Z");
		float w = tag.getFloat(name + "W");
		return new Quaternion(x, y, z, w);
	}
}
