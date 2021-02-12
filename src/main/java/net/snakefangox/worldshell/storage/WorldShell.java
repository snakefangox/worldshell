package net.snakefangox.worldshell.storage;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
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
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WorldShell implements BlockRenderView {

	private final WorldLinkEntity parent;
	private final Map<BlockPos, BlockState> blockStateMap = new LinkedHashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntityMap = new LinkedHashMap<>();
	private final BlockPos.Mutable reusablePos = new BlockPos.Mutable();
	private final WorldShellRenderCache cache = new WorldShellRenderCache();
	private final int cacheValidTime;
	private int cacheResetTimer = 0;

	public WorldShell(WorldLinkEntity parent, int cacheValidTime) {
		this.parent = parent;
		this.cacheValidTime = cacheValidTime;
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntityMap.get(pos);
	}

	public void setWorld(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap) {
		blockStateMap.putAll(stateMap);
		blockEntityMap.putAll(entityMap);
		markCacheInvalid();
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
				blockEntityMap.put(pos, be);
				be.setWorld(world);
				be.setCachedState(blockStateMap.get(pos));
				if (tag != null) be.fromTag(tag);
			}
		}
		markCacheInvalid();
	}

	private BlockPos toWorldPos(BlockPos pos) {
		Vec3d offset = parent.getBlockOffset();
		return reusablePos.set(pos).add(offset.x, offset.y, offset.z).add(parent.getBlockPos());
	}

	public void addBlockEvent(BlockPos pos, int type, int data) {
		getBlockState(pos).onSyncedBlockEvent(parent.getEntityWorld(), pos, type, data);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blockStateMap.containsKey(pos) ? blockStateMap.get(pos) : Blocks.AIR.getDefaultState();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return blockStateMap.containsKey(pos) ? blockStateMap.get(pos).getFluidState() : Fluids.EMPTY.getDefaultState();
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
	public int getLightLevel(LightType type, BlockPos pos) {
		return getLightingProvider().get(type).getLightLevel(toWorldPos(pos));
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return getLightingProvider().getLight(toWorldPos(pos), ambientDarkness);
	}

	@Override
	public boolean isSkyVisible(BlockPos pos) {
		return getLightLevel(LightType.SKY, toWorldPos(pos)) >= this.getMaxLightLevel();
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return parent.getEntityWorld().getColor(toWorldPos(pos), colorResolver);
	}

	@Override
	public int getBottomSectionLimit() {
		return parent.getEntityWorld().getBottomSectionLimit();
	}

	@Override
	public int getSectionCount() {
		return parent.getEntityWorld().getBottomSectionLimit();
	}

	public void tickCache() {
		--cacheResetTimer;
	}

	public boolean isCacheValid() {
		return cacheResetTimer > 0;
	}

	public void markCacheInvalid() {
		cacheResetTimer = 0;
	}

	public void markCacheValid() {
		cacheResetTimer = cacheValidTime;
	}

	public WorldShellRenderCache getCache() {
		return cache;
	}
}
