package net.snakefangox.worldshell.transfer;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

/**
 * One of the provided Iterators, this one iterates over all
 * the blocks in a {@link BlockBox}
 */
public class BlockBoxIterator implements Iterator<BlockPos> {

	private final Iterator<BlockPos> iterator;

	private BlockBoxIterator(BlockBox box) {
		iterator = BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(),
				box.getMaxX(), box.getMaxY(), box.getMaxZ()).iterator();
	}

	public static BlockBoxIterator of(BlockBox box) {
		return new BlockBoxIterator(box);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public BlockPos next() {
		return iterator.next();
	}
}
