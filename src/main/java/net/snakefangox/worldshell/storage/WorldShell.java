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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;
import net.snakefangox.worldshell.client.WorldShellRenderCache;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.world.ProxyWorld;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WorldShell implements BlockRenderView {

	private final WorldLinkEntity parent;
	private final ProxyWorld proxyWorld;
	private final Map<BlockPos, BlockState> blockStateMap = new LinkedHashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntityMap = new LinkedHashMap<>();
	private final List<ShellTickInvoker> tickInvokers = new ArrayList<>();
	private final BlockPos.Mutable reusablePos = new BlockPos.Mutable();
	@Environment(EnvType.CLIENT)
	private final WorldShellRenderCache cache = new WorldShellRenderCache();
	private final int cacheValidTime;
	private int cacheResetTimer = 0;

	public WorldShell(WorldLinkEntity parent, int cacheValidTime) {
		this.parent = parent;
		this.cacheValidTime = cacheValidTime;
		proxyWorld = new ProxyWorld(parent.getEntityWorld(), this);
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
				entry.getValue().setWorld(proxyWorld);
				BlockEntityTicker<?> ticker = blockStateMap.get(entry.getKey()).getBlockEntityTicker(proxyWorld, entry.getValue().getType());
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

	public void setBlock(BlockPos pos, BlockState state, CompoundTag tag, World world) {
		blockStateMap.put(pos, state);
		if (state.hasBlockEntity()) {
			BlockEntity be = ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state);
			if (be != null) {
				BlockEntity oldBe = blockEntityMap.put(pos, be);
				if (oldBe != null) tickInvokers.remove(new ShellTickInvoker(oldBe, null));
				be.setWorld(proxyWorld);
				be.setCachedState(blockStateMap.get(pos));
				if (tag != null) be.fromTag(tag);
				BlockEntityTicker<?> ticker = state.getBlockEntityTicker(proxyWorld, be.getType());
				if (ticker != null) tickInvokers.add(new ShellTickInvoker(be, ticker));
			}
		}
		markCacheInvalid();
	}

	public void addBlockEvent(BlockPos pos, int type, int data) {
		getBlockState(pos).onSyncedBlockEvent(proxyWorld, pos, type, data);
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
	public int getSectionCount() {
		return parent.getEntityWorld().getBottomSectionLimit();
	}

	@Override
	public int getBottomSectionLimit() {
		return parent.getEntityWorld().getBottomSectionLimit();
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

	public ProxyWorld getProxyWorld() {
		return proxyWorld;
	}

	public static class ShellTickInvoker {
		private final BlockEntity be;
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

			return be.equals(invoker.be);
		}
	}
}
