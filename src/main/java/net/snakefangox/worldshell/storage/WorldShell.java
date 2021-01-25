package net.snakefangox.worldshell.storage;

import java.util.HashMap;
import java.util.Map;

import net.snakefangox.worldshell.entity.WorldLinkEntity;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

public class WorldShell implements BlockRenderView {

	private final WorldLinkEntity parent;
	private final Map<BlockPos, BlockState> blockStateMap = new HashMap<>();
	private final Map<BlockPos, BlockEntity> blockEntityMap = new HashMap<>();

	public WorldShell(WorldLinkEntity parent) {
		this.parent = parent;
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntityMap.get(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blockStateMap.get(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return blockStateMap.get(pos).getFluidState();
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
		return parent.getEntityWorld().getColor(pos, colorResolver);
	}

	@Override
	public int getBottomSectionLimit() {
		return parent.getEntityWorld().getBottomSectionLimit();
	}

	@Override
	public int getSectionCount() {
		return parent.getEntityWorld().getBottomSectionLimit();
	}
}
