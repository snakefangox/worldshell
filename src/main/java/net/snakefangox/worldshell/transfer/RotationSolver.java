package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

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
	Matrix3d[] BLOCK_ROTATIONS = new Matrix3d[] {Matrix3d.IDENTITY, new Matrix3d(new QuaternionD(0, 90, 0, true)),
			new Matrix3d(new QuaternionD(0, 180, 0, true)), new Matrix3d(new QuaternionD(0, -90, 0, true))};

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
	BlockState solveRotation(Matrix3d rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState);

	static BlockState solveOriginalRotation(Matrix3d rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		return oldState;
	}

	static BlockState solveCardinalRotation(Matrix3d rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		blockRotateBlockPos(blockRotation, pos);
		return oldState.rotate(blockRotation);
	}

	static BlockState solveTrueRotation(Matrix3d rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		pos.set(rotation.transformX(pos.getX(), pos.getY(), pos.getZ()), rotation.transformY(pos.getX(), pos.getY(), pos.getZ()),
				rotation.transformZ(pos.getX(), pos.getY(), pos.getZ()));
		return oldState.rotate(blockRotation);
	}

	static void blockRotateBlockPos(BlockRotation blockRotation, BlockPos.Mutable pos) {
		Matrix3d m = BLOCK_ROTATIONS[blockRotation.ordinal()];
		pos.set(m.transformX(pos.getX(), pos.getY(), pos.getZ()), m.transformY(pos.getX(), pos.getY(), pos.getZ()),
				m.transformZ(pos.getX(), pos.getY(), pos.getZ()));
	}
}
