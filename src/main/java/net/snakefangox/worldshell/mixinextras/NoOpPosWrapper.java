package net.snakefangox.worldshell.mixinextras;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * When passed to {@link net.minecraft.world.World#setBlockState(BlockPos, BlockState, int, int)}
 * will prevent blocks from calling {@link net.minecraft.block.Block#onStateReplaced(BlockState, World, BlockPos, BlockState, boolean)}
 * or {@link net.minecraft.block.Block#onBlockAdded(BlockState, World, BlockPos, BlockState, boolean)}.
 * <p>
 * Intended to allow easy transfer to the shell storage dimension, calling the method with this class effectively
 * transfers the block as-is. Still need to do the correct flags to perfectly pull it off and we have to update
 * the neighbours and remove the {@link net.minecraft.block.entity.BlockEntity}s of the blocks that get moved this way for them.
 */
public class NoOpPosWrapper extends BlockPos.Mutable {
}
