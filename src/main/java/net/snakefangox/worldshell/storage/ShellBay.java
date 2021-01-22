package net.snakefangox.worldshell.storage;

import java.util.Optional;

import net.snakefangox.worldshell.data.RelativeBlockPos;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.math.BlockBox;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a shell in the storage dimension
 */
public class ShellBay {

    //The center of the shell
    private RelativeBlockPos center;

    //Defines the box the shell fits within
    private BlockBox bounds;

    //The entity changes to this shell should propagate to
    private Optional<WorldLinkEntity> linkedEntity = Optional.empty();

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

	public void linkEntity(@NotNull WorldLinkEntity worldLinkEntity) {
        linkedEntity = Optional.of(worldLinkEntity);
	}
}
