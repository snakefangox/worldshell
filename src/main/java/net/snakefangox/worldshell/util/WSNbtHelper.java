package net.snakefangox.worldshell.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.collision.QuaternionD;

/** A common place for implementations of NBT serialization */
public class WSNbtHelper {

	public static void putVec3d(CompoundTag tag, Vec3d vec3d, String name) {
		ListTag list = new ListTag();
		list.add(DoubleTag.of(vec3d.x));
		list.add(DoubleTag.of(vec3d.y));
		list.add(DoubleTag.of(vec3d.z));
		tag.put(name, list);
	}

	public static Vec3d getVec3d(CompoundTag tag, String name) {
		ListTag list = tag.getList(name, DoubleTag.ZERO.getType());
		return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static void putQuaternion(CompoundTag tag, String name, QuaternionD rotation) {
		ListTag list = new ListTag();
		list.add(DoubleTag.of(rotation.getX()));
		list.add(DoubleTag.of(rotation.getY()));
		list.add(DoubleTag.of(rotation.getZ()));
		list.add(DoubleTag.of(rotation.getW()));
		tag.put(name, list);
	}

	public static QuaternionD getQuaternion(CompoundTag tag, String name) {
		ListTag list = tag.getList(name, DoubleTag.ZERO.getType());
		return new QuaternionD(list.getDouble(0), list.getDouble(1), list.getDouble(2), list.getDouble(3));
	}
}
