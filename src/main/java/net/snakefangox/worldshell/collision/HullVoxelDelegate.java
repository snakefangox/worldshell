package net.snakefangox.worldshell.collision;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

public class HullVoxelDelegate extends VoxelShape {

	private final ShellCollisionHull hull;

	public HullVoxelDelegate(ShellCollisionHull hull) {
		super(new VoxelSetDelegate(hull));
		this.hull = hull;
	}

	@Override
	protected DoubleList getPointPositions(Direction.Axis axis) {
		return new DoubleArrayList(new double[]{hull.getMin(axis), hull.getMax(axis)});
	}

	@Override
	public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
		return hull.calculateMaxDistance(axis, box, maxDist);
	}

	static class VoxelSetDelegate extends VoxelSet {
		private final ShellCollisionHull hull;

		protected VoxelSetDelegate(ShellCollisionHull hull) {
			super(0, 0, 0);
			this.hull = hull;
		}

		@Override
		public boolean contains(int x, int y, int z) {
			return hull.contains(x, y, z);
		}

		@Override
		public void set(int x, int y, int z) {
			//No
		}

		@Override
		public int getMin(Direction.Axis axis) {
			return (int) hull.getMin(axis);
		}

		@Override
		public int getMax(Direction.Axis axis) {
			return (int) hull.getMax(axis);
		}
	}
}
