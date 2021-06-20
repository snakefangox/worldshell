package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;
import net.snakefangox.worldshell.mixinextras.NoOpPosWrapper;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.LocalSpace;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * Shell operations can take a while. This is a basic class that the {@link ShellTransferHandler}
 * can use to perform them over time.
 */
public abstract class ShellTransferOperator implements Comparable<ShellTransferOperator> {

	protected static final int MAX_OPS = 200;
	protected static final int FLAGS = 2 | 16 | 32 | 64;
	protected static final int UPDATE_DEPTH = 3;
	protected static final BlockState CLEAR_STATE = Blocks.AIR.getDefaultState();
	private static final float ROTATE_BACK = 0.7071067f;
	private final ServerWorld world;
	private final NoOpPosWrapper posWrapper = new NoOpPosWrapper();
	private final Stack<Long> cleanUpPositions = new Stack<>();
	private int timeSpent = 0;

	public ShellTransferOperator(ServerWorld world) {
		this.world = world;
	}

	void addTime(long amount) {
		timeSpent += amount;
	}

	ServerWorld getWorld() {
		return world;
	}

	abstract boolean isFinished();

	abstract void performPass();

	public int compareTo(@NotNull ShellTransferOperator o) {
		return getTime() - o.getTime();
	}

	int getTime() {
		return timeSpent;
	}

	protected BlockRotation getBlockRotation(Quaternion rotation) {
		Vector3d vec = rotation.multLocal(new Vector3d(ROTATE_BACK, 0f, ROTATE_BACK));
		if (vec.x > 0) {
			if (vec.z > 0) {
				return BlockRotation.NONE;
			} else {
				return BlockRotation.CLOCKWISE_90;
			}
		} else {
			if (vec.z > 0) {
				return BlockRotation.COUNTERCLOCKWISE_90;
			} else {
				return BlockRotation.CLOCKWISE_180;
			}
		}
	}

	protected void transferBlock(World from, World to, BlockPos pos) {
		transferBlock(from, to, pos, true, RotationSolver.ORIGINAL, Quaternion.IDENTITY, BlockRotation.NONE, ConflictSolver.OVERWRITE);
	}

	protected void transferBlock(World from, World to, BlockPos pos, boolean cleanUpRequired,
								 RotationSolver rotationSolver, Quaternion rotation, BlockRotation blockRotation, ConflictSolver conflictSolver) {
		BlockState state = from.getBlockState(pos);
		if (state.isAir()) return;
		posWrapper.set(pos);
		getLocalSpace().globalToGlobal(getRemoteSpace(), posWrapper);
		state = rotationSolver.solveRotation(rotation, blockRotation, posWrapper, state);
		BlockState currentState = to.getBlockState(posWrapper);
		if (!currentState.getMaterial().isReplaceable())
			state = conflictSolver.solveConflict(to, posWrapper, state, currentState);
		to.setBlockState(posWrapper, state, FLAGS, 0);
		if (getRemoteSpace() instanceof Bay) ((Bay) getRemoteSpace()).updateBoxBounds(posWrapper);
		if (state.hasBlockEntity()) {
			BlockEntity blockEntity = from.getBlockEntity(pos);
			if (blockEntity != null) transferBlockEntity(from, to, pos, posWrapper.toImmutable(), blockEntity, state);
		}
		posWrapper.set(pos);
		from.setBlockState(posWrapper, CLEAR_STATE, FLAGS, 0);
		if (cleanUpRequired) cleanUpPositions.push(posWrapper.asLong());
	}

	protected abstract LocalSpace getLocalSpace();

	protected abstract LocalSpace getRemoteSpace();

	protected void transferBlockEntity(World from, World to, BlockPos oldPos, BlockPos newPos, BlockEntity blockEntity, BlockState state) {
		NbtCompound nbt = new NbtCompound();
		blockEntity.writeNbt(nbt);
		from.removeBlockEntity(oldPos);
		nbt.putInt("x", newPos.getX());
		nbt.putInt("y", newPos.getY());
		nbt.putInt("z", newPos.getZ());
		BlockEntity newBlockEntity = to.getBlockEntity(newPos);
		if (newBlockEntity != null) {
			newBlockEntity.readNbt(nbt);
		} else {
			newBlockEntity = BlockEntity.createFromNbt(newPos, state, nbt);
			if (newBlockEntity != null) {
				newBlockEntity.readNbt(nbt);
				to.addBlockEntity(newBlockEntity);
			}
		}
	}

	protected boolean cleanUpRemaining() {
		return !cleanUpPositions.isEmpty();
	}

	protected void cleanUpStepUpdate(World toClean) {
		toClean.getBlockState(posWrapper.set(cleanUpPositions.pop())).updateNeighbors(world, posWrapper, FLAGS, UPDATE_DEPTH);
	}

}
