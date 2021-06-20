package net.snakefangox.worldshell.transfer;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

/**
 * One of the provided Iterators, this one iterates over all
 * the blocks in a {@link BlockBox}
 */
public class BlockBoxIterator implements Iterator<BlockPos> {

	private final BlockBox box;
	private final BlockPos.Mutable currentPos;
	private int x, y, z;

	private BlockBoxIterator(BlockBox box) {
		currentPos = new BlockPos.Mutable();
		this.box = box;
		x = box.getMinX();
		y = box.getMinY();
		z = box.getMinZ();
	}

	public static BlockBoxIterator of(BlockBox box) {
		return new BlockBoxIterator(box);
	}

	@Override
	public boolean hasNext() {
		return x < box.getMaxX() || z < box.getMaxZ() || y < box.getMaxY();
	}

	@Override
	public BlockPos next() {
		if (x > box.getMaxX()) {
			x = box.getMinX();
			++y;
		}
		if (y > box.getMaxY()) {
			y = box.getMinY();
			++z;
		}
		return currentPos.set(x++, y, z);
	}
}
