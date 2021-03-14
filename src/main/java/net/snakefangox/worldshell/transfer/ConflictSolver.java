package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * Determines what should happen in the event that a deconstructing
 * worldshell intersects with a non-replaceable block.
 * There are a few options that you may want to occur, I've tried my best to provide them all here.
 * Of course should the ones here not fit you can write your own.
 */
@FunctionalInterface
public interface ConflictSolver {

	/** Overwrites the world with the worldshell */
	ConflictSolver OVERWRITE = ConflictSolver::solveConflictOverwrite;

	/** Overwrites the worldshell with the world */
	ConflictSolver UNDERWRITE = ConflictSolver::solveConflictUnderwrite;

	/** Places the hardest block */
	ConflictSolver HARDNESS = ConflictSolver::solveConflictHardness;

	/** Places the most blast resistant block */
	ConflictSolver RESISTANCE = ConflictSolver::solveConflictResistance;

	/** Just explodes and places neither :tiny_potato: */
	ConflictSolver EXPLOSION = ConflictSolver::solveConflictExplosion;

	/**
	 * Solves a block overlap during deconstruction
	 *
	 * @param world         the world being deconstructed into
	 * @param pos           the position the conflict took place
	 * @param shellState    the state the worldshell would like to be there
	 * @param existingState the block that already exists
	 * @return the state that should be place there
	 */
	BlockState solveConflict(World world, BlockPos pos, BlockState shellState, BlockState existingState);

	static BlockState solveConflictOverwrite(World world, BlockPos pos, BlockState shellState, BlockState existingState) {
		return shellState;
	}

	static BlockState solveConflictUnderwrite(World world, BlockPos pos, BlockState shellState, BlockState existingState) {
		return existingState;
	}

	static BlockState solveConflictHardness(World world, BlockPos pos, BlockState shellState, BlockState existingState) {
		return shellState.getHardness(world, pos) < existingState.getHardness(world, pos) ? existingState : shellState;
	}

	static BlockState solveConflictResistance(World world, BlockPos pos, BlockState shellState, BlockState existingState) {
		return shellState.getBlock().getBlastResistance() < existingState.getBlock().getBlastResistance() ? existingState : shellState;
	}

	static BlockState solveConflictExplosion(World world, BlockPos pos, BlockState shellState, BlockState existingState) {
		world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3, Explosion.DestructionType.BREAK);
		return Blocks.AIR.getDefaultState();
	}
}
