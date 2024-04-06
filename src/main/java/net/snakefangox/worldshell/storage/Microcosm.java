package net.snakefangox.worldshell.storage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.light.LightingProvider;
import net.snakefangox.worldshell.client.WorldShellRenderCache;
import net.snakefangox.worldshell.world.DelegateWorld;
import net.snakefangox.worldshell.world.Worldshell;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Stores all of the block information needed to render an accurate world in miniature */
public class Microcosm implements BlockRenderView, CollisionView, Worldshell {

	private static final WorldBorder SHELL_BORDER = new WorldBorder();
	private final World parentWorld;
	private final LocalSpace localSpace;
	private final DelegateWorld delegateWorld;
	private final Map<BlockPos, BlockState> blockStateMap = new LinkedHashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntityMap = new LinkedHashMap<>();
	private final List<ShellTickInvoker> tickInvokers = new ArrayList<>();
	@Environment(EnvType.CLIENT)
	private final WorldShellRenderCache cache;
	private final int cacheValidTime;
	private int cacheResetTimer = 0;

	/** Creates a server sided microcosm, without the render cache */
	public Microcosm(World world, LocalSpace localSpace) {
		this.parentWorld = world;
		this.localSpace = localSpace;
		cache = null;
		cacheValidTime = 0;
		delegateWorld = new DelegateWorld(parentWorld, this, parentWorld.getRegistryManager());
	}

	/** Creates a client sided microcosm, with the render cache */
	public Microcosm(World world, LocalSpace localSpace, int cacheValidTime) {
		this.parentWorld = world;
		this.localSpace = localSpace;
		cache = new WorldShellRenderCache();
		this.cacheValidTime = cacheValidTime;
		delegateWorld = new DelegateWorld(parentWorld, this, parentWorld.getRegistryManager());
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntityMap.get(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blockStateMap.containsKey(pos) ? blockStateMap.get(pos) : Blocks.AIR.getDefaultState();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return blockStateMap.containsKey(pos) ? blockStateMap.get(pos).getFluidState() : Fluids.EMPTY.getDefaultState();
	}

	public void setWorld(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap, List<ShellTickInvoker> tickers) {
		blockStateMap.putAll(stateMap);
		if (entityMap != null) {
			blockEntityMap.putAll(entityMap);
			for (Map.Entry<BlockPos, BlockEntity> entry : blockEntityMap.entrySet()) {
				entry.getValue().setWorld(delegateWorld);
				BlockEntityTicker<?> ticker = blockStateMap.get(entry.getKey()).getBlockEntityTicker(delegateWorld, entry.getValue().getType());
				if (ticker != null) tickers.add(new ShellTickInvoker(entry.getValue(), ticker));
			}
		}
		if (tickers != null)
			tickInvokers.addAll(tickers);
		markCacheInvalid();
	}

	public void markCacheInvalid() {
		cacheResetTimer = 0;
	}

	public Set<Map.Entry<BlockPos, BlockState>> getBlocks() {
		return blockStateMap.entrySet();
	}

	public Set<Map.Entry<BlockPos, BlockEntity>> getBlockEntities() {
		return blockEntityMap.entrySet();
	}

	public boolean hasBlock(BlockPos pos) {
		return blockStateMap.containsKey(pos);
	}

	@SuppressWarnings("deprecation")
	public void setBlock(BlockPos pos, BlockState state, NbtCompound tag) {
		blockStateMap.put(pos, state);
		if (state.hasBlockEntity()) {
			BlockEntity be = ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state);
			if (be != null) {
				BlockEntity oldBe = blockEntityMap.put(pos, be);
				if (oldBe != null) tickInvokers.remove(new ShellTickInvoker(oldBe, null));
				be.setWorld(delegateWorld);
				be.setCachedState(blockStateMap.get(pos));
				if (tag != null) be.readNbt(tag);
				BlockEntityTicker<?> ticker = state.getBlockEntityTicker(delegateWorld, be.getType());
				if (ticker != null) tickInvokers.add(new ShellTickInvoker(be, ticker));
			}
		}
		markCacheInvalid();
	}

	public void addBlockEvent(BlockPos pos, int type, int data) {
		getBlockState(pos).onSyncedBlockEvent(delegateWorld, pos, type, data);
	}

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return parentWorld.getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return parentWorld.getLightingProvider();
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return parentWorld.getColor(toWorldPos(pos), colorResolver);
	}

	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return parentWorld.getLightLevel(type, toWorldPos(pos));
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return parentWorld.getBaseLightLevel(toWorldPos(pos), ambientDarkness);
	}

	@Override
	public boolean isSkyVisible(BlockPos pos) {
		return parentWorld.isSkyVisible(toWorldPos(pos));
	}

	private BlockPos toWorldPos(BlockPos pos) {
		return localSpace.toGlobal(pos);
	}

	public void tick() {
		tickInvokers.forEach(ShellTickInvoker::tick);
	}

	@Override
	public int getHeight() {
		return parentWorld.getHeight();
	}

	@Override
	public int getBottomY() {
		return parentWorld.getBottomY();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return SHELL_BORDER;
	}

	@Nullable
	@Override
	public BlockView getChunkAsView(int chunkX, int chunkZ) {
		return this;
	}

	@Override
	public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
		return Collections.emptyList();
	}

	public void tickCache() {
		--cacheResetTimer;
	}

	public boolean isCacheValid() {
		return cacheResetTimer > 0;
	}

	public void markCacheValid() {
		cacheResetTimer = cacheValidTime;
	}

	@Environment(EnvType.CLIENT)
	public WorldShellRenderCache getCache() {
		return cache;
	}

	public boolean isEmpty() {
		return blockStateMap.isEmpty();
	}

	public DelegateWorld getProxyWorld() {
		return delegateWorld;
	}

	public static class ShellTickInvoker {

		private final BlockEntity be;
		/**
		 * I would love so much to not have to do this but even Mojang's version of this doesn't work
		 * It shouldn't ever crash at least
		 */
		@SuppressWarnings("rawtypes")
		private final BlockEntityTicker ticker;

		@SuppressWarnings("rawtypes")
		public ShellTickInvoker(BlockEntity entity, BlockEntityTicker ticker) {
			this.ticker = ticker;
			this.be = entity;
		}

		@SuppressWarnings("unchecked")
		public void tick() {
			this.ticker.tick(be.getWorld(), be.getPos(), be.getCachedState(), be);
		}

		@Override
		public int hashCode() {
			return be.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ShellTickInvoker)) return false;

			ShellTickInvoker invoker = (ShellTickInvoker) o;

			return Objects.equals(be, invoker.be);
		}
	}
}
