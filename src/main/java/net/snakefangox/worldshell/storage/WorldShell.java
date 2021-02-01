package net.snakefangox.worldshell.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.snakefangox.worldshell.client.WorldShellRenderCache;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import org.jetbrains.annotations.Nullable;

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
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

public class WorldShell implements BlockRenderView {

	private final WorldLinkEntity parent;
	private Map<BlockPos, BlockState> blockStateMap = new HashMap<>();
	private Map<BlockPos, BlockEntity> blockEntityMap = new HashMap<>();
	private final BlockPos.Mutable reusablePos = new BlockPos.Mutable();
	private final WorldShellRenderCache cache = new WorldShellRenderCache();
	private boolean isCacheValid = false;

	public WorldShell(WorldLinkEntity parent) {
		this.parent = parent;
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntityMap.get(pos);
	}

	public void setWorld(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap) {
		blockStateMap = stateMap;
		blockEntityMap = entityMap;
	}

	public Set<Map.Entry<BlockPos, BlockState>> getBlocks() {
		return blockStateMap.entrySet();
	}

	public void setBlock(BlockPos pos, BlockState state, CompoundTag tag) {
		blockStateMap.put(pos, state);
		if (state.hasBlockEntity()) {
			blockEntityMap.put(pos, ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state));
			if (tag != null) blockEntityMap.get(pos).fromTag(tag);
		}
	}

	private BlockPos toWorldPos(BlockPos pos) {
		Vec3d offset = parent.getBlockOffset();
		return reusablePos.set(pos).add(offset.x, offset.y, offset.z).add(parent.getBlockPos());
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

	public boolean isCacheValid() {
		return isCacheValid;
	}

	public void markCacheInvalid() {
		isCacheValid = false;
	}

	public void markCacheValid() {
		isCacheValid = true;
	}

	public WorldShellRenderCache getCache() {
		return cache;
	}
}
