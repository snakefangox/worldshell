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
import net.snakefangox.worldshell.util.CoordUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Worldshell entities are complex things, to create one there are a huge number of steps
 * that need to be performed. As such creating one isn't as simple as calling new WorldShellEntity().
 * This is a rather non-optional helper class to handle all of that for you.
 */
public class ShellTransferHandlerOld {

	private static final int FLAGS = 2 | 16 | 32 | 64;

	public static WorldShellEntity transferToShell(ServerWorld world, BlockPos core, List<BlockPos> blocks) {
		return transferToShell(world, core, blocks, new WorldShellEntity(WorldShell.WORLD_LINK_ENTITY_TYPE, world));
	}

	//TODO Set this up the same way the clone command works, create an object that implements LocalSpace to hold blocks to be moved
	public static <T extends WorldShellEntity> T transferToShell(ServerWorld world, BlockPos core, List<BlockPos> blocks, T worldLinkEntity) {
		World shellWorld = WorldShell.getStorageDim(world.getServer());
		ShellStorageData storageData = ShellStorageData.getOrCreate(world.getServer());
		BlockPos bayPos = storageData.getFreeBay();
		BlockBox bayBounds = BlockBox.empty();
		Bay bay = new Bay(bayPos, bayBounds);
		for (BlockPos bp : blocks) {
			BlockPos dest = CoordUtil.transferCoordSpace(core, bayPos, bp);
			copyBlock(world, shellWorld, bp, dest);
			updateBoxBounds(bayBounds, bp);
		}
		EntityBounds dimensions = new EntityBounds(bayBounds.getBlockCountX(), bayBounds.getBlockCountY(), bayBounds.getBlockCountZ(), false);
		worldLinkEntity.setDimensions(dimensions);
		Vec3d center = CoordUtil.getBoxCenter(bayBounds);
		worldLinkEntity.setPosition(center.getX() + 0.5, bayBounds.minY, center.getZ() + 0.5);
		worldLinkEntity.setBlockOffset(new Vec3d((core.getX() - center.x) - 0.5, core.getY() - bayBounds.minY, (core.getZ() - center.z) - 0.5));
		blocks.forEach((bp) -> world.setBlockState(bp, Blocks.AIR.getDefaultState()));
		CoordUtil.transformBoxCoordSpace(core, bayPos, bayBounds);
		int id = storageData.addBay(bay);
		storageData.getBay(id).loadAllChunks(world.getServer());
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
		BlockStateArgument bsa = new BlockStateArgument(state, Collections.emptySet(), be != null ? be.writeNbt(new CompoundTag()) : null);
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