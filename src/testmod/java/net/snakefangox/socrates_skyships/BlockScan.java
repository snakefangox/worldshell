package net.snakefangox.socrates_skyships;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class BlockScan implements Iterator<BlockPos> {
	private final World world;
	private final Queue<BlockPos> toCheck = new ArrayDeque<>();
	private final Set<BlockPos> added = new HashSet<>();
	private int counter = 0;

	public BlockScan(BlockPos start, World world) {
		this.world = world;
		toCheck.add(start);
	}

	@Override
	public boolean hasNext() {
		if (counter > 1000)
			return false;
		return !toCheck.isEmpty();
	}

	@Override
	public BlockPos next() {
		BlockPos blockPos = toCheck.remove();
		for (Direction dir : Direction.values()) {
			BlockPos offPos = blockPos.offset(dir);
			if (added.contains(offPos))
				continue;

			BlockState state = world.getBlockState(offPos);
			if (state.isAir() || state.isIn(BlockTags.DIRT) || state.isIn(BlockTags.CROPS) ||
					state.getHardness(world, offPos) < 0)
				continue;

			toCheck.add(offPos);
			added.add(offPos);
		}
		counter++;
		return blockPos;
	}
}
