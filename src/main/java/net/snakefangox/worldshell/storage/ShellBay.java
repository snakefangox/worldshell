package net.snakefangox.worldshell.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.math.BlockBox;
import net.snakefangox.worldshell.data.RelativeBlockPos;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a shell in the storage dimension
 */
public class ShellBay {

    //The center of the shell
    private RelativeBlockPos center;

    //Defines the box the shell fits within
    private BlockBox bounds;

    public ShellBay(RelativeBlockPos center, BlockBox bounds) {
        this.center = center;
        this.bounds = bounds;
    }

    public ShellBay(CompoundTag tag) {
        fromTag(tag);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("center", center.asLong());
        tag.put("bounds", bounds.toNbt());
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        center = RelativeBlockPos.fromLong(tag.getLong("center"));
        bounds = new BlockBox(((IntArrayTag) tag.get("bounds")).getIntArray());
    }

    public BlockBox getBox() {
        return bounds;
    }
}
