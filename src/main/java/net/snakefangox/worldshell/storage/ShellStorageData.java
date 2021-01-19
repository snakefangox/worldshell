package net.snakefangox.worldshell.storage;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.data.RelativeBlockPos;
import net.snakefangox.worldshell.util.ShellTransferHandler;

import java.util.*;

public class ShellStorageData extends PersistentState {

    private static final String ID = WSUniversal.MODID + ":shell_storage";
    private static final int WORLD_RADIUS = 30000000;

    private int bufferSpace = WSUniversal.CONFIG.bufferSpace;
    private int freeIndex = 1;
    private final Map<Integer, ShellBay> bays = new HashMap<>();
    private final List<Integer> emptyBays = new ArrayList<>();

    public RelativeBlockPos getFreeBay() {
        int id = findEmptyIndex(false);
        int indexFactor = (WORLD_RADIUS * 2) / bufferSpace;
        int x = id % indexFactor;
        int z = (id / indexFactor) + 1;
        return new RelativeBlockPos((x * bufferSpace) - WORLD_RADIUS, 0, (z * bufferSpace) - WORLD_RADIUS);
    }

    public int addBay(ShellBay bay) {
        int id = findEmptyIndex(true);
        bays.put(id, bay);
        markDirty();
        return id;
    }

    public void freeBay(int id, MinecraftServer server) {
        if (!bays.containsKey(id)) return;
        World world = server.getWorld(WSUniversal.STORAGE_DIM);
        ShellBay bay = bays.remove(id);
        emptyBays.add(id);
        ShellTransferHandler.forEachInBox(bay.getBox(), (bp) -> world.setBlockState(bp, Blocks.AIR.getDefaultState()));
        markDirty();
    }

    private int findEmptyIndex(boolean mutate) {
        if (emptyBays.size() > 0) {
            return mutate ? emptyBays.remove(0) : emptyBays.get(0);
        } else {
            return freeIndex++;
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("freeIndex", freeIndex);
        tag.putInt("bufferSpace", bufferSpace);
        CompoundTag bayList = new CompoundTag();
        for (Map.Entry<Integer, ShellBay> entry : bays.entrySet()) {
            bayList.put(String.valueOf(entry.getKey()), entry.getValue().toTag());
        }
        tag.put("bayList", bayList);
        tag.putIntArray("emptyBays", emptyBays);
        return tag;
    }

    public static ShellStorageData fromTag(CompoundTag tag) {
        ShellStorageData storageData = new ShellStorageData();
        storageData.freeIndex = tag.getInt("freeIndex");
        storageData.bufferSpace = tag.getInt("bufferSpace");
        CompoundTag bayList = tag.getCompound("bayList");
        for (String key : bayList.getKeys()) {
            storageData.bays.put(Integer.valueOf(key), new ShellBay(bayList.getCompound(key)));
        }
        int[] eb = tag.getIntArray("emptyBays");
        storageData.emptyBays.clear();
        for (int i : eb) storageData.emptyBays.add(i);
        return storageData;
    }

    public static ShellStorageData getOrCreate(MinecraftServer server) {
        PersistentStateManager stateManager = server.getWorld(WSUniversal.STORAGE_DIM).getPersistentStateManager();
        return stateManager.getOrCreate(ShellStorageData::fromTag, ShellStorageData::new, ID);
    }
}
