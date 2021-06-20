package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;

/**
 * When returning a worldshell to the world we cannot preserve rotation.
 * There are a few options that you may want to occur, I've tried my best to provide them all here.
 * Of course should the ones here not fit you can write your own.
 */
@FunctionalInterface
public interface RotationSolver {

	/** Returns the worldshell in the exact orientation it was in originally */
	RotationSolver ORIGINAL = RotationSolver::solveOriginalRotation;
	/** Returns the worldshell rotated to the closest cardinal direction */
	RotationSolver CARDINAL = RotationSolver::solveOriginalRotation;
	/** Returns the worldshell rotated exactly, note: probably bad, don't use without testing */
	RotationSolver TRUE = RotationSolver::solveTrueRotation;

	//TODO check this is correct
	Quaternion[] BLOCK_ROTATIONS = new Quaternion[] {Quaternion.IDENTITY, new Quaternion().fromAngles(0, (Math.PI / 2f), 0),
			new Quaternion().fromAngles(0, (Math.PI), 0), new Quaternion().fromAngles(0, (-Math.PI / 2f), 0)};

	/**
	 * Given the position and state of a block determines where it should go and
	 * if the state should be changed
	 *
	 * @param rotation      the rotation of the worldshell
	 * @param blockRotation the distance the worldshell has rotated clamped to block rotation
	 * @param pos           the current position of the block. <b>This should be set to the calculated destination</b>
	 * @param oldState      the current state of the block
	 * @return the state the block should be in when placed
	 */
	BlockState solveRotation(Quaternion rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState);

	static BlockState solveOriginalRotation(Quaternion rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		return oldState;
	}

	static BlockState solveCardinalRotation(Quaternion rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		blockRotateBlockPos(blockRotation, pos);
		return oldState.rotate(blockRotation);
	}

	static BlockState solveTrueRotation(Quaternion rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		rotation.multLocal(vec);
		pos.set(vec.x, vec.y, vec.z);
		return oldState.rotate(blockRotation);
	}

	static void blockRotateBlockPos(BlockRotation blockRotation, BlockPos.Mutable pos) {
		Quaternion m = BLOCK_ROTATIONS[blockRotation.ordinal()];
		Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		m.multLocal(vec);
		pos.set(vec.x, vec.y, vec.z);
	}
}