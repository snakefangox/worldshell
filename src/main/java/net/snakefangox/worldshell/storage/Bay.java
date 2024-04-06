package net.snakefangox.worldshell.storage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShellMain;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.transfer.BlockBoxIterator;
import net.snakefangox.worldshell.util.WSNbtHelper;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a Worldshell in the storage dimension
 */
public class Bay implements LocalSpace {

	/** Calls on the {@link ShellStorageData} holding this */
	Runnable markDirtyFunc = () -> {
	};
	/** The center of the shell */
	private BlockPos center;
	/** Defines the box the shell fits within */
	private BlockBox bounds;
	/** The entity changes to this shell should propagate to */
	private WorldShellEntity linkedEntity = null;

	public Bay(BlockPos center) {
		this.center = center;
		this.bounds = new BlockBox(center);
	}

	public Bay(NbtCompound tag) {
		fromNbt(tag);
	}

	public void fromNbt(NbtCompound tag) {
		center = BlockPos.fromLong(tag.getLong("center"));
		int[] dims = ((NbtIntArray) tag.get("bounds")).getIntArray();
		bounds = WSNbtHelper.blockBoxFromNbt(dims);
	}

	public void createClientPacket(MinecraftServer server, PacketByteBuf buf) {
		World world = WorldShellMain.getStorageDim(server);
		Map<BlockState, List<BlockPos>> stateListMap = new HashMap<>();
		List<BlockEntity> blockEntities = new ArrayList<>();
		BlockBoxIterator.of(bounds).forEachRemaining(bp -> {
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
		WorldShellPacketHelper.writeBlocks(buf, stateListMap, blockEntities, this);
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

	public NbtCompound toNbt() {
		NbtCompound tag = new NbtCompound();
		tag.putLong("center", center.asLong());
		tag.put("bounds", WSNbtHelper.blockBoxToNbt(bounds));
		return tag;
	}

	public BlockBox getBounds() {
		return bounds;
	}

	public void linkEntity(@NotNull WorldShellEntity worldShellEntity) {
		linkedEntity = worldShellEntity;
		setLoadForChunks(worldShellEntity.world.getServer(), true);
		if (worldShellEntity.getMicrocosm().isEmpty()) {
			fillServerWorldShell(worldShellEntity);
		}
	}

	public void setLoadForChunks(MinecraftServer server, boolean load) {
		ServerWorld world = WorldShellMain.getStorageDim(server);
		ChunkPos.stream(new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.getMinX()), ChunkSectionPos.getSectionCoord(bounds.getMinZ())),
				new ChunkPos(ChunkSectionPos.getSectionCoord(bounds.getMaxX()), ChunkSectionPos.getSectionCoord(bounds.getMaxZ())))
				.forEach(chunkPos -> world.setChunkForced(chunkPos.x, chunkPos.z, load));
	}

	private void fillServerWorldShell(WorldShellEntity entity) {
		World world = WorldShellMain.getStorageDim(entity.world.getServer());
		Map<BlockPos, BlockState> stateMap = new HashMap<>();
		BlockBoxIterator.of(bounds).forEachRemaining(bp -> {
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
		double x = bounds.getMinX() + (((double) (bounds.getMaxX() - bounds.getMinX())) / 2.0);
		double y = bounds.getMinY() + (((double) (bounds.getMaxY() - bounds.getMinY())) / 2.0);
		double z = bounds.getMinZ() + (((double) (bounds.getMaxZ() - bounds.getMinZ())) / 2.0);
		return new Vec3d(x, y, z);
	}

	// They don't want you to know this, but `encompass` isn't deprecated
	@SuppressWarnings("deprecation")
	public boolean updateBoxBounds(BlockPos pos) {
		if (bounds.contains(pos)) return false;
		bounds.encompass(pos);
		markDirty();
		return true;
	}

	public void markDirty() {
		markDirtyFunc.run();
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
