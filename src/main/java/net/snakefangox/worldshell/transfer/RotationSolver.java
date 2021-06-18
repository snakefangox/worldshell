package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.snakefangox.worldshell.collision.RotationHelper;
import oimo.common.Mat3;
import oimo.common.Quat;
import oimo.common.Vec3;

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
	RotationSolver CARDINAL = RotationSolver::solveCardinalRotation;
	/** Returns the worldshell rotated exactly, note: probably bad, don't use without testing */
	RotationSolver TRUE = RotationSolver::solveTrueRotation;

	//TODO check this is correct
	Mat3[] BLOCK_ROTATIONS = new Mat3[] {RotationHelper.identityMat3(), RotationHelper.identityMat3().appendRotationEq(1.5708, 0, 1, 0),
			RotationHelper.identityMat3().appendRotationEq(Math.PI, 0, 1, 0), RotationHelper.identityMat3().appendRotationEq(-1.5708, 0, 1, 0)};

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
	BlockState solveRotation(Mat3 rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState);

	static BlockState solveOriginalRotation(Mat3 rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		return oldState;
	}

	static BlockState solveCardinalRotation(Mat3 rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		blockRotateBlockPos(blockRotation, pos);
		return oldState.rotate(blockRotation);
	}

	static BlockState solveTrueRotation(Mat3 rotation, BlockRotation blockRotation, BlockPos.Mutable pos, BlockState oldState) {
		Vec3 vec = new Vec3((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()).mulMat3Eq(rotation);
		pos.set(vec.x, vec.y, vec.z);
		return oldState.rotate(blockRotation);
	}

	static void blockRotateBlockPos(BlockRotation blockRotation, BlockPos.Mutable pos) {
		Mat3 m = BLOCK_ROTATIONS[blockRotation.ordinal()];
		Vec3 vec = new Vec3((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()).mulMat3Eq(m);
		pos.set(vec.x, vec.y, vec.z);
	}
}
