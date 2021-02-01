package net.snakefangox.worldshell.data;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Defines extra methods to transform between a local coord space
 * with this at 0, 0, 0 and the global coord space
 */
public class RelativeBlockPos extends BlockPos {

	public RelativeBlockPos(int i, int j, int k) {
		super(i, j, k);
	}

	public BlockPos toLocal(BlockPos pos) {
		return pos.subtract(this);
	}

	public BlockPos toGlobal(BlockPos pos) {
		return pos.add(this);
	}

	public BlockPos transferCoordSpace(BlockPos target, BlockPos pos) {
		return new BlockPos(target.getX() + (pos.getX() - getX()), target.getY() + (pos.getY() - getY()),
						target.getZ() + (pos.getZ() - getZ()));
	}

	public Vec3d transferCoordSpace(Vec3d target, Vec3d pos) {
		return new Vec3d(target.getX() + (pos.getX() - getX()), target.getY() + (pos.getY() - getY()),
						target.getZ() + (pos.getZ() - getZ()));
	}

	public BlockPos transferCoordSpace(Vec3d target, BlockPos pos) {
		return new BlockPos(target.getX() + (pos.getX() - getX()), target.getY() + (pos.getY() - getY()),
						target.getZ() + (pos.getZ() - getZ()));
	}

	public void makeBoxLocal(BlockBox box) {
		box.maxX = box.maxX - getX();
		box.minX = box.minX - getX();
		box.maxY = box.maxY - getY();
		box.minY = box.minY - getY();
		box.maxZ = box.maxZ - getZ();
		box.minZ = box.minZ - getZ();
	}

	public void makeBoxGlobal(BlockBox box) {
		box.maxX = box.maxX + getX();
		box.minX = box.minX + getX();
		box.maxY = box.maxY + getY();
		box.minY = box.minY + getY();
		box.maxZ = box.maxZ + getZ();
		box.minZ = box.minZ + getZ();
	}

	public void transformBoxCoordSpace(RelativeBlockPos target, BlockBox box) {
		makeBoxLocal(box);
		target.makeBoxGlobal(box);
	}

	public static RelativeBlockPos fromLong(long packedPos) {
		return new RelativeBlockPos(unpackLongX(packedPos), unpackLongY(packedPos), unpackLongZ(packedPos));
	}

	public static RelativeBlockPos toRelative(BlockPos pos) {
		return new RelativeBlockPos(pos.getX(), pos.getY(), pos.getZ());
	}

	public Vec3d subtract(double x, double y, double z) {
		return new Vec3d(getX() - x, getY() - y, getZ() - z);
	}
}
