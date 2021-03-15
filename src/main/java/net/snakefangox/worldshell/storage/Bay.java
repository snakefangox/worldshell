package net.snakefangox.worldshell.storage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShell;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.util.ShellTransferHandlerOld;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a Worldshell in the storage dimension
 */
public class Bay implements LocalSpace {

	/** The center of the shell */
	private BlockPos center;

	/** Defines the box the shell fits within */
	private BlockBox bounds;

	/** The entity changes to this shell should propagate to */
	private WorldShellEntity linkedEntity = null;

	/** Calls on the {@link ShellStorageData} holding this */
	Runnable markDirtyFunc = () -> {};

	public Bay(BlockPos center, BlockBox bounds) {
		this.center = center;
		this.bounds = bounds;
	}

	public Bay(CompoundTag tag) {
		fromTag(tag);
	}

	public void fromTag(CompoundTag tag) {
		center = BlockPos.fromLong(tag.getLong("center"));
		bounds = new BlockBox(((IntArrayTag) tag.get("bounds")).getIntArray());
	}

	public PacketByteBuf createClientPacket(MinecraftServer server, PacketByteBuf buf) {
		World world = WorldShell.getStorageDim(server);
		Map<BlockState, List<BlockPos>> stateListMap = new HashMap<>();
		List<BlockEntity> blockEntities = new ArrayList<>();
		ShellTransferHandlerOld.forEachInBox(bounds, (bp) -> {
			BlockState state = world.getBlockState(bp);
			if (!state.isAir()) {
				if (stateListMap.containsKey(state)) {
					stateListMap.get(state).add(bp.toImmutable());
				} else {
					List<BlockPos> list = new ArrayList<BlockPos>();
					list.add(bp.toImmutable());
					stateListMap.put(state, list);
				}
				if (state.hasBlockEntity()) {
					BlockEntity be = world.getBlockEntity(bp);
					if (be != null && be.toUpdatePacket() != null) blockEntities.add(be);
				}
			}
		});
		return WorldShellPacketHelper.writeBlocks(buf, stateListMap, blockEntities, center);
	}

	public void loadAllChunks(MinecraftServer server) {
		ServerWorld world = WorldShell.getStorageDim(server);
		ChunkPos.stream(new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.minX), ChunkSectionPos.getSectionCoord(bounds.minZ)),
				new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.maxX), ChunkSectionPos.getSectionCoord(bounds.maxZ)))
				.forEach(chunkPos -> world.setChunkForced(chunkPos.x, chunkPos.z, true));
	}

	private void onBoundsUpdate() {
		markDirty();
	}

	public Vec3d toEntityGlobalSpace(Vec3d vec) {
		return globalToGlobal(linkedEntity, vec);
	}

	public Vec3d toEntityGlobalSpace(double x, double y, double z) {
		return globalToGlobal(linkedEntity, x, y, z);
	}

	public BlockPos toEntityGlobalSpace(BlockPos pos) {
		return globalToGlobal(linkedEntity, pos);
	}

	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putLong("center", center.asLong());
		tag.put("bounds", bounds.toNbt());
		return tag;
	}

	public void markDirty() {
		markDirtyFunc.run();
	}

	public BlockBox getBounds() {
		return bounds;
	}

	public void linkEntity(@NotNull WorldShellEntity worldShellEntity) {
		linkedEntity = worldShellEntity;
		//TODO Move to a better place
		loadAllChunks(worldShellEntity.world.getServer());
		if (worldShellEntity.getMicrocosm().isEmpty()) {
			fillServerWorldShell(worldShellEntity);
		}
	}

	private void fillServerWorldShell(WorldShellEntity entity) {
		World world = WorldShell.getStorageDim(entity.world.getServer());
		Map<BlockPos, BlockState> stateMap = new HashMap<>();
		ShellTransferHandlerOld.forEachInBox(bounds, (bp) -> {
			BlockState state = world.getBlockState(bp);
			if (!state.isAir())
				stateMap.put(toLocal(bp), state);
		});
		entity.initializeWorldShell(stateMap, null, null);
	}

	public Optional<WorldShellEntity> getLinkedEntity() {
		return Optional.ofNullable(linkedEntity);
	}

	public BlockPos getCenter() {
		return center;
	}

	public Vec3d getBoundsCenter() {
		double x = bounds.minX + (((double) (bounds.maxX - bounds.minX)) / 2.0);
		double y = bounds.minY + (((double) (bounds.maxY - bounds.minY)) / 2.0);
		double z = bounds.minZ + (((double) (bounds.maxZ - bounds.minZ)) / 2.0);
		return new Vec3d(x, y, z);
	}

	public void updateBoxBounds(BlockPos pos) {
		boolean hasChanged = false;
		if (pos.getX() > bounds.maxX) {
			bounds.maxX = pos.getX();
			hasChanged = true;
		} else if (pos.getX() < bounds.minX) {
			bounds.minX = pos.getX();
			hasChanged = true;
		}
		if (pos.getY() > bounds.maxY) {
			bounds.maxY = pos.getY();
			hasChanged = true;
		} else if (pos.getY() < bounds.minY) {
			bounds.minY = pos.getY();
			hasChanged = true;
		}
		if (pos.getZ() > bounds.maxZ) {
			bounds.maxZ = pos.getZ();
			hasChanged = true;
		} else if (pos.getZ() < bounds.minZ) {
			bounds.minZ = pos.getZ();
			hasChanged = true;
		}
		if (hasChanged) onBoundsUpdate();
	}

	@Override
	public double getLocalX() {
		return center.getX();
	}

	@Override
	public double getLocalY() {
		return center.getY();
	}

	@Override
	public double getLocalZ() {
		return center.getZ();
	}

	@Override
	public int hashCode() {
		int result = center != null ? center.hashCode() : 0;
		result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
		result = 31 * result + linkedEntity.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Bay)) return false;

		Bay bay = (Bay) o;

		if (!Objects.equals(center, bay.center)) return false;
		if (!Objects.equals(bounds, bay.bounds)) return false;
		return linkedEntity.equals(bay.linkedEntity);
	}
}
