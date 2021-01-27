package net.snakefangox.worldshell.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.data.RelativeBlockPos;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.storage.ShellStorageData;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ShellTransferHandler {

	private static final int FLAGS = 2 | 16 | 32 | 64;

	public static WorldLinkEntity transferToShell(ServerWorld world, RelativeBlockPos core, List<BlockPos> blocks) {
		return transferToShell(world, core, blocks, new WorldLinkEntity(WSUniversal.WORLD_LINK_ENTITY_TYPE, world));
	}

	//TODO Set this up the same way the clone command works
	public static <T extends WorldLinkEntity> T transferToShell(ServerWorld world, RelativeBlockPos core, List<BlockPos> blocks, T worldLinkEntity) {
		World shellWorld = world.getServer().getWorld(WSUniversal.STORAGE_DIM);
		ShellStorageData storageData = ShellStorageData.getOrCreate(world.getServer());
		RelativeBlockPos bayPos = storageData.getFreeBay();
		BlockBox bayBounds = BlockBox.empty();
		for (BlockPos bp : blocks) {
			BlockPos dest = core.transferCoordSpace(bayPos, bp);
			copyBlock(world, shellWorld, bp, dest);
			updateBoxBounds(bayBounds, bp);
		}
		worldLinkEntity.setBoundingBox(Box.from(bayBounds));
		blocks.forEach((bp) -> world.setBlockState(bp, Blocks.AIR.getDefaultState()));
		System.out.println("execute in worldshell:shell_storage run tp " + bayPos.toShortString().replace(",", ""));
		core.transformBoxCoordSpace(bayPos, bayBounds);
		int id = storageData.addBay(new ShellBay(bayPos, bayBounds));
		System.out.println("Real id:" + id + " Calced id:" + storageData.getBayIdFromPos(bayPos));
		worldLinkEntity.setShellId(id);
		return worldLinkEntity;
	}

	private static void copyBlock(World from, World to, BlockPos oldPos, BlockPos newPos) {
		BlockState state = from.getBlockState(oldPos);
		BlockEntity be = from.getBlockEntity(oldPos);
		if (state.getBlock() instanceof BlockEntityProvider) {
			// Remove old BE
			from.removeBlockEntity(oldPos);
			// Non-Mojang code may not check for null when deleting the block, so replace it with an empty BE
			from.addBlockEntity(((BlockEntityProvider) state.getBlock()).createBlockEntity(oldPos, state));
		}
		BlockStateArgument bsa = new BlockStateArgument(state, Collections.emptySet(), be != null ? be.toTag(new CompoundTag()) : null);
		bsa.setBlockState((ServerWorld) to, newPos, FLAGS);
	}

	public static void updateBoxBounds(BlockBox blockBox, BlockPos pos) {
		if (pos.getX() > blockBox.maxX) {
			blockBox.maxX = pos.getX();
		} else if (pos.getX() < blockBox.minX) {
			blockBox.minX = pos.getX();
		}
		if (pos.getY() > blockBox.maxY) {
			blockBox.maxY = pos.getY();
		} else if (pos.getY() < blockBox.minY) {
			blockBox.minY = pos.getY();
		}
		if (pos.getZ() > blockBox.maxZ) {
			blockBox.maxZ = pos.getZ();
		} else if (pos.getZ() < blockBox.minZ) {
			blockBox.minZ = pos.getZ();
		}
	}

	/**
	 * Iterates over a box and calls the function on every block
	 *
	 * @param box
	 * @param func
	 */
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
