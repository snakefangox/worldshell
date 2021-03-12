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
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.util.ShellTransferHandler;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a Worldshell in the storage dimension
 */
public class Bay implements LocalSpace {

	/**The center of the shell*/
	private BlockPos center;

	/**Defines the box the shell fits within*/
	private BlockBox bounds;

	/**The entity changes to this shell should propagate to*/
	private WorldShellEntity linkedEntity = null;

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
		ShellTransferHandler.forEachInBox(bounds, (bp) -> {
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

	public void markDirty(ServerWorld world) {
		ShellStorageData.getOrCreate(world).markDirty();
	}

	public BlockBox getBounds() {
		return bounds;
	}

	public void linkEntity(@NotNull WorldShellEntity worldShellEntity) {
		linkedEntity = worldShellEntity;
		if (worldShellEntity.getMicrocosm().isEmpty()) {
			fillServerWorldShell(worldShellEntity);
		}
	}

	private void fillServerWorldShell(WorldShellEntity entity) {
		World world = WorldShell.getStorageDim(entity.world.getServer());
		Map<BlockPos, BlockState> stateMap = new HashMap<>();
		ShellTransferHandler.forEachInBox(bounds, (bp) -> {
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
}