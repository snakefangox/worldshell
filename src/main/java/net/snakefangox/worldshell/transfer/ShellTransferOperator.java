package net.snakefangox.worldshell.transfer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    private static final double ROTATE_BACK = 0.7071067;
    protected static final BlockState CLEAR_STATE = Blocks.AIR.getDefaultState();

    private final ServerWorld world;
    private int timeSpent = 0;

    private final NoOpPosWrapper posWrapper = new NoOpPosWrapper();
    private final Stack<Long> cleanUpPositions = new Stack<>();

    public ShellTransferOperator(ServerWorld world) {
        this.world = world;
    }

    int getTime() {
        return timeSpent;
    }

    void addTime(long amount) {
        timeSpent += amount;
    }

    ServerWorld getWorld() {
        return world;
    }

    abstract boolean isFinished();

    abstract void performPass();

    protected abstract LocalSpace getLocalSpace();

    protected abstract LocalSpace getRemoteSpace();

    public int compareTo(@NotNull ShellTransferOperator o) {
        return getTime() - o.getTime();
    }

    protected BlockRotation getBlockRotation(Matrix3d rotation) {
        double x = rotation.transformX(ROTATE_BACK, 0.0, ROTATE_BACK);
        double z = rotation.transformZ(ROTATE_BACK, 0.0, ROTATE_BACK);
        if (x > 0) {
            if (z > 0) {
                return BlockRotation.NONE;
            } else {
                return BlockRotation.CLOCKWISE_90;
            }
        } else {
            if (z > 0) {
                return BlockRotation.COUNTERCLOCKWISE_90;
            } else {
                return BlockRotation.CLOCKWISE_180;
            }
        }
    }

    protected void transferBlock(World from, World to, BlockPos pos) {
        transferBlock(from, to, pos, true, RotationSolver.ORIGINAL, Matrix3d.IDENTITY, BlockRotation.NONE, ConflictSolver.OVERWRITE);
    }

    protected void transferBlock(World from, World to, BlockPos pos, boolean cleanUpRequired,
                                 RotationSolver rotationSolver, Matrix3d rotation, BlockRotation blockRotation, ConflictSolver conflictSolver) {
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
