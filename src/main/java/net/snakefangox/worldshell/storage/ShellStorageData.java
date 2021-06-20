package net.snakefangox.worldshell.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.snakefangox.worldshell.WorldShellConfig;
import net.snakefangox.worldshell.WorldShellMain;
import net.snakefangox.worldshell.transfer.WorldShellDeconstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellStorageData extends PersistentState {

	private static final String ID = WorldShellMain.MODID + ":shell_storage";
	private static final int WORLD_RADIUS = 30000000;
	private final Map<Integer, Bay> bays = new HashMap<>();
	private final List<Integer> emptyBays = new ArrayList<>();
	private int bufferSpace = WorldShellConfig.getBufferSpace();
	private int freeIndex = 1;

	public static ShellStorageData getOrCreate(MinecraftServer server) {
		PersistentStateManager stateManager = WorldShellMain.getStorageDim(server).getPersistentStateManager();
		return stateManager.getOrCreate(ShellStorageData::fromNbt, ShellStorageData::new, ID);
	}

	public static ShellStorageData fromNbt(NbtCompound tag) {
		ShellStorageData storageData = new ShellStorageData();
		storageData.freeIndex = tag.getInt("freeIndex");
		storageData.bufferSpace = tag.getInt("bufferSpace");
		NbtCompound bayList = tag.getCompound("bayList");
		for (String key : bayList.getKeys()) {
			storageData.bays.put(Integer.valueOf(key), new Bay(bayList.getCompound(key)));
		}
		int[] eb = tag.getIntArray("emptyBays");
		storageData.emptyBays.clear();
		for (int i : eb) storageData.emptyBays.add(i);
		return storageData;
	}

	public BlockPos getFreeBay() {
		int id = findEmptyIndex(false);
		int maxBays = (WORLD_RADIUS * 2) / bufferSpace;
		int x = id % maxBays;
		int z = (id / maxBays) + 1;
		return new BlockPos((x * bufferSpace) - WORLD_RADIUS, 0, (z * bufferSpace) - WORLD_RADIUS);
	}

	private int findEmptyIndex(boolean mutate) {
		if (emptyBays.size() > 0) {
			return mutate ? emptyBays.remove(0) : emptyBays.get(0);
		} else {
			return mutate ? freeIndex++ : freeIndex;
		}
	}

	public int getBayIdFromPos(BlockPos pos) {
		int x = Math.round(((float) pos.getX() + WORLD_RADIUS) / (float) bufferSpace);
		int z = Math.round(((float) pos.getZ() + WORLD_RADIUS) / (float) bufferSpace);
		int maxBays = (WORLD_RADIUS * 2) / bufferSpace;
		return x + ((z - 1) * maxBays);
	}

	public Bay getBay(int shellId) {
		return bays.get(shellId);
	}

	public int addBay(Bay bay) {
		int id = findEmptyIndex(true);
		bays.put(id, bay);
		bay.markDirtyFunc = this::markDirty;
		markDirty();
		return id;
	}

	/** You should not be calling this directly, WorldShellDeconstructor will handle it for you */
	public void freeBay(int id, WorldShellDeconstructor deconstructor) {
		if (!bays.containsKey(id) || !deconstructor.isRemoving()) return;
		Bay bay = bays.remove(id);
		bay.markDirtyFunc = () -> {
		};
		emptyBays.add(id);
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.putInt("freeIndex", freeIndex);
		tag.putInt("bufferSpace", bufferSpace);
		NbtCompound bayList = new NbtCompound();
		for (Map.Entry<Integer, Bay> entry : bays.entrySet()) {
			bayList.put(String.valueOf(entry.getKey()), entry.getValue().toNbt());
		}
		tag.put("bayList", bayList);
		tag.putIntArray("emptyBays", emptyBays);
		return tag;
	}
}
