package net.snakefangox.worldshell.data;

import net.minecraft.util.math.BlockPos;

/**
 * Defines extra methods to transform between a local coord space
 * with this at 0, 0, 0 and the global coord space
 */
public class RelativeBlockPos extends BlockPos {

    public RelativeBlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos toLocal(BlockPos pos) {
        return pos.subtract(this);
    }

    public BlockPos toGlobal(BlockPos pos){ return pos.add(this); }

    public BlockPos transferCoordSpace(RelativeBlockPos target, BlockPos pos) {
        return new BlockPos(target.getX() + (pos.getX() - getX()), target.getY() + (pos.getY() - getY()),
                target.getZ() + (pos.getZ() - getZ()));
    }

    public static RelativeBlockPos fromLong(long packedPos) {
        return new RelativeBlockPos(unpackLongX(packedPos), unpackLongY(packedPos), unpackLongZ(packedPos));
    }

    public static RelativeBlockPos toRelative(BlockPos pos) {
        return new RelativeBlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
