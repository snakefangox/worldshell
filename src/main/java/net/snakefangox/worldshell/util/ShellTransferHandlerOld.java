package net.snakefangox.worldshell.util;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShell;
import net.snakefangox.worldshell.collision.EntityBounds;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.ShellStorageData;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Worldshell entities are complex things, to create one there are a huge number of steps
 * that need to be performed. As such creating one isn't as simple as calling new WorldShellEntity().
 * This is a rather non-optional helper class to handle all of that for you.
 */
public class ShellTransferHandlerOld {

	/**
	 * Iterates over a box and calls the function on every block
	 *
	 * @param box
	 * @param func
	 */
	//TODO replace with the iterator
	public static void forEachInBox(BlockBox box, Consumer<BlockPos.Mutable> func) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int x = box.minX; x <= box.maxX; ++x) {
			for (int y = box.minY; y <= box.maxY; ++y) {
				for (int z = box.minZ; z <= box.maxZ; ++z) {
					func.accept(mutable.set(x, y, z));
				}
			}
		}
	}
}
