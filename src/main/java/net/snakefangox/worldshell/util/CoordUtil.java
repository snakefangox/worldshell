package net.snakefangox.worldshell.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.snakefangox.worldshell.collision.Matrix3D;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.entity.WorldLinkEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Worldshell works with a lot of different coordinate spaces and needs to convert between them,
 * This class contains a lot of static helper methods for doing so. <p>
 * Sorry for the repetition, Vec3i and Vec3d share no common child so I'm stuck with this huge mess.
 */
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

	public static BlockPos toGlobal(BlockPos center, BlockPos pos) {
		return pos.add(center);
	}

	public static Vec3d toLocal(Vec3d center, Vec3d pos) {
		return pos.subtract(center);
	}

	public static Vec3d toGlobal(Vec3d center, Vec3d pos) {
		return pos.add(center);
	}

	public static Vec3d toGlobal(BlockPos center, Vec3d pos) {
		return pos.add(center.getX(), center.getY(), center.getZ());
	}

	public static Vec3d transferCoordSpace(BlockPos current, Vec3d target, Vec3d pos) {
		return new Vec3d(target.getX() + (pos.getX() - current.getX()), target.getY() + (pos.getY() - current.getY()),
				target.getZ() + (pos.getZ() - current.getZ()));
	}

	public static Vec3d transferCoordSpace(Vec3d current, Vec3d target, Vec3d pos) {
		return new Vec3d(target.getX() + (pos.getX() - current.getX()), target.getY() + (pos.getY() - current.getY()),
				target.getZ() + (pos.getZ() - current.getZ()));
	}

	public static BlockPos transferCoordSpace(BlockPos current, BlockPos target, BlockPos pos) {
		return new BlockPos(target.getX() + (pos.getX() - current.getX()), target.getY() + (pos.getY() - current.getY()),
				target.getZ() + (pos.getZ() - current.getZ()));
	}

	public static Vec3d linkEntityToWorld(BlockPos current, WorldLinkEntity target, Vec3d pos) {
		return new Vec3d(target.getX() + target.getBlockOffset().x + (pos.getX() - current.getX()),
				target.getY() + target.getBlockOffset().y + (pos.getY() - current.getY()),
				target.getZ() + target.getBlockOffset().z + (pos.getZ() - current.getZ()));
	}

	public static BlockPos linkEntityToWorld(BlockPos current, WorldLinkEntity target, BlockPos pos) {
		return new BlockPos(target.getX() + target.getBlockOffset().x + (pos.getX() - current.getX()),
				target.getY() + target.getBlockOffset().y + (pos.getY() - current.getY()),
				target.getZ() + target.getBlockOffset().z + (pos.getZ() - current.getZ()));
	}

	public static Vec3d linkEntityToWorld(BlockPos current, WorldLinkEntity target, double posX, double posY, double posZ) {
		return new Vec3d(target.getX() + target.getBlockOffset().x + (posX - current.getX()),
				target.getY() + target.getBlockOffset().y + (posY - current.getY()),
				target.getZ() + target.getBlockOffset().z + (posZ - current.getZ()));
	}

	public static Vec3d worldToLinkEntity(WorldLinkEntity entity, Vec3d pos) {
		return new Vec3d(pos.x - (entity.getX() + entity.getBlockOffset().x),
				pos.y - (entity.getY() + entity.getBlockOffset().y),
				pos.z - (entity.getZ() + entity.getBlockOffset().z));
	}

	public static void worldToLinkEntity(WorldLinkEntity entity, ShellCollisionHull.Vec3dM vec) {
		vec.setAll(vec.x - (entity.getX() + entity.getBlockOffset().x),
				vec.y - (entity.getY() + entity.getBlockOffset().y),
				vec.z - (entity.getZ() + entity.getBlockOffset().z));
	}

	public static List<Box> getTransformedBoxesFromVoxelShape(VoxelShape voxelShape, double xOff, double yOff, double zOff) {
		List<Box> list = new ArrayList<>();
		voxelShape.forEachBox((x1, y1, z1, x2, y2, z2) ->
				list.add(new Box(x1 + xOff, y1 + yOff, z1 + zOff, x2 + xOff, y2 + yOff, z2 + zOff)));
		return list;
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

	public static void transformBoxCoordSpace(BlockPos current, BlockPos target, BlockBox box) {
		makeBoxLocal(current, box);
		makeBoxGlobal(target, box);
	}
}
