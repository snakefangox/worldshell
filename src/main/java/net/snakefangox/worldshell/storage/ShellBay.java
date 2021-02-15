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
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.util.ShellTransferHandler;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a shell in the storage dimension
 */
public class ShellBay {

	//The center of the shell
	private BlockPos center;

	//Defines the box the shell fits within
	private BlockBox bounds;

	//The entity changes to this shell should propagate to
	private Optional<WorldLinkEntity> linkedEntity = Optional.empty();

	public ShellBay(BlockPos center, BlockBox bounds) {
		this.center = center;
		this.bounds = bounds;
	}

	public ShellBay(CompoundTag tag) {
		fromTag(tag);
	}

	public PacketByteBuf createClientPacket(MinecraftServer server, PacketByteBuf buf) {
		World world = WSUniversal.getStorageDim(server);
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
		ServerWorld world = WSUniversal.getStorageDim(server);
		ChunkPos.stream(new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.minX), ChunkSectionPos.getSectionCoord(bounds.minZ)),
				new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.maxX), ChunkSectionPos.getSectionCoord(bounds.maxZ)))
				.forEach(chunkPos -> world.setChunkForced(chunkPos.x, chunkPos.z, true));
	}

	public Vec3d toEntityCoordSpace(Vec3d vec) {
		return CoordUtil.linkEntityToWorld(center, linkedEntity.get(), vec);
	}

	public Vec3d toEntityCoordSpace(double x, double y, double z) {
		return CoordUtil.linkEntityToWorld(center, linkedEntity.get(), x, y, z);
	}

	public BlockPos toEntityCoordSpace(BlockPos pos) {
		return CoordUtil.linkEntityToWorld(center, linkedEntity.get(), pos);
	}

	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putLong("center", center.asLong());
		tag.put("bounds", bounds.toNbt());
		return tag;
	}

	public void fromTag(CompoundTag tag) {
		center = BlockPos.fromLong(tag.getLong("center"));
		bounds = new BlockBox(((IntArrayTag) tag.get("bounds")).getIntArray());
	}

	public void markDirty(ServerWorld world) {
		ShellStorageData.getOrCreate(world).markDirty();
	}

	public BlockBox getBounds() {
		return bounds;
	}

	public void linkEntity(@NotNull WorldLinkEntity worldLinkEntity) {
		linkedEntity = Optional.of(worldLinkEntity);
	}

	public Optional<WorldLinkEntity> getLinkedEntity() {
		return linkedEntity;
	}

	public BlockPos getCenter() {
		return center;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ShellBay)) return false;

		ShellBay shellBay = (ShellBay) o;

		if (center != null ? !center.equals(shellBay.center) : shellBay.center != null) return false;
		if (bounds != null ? !bounds.equals(shellBay.bounds) : shellBay.bounds != null) return false;
		return linkedEntity.equals(shellBay.linkedEntity);
	}

	@Override
	public int hashCode() {
		int result = center != null ? center.hashCode() : 0;
		result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
		result = 31 * result + linkedEntity.hashCode();
		return result;
	}
}
