package net.snakefangox.worldshell.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.entity.WorldShellEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Worldshell works with a lot of different coordinate spaces and needs to convert between them,
 * This class contains a lot of static helper methods for doing so. <p>
 * Sorry for the repetition, Vec3i and Vec3d share no common child so I'm stuck with this huge mess.
 */
//TODO This needs to die
public class CoordUtil {

	public static final BlockPos BP_ZERO = new BlockPos(0, 0, 0);

	public static Vec3d getBoxCenter(BlockBox box) {
		double x = box.minX + (((double) (box.maxX - box.minX)) / 2.0);
		double y = box.minY + (((double) (box.maxY - box.minY)) / 2.0);
		double z = box.minZ + (((double) (box.maxZ - box.minZ)) / 2.0);
		return new Vec3d(x, y, z);
	}

	public static BlockPos toLocal(BlockPos center, BlockPos pos) {
		return pos.subtract(center);
	}

	public static BlockPos transferCoordSpace(BlockPos current, BlockPos target, BlockPos pos) {
		return new BlockPos(target.getX() + (pos.getX() - current.getX()), target.getY() + (pos.getY() - current.getY()),
				target.getZ() + (pos.getZ() - current.getZ()));
	}

	public static void transformBoxCoordSpace(BlockPos current, BlockPos target, BlockBox box) {
		makeBoxLocal(current, box);
		makeBoxGlobal(target, box);
	}

	public static void makeBoxLocal(BlockPos current, BlockBox box) {
		box.maxX = box.maxX - current.getX();
		box.minX = box.minX - current.getX();
		box.maxY = box.maxY - current.getY();
		box.minY = box.minY - current.getY();
		box.maxZ = box.maxZ - current.getZ();
		box.minZ = box.minZ - current.getZ();
	}

	public static void makeBoxGlobal(BlockPos current, BlockBox box) {
		box.maxX = box.maxX + current.getX();
		box.minX = box.minX + current.getX();
		box.maxY = box.maxY + current.getY();
		box.minY = box.minY + current.getY();
		box.maxZ = box.maxZ + current.getZ();
		box.minZ = box.minZ + current.getZ();
	}
}
