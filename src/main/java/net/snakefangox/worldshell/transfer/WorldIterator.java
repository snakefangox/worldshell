package net.snakefangox.worldshell.transfer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

/**
 * A basic world iterator for {@link WorldShellConstructor}
 */
public abstract class WorldIterator implements Iterator<BlockPos> {

	protected final World world;
	protected final BlockPos.Mutable next = new BlockPos.Mutable();
	private final BlockPos.Mutable current = new BlockPos.Mutable();
	protected int index = 0;

	protected WorldIterator(World world, BlockPos start) {
		this.world = world;
		next.set(start);
	}

	@Override
	public boolean hasNext() {
		return index < getMax();
	}

	@Override
	public BlockPos next() {
		current.set(next);
		++index;
		getNext();
		return current;
	}

	protected abstract void getNext();

	protected abstract int getMax();
}
