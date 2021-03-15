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

	public BlockBoxIterator(BlockBox box) {
		currentPos = new BlockPos.Mutable();
		this.box = box;
		x = box.minX;
		y = box.minY;
		z = box.minZ;
	}

	@Override
	public boolean hasNext() {
		return x < box.maxX || z < box.maxZ || y < box.maxY;
	}

	@Override
	public BlockPos next() {
		if (x > box.maxX) {
			x = box.minX;
			++y;
		}
		if (y > box.maxY) {
			y = box.minY;
			++z;
		}
		return currentPos.set(x++, y, z);
	}
}
