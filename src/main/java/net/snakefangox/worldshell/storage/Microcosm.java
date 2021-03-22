package net.snakefangox.worldshell.storage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;
import net.snakefangox.worldshell.client.WorldShellRenderCache;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.world.DelegateWorld;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Stores all of the block information needed to render an accurate world in miniature */
public class Microcosm implements BlockRenderView {

	private final WorldShellEntity parent;
	private final DelegateWorld delegateWorld;
	private final Map<BlockPos, BlockState> blockStateMap = new LinkedHashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntityMap = new LinkedHashMap<>();
	private final List<ShellTickInvoker> tickInvokers = new ArrayList<>();
	private final BlockPos.Mutable reusablePos = new BlockPos.Mutable();
	@Environment(EnvType.CLIENT)
	private final WorldShellRenderCache cache;
	private final int cacheValidTime;
	private int cacheResetTimer = 0;

	/** Creates a server sided microcosm, without the render cache */
	public Microcosm(WorldShellEntity parent /*TODO Replace with world and LocalSpace*/) {
		this.parent = parent;
		cache = null;
		cacheValidTime = 0;
		delegateWorld = new DelegateWorld(parent.getEntityWorld(), this);
	}

	/** Creates a client sided microcosm, with the render cache */
	public Microcosm(WorldShellEntity parent, int cacheValidTime) {
		this.parent = parent;
		cache = new WorldShellRenderCache();
		this.cacheValidTime = cacheValidTime;
		delegateWorld = new DelegateWorld(parent.getEntityWorld(), this);
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
		return parent.getEntityWorld().getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return parent.getEntityWorld().getLightingProvider();
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return parent.getEntityWorld().getColor(toWorldPos(pos), colorResolver);
	}

	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return getLightingProvider().get(type).getLightLevel(toWorldPos(pos));
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return getLightingProvider().getLight(toWorldPos(pos), ambientDarkness);
	}

	private BlockPos toWorldPos(BlockPos pos) {
		Vec3d offset = parent.getBlockOffset();
		return reusablePos.set(pos).add(offset.x, offset.y, offset.z).add(parent.getBlockPos());
	}

	@Override
	public boolean isSkyVisible(BlockPos pos) {
		return getLightLevel(LightType.SKY, toWorldPos(pos)) >= this.getMaxLightLevel();
	}

	public void tick() {
		tickInvokers.forEach(ShellTickInvoker::tick);
	}

	@Override
	public int getHeight() {
		return parent.getEntityWorld().getHeight();
	}

	@Override
	public int getBottomY() {
		return parent.getEntityWorld().getBottomY();
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
