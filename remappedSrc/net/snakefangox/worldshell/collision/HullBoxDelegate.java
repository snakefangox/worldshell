package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.Optional;

/**
 * Vanilla thinks it can change the size of our bounding box
 * It can but it can't create new ones, so we have this to point back to the original.
 */
public class HullBoxDelegate extends Box implements SpecialBox {

	private final ShellCollisionHull hull;

	public HullBoxDelegate(Box box, ShellCollisionHull hull) {
		super(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		this.hull = hull;
	}

	@Override
	public Box shrink(double x, double y, double z) {
		return new HullBoxDelegate(super.shrink(x, y, z), hull);
	}

	@Override
	public Box stretch(double x, double y, double z) {
		return new HullBoxDelegate(super.stretch(x, y, z), hull);
	}

	@Override
	public Box expand(double x, double y, double z) {
		return new HullBoxDelegate(super.expand(x, y, z), hull);
	}

	@Override
	public Box intersection(Box box) {
		return new HullBoxDelegate(super.intersection(box), hull);
	}

	@Override
	public Box union(Box box) {
		return new HullBoxDelegate(super.union(box), hull);
	}

	@Override
	public Box offset(double x, double y, double z) {
		return new HullBoxDelegate(super.offset(x, y, z), hull);
	}

	@Override
	public Box offset(BlockPos blockPos) {
		return new HullBoxDelegate(super.offset(blockPos), hull);
	}

	@Override
	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return hull.intersects(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return hull.contains(x, y, z);
	}

	@Override
	public Optional<Vec3d> raycast(Vec3d min, Vec3d max) {
		return hull.raycast(min, max);
	}

	@Override
	public VoxelShape toVoxelShape() {
		return hull.toVoxelShape();
	}
}
