package net.snakefangox.worldshell.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Vec3d;

public class CoordinateHelper {

	public static Vec3d getBoxCenter(BlockBox box) {
		double x = box.minX + (((double) (box.maxX - box.minX)) / 2.0);
		double y = box.minY + (((double) (box.maxY - box.minY)) / 2.0);
		double z = box.minZ + (((double) (box.maxZ - box.minZ)) / 2.0);
		return new Vec3d(x, y, z);
	}
}
