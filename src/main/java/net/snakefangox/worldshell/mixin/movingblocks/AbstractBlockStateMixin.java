package net.snakefangox.worldshell.mixin.movingblocks;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.mixinextras.NoOpPosWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin extends State<Block, BlockState> {

	protected AbstractBlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<BlockState> codec) {
		super(owner, entries, codec);
	}

	@Inject(method = "onStateReplaced", at = @At("HEAD"), cancellable = true)
	private void onStateReplaced(World world, BlockPos pos, BlockState state, boolean moved, CallbackInfo ci){
		if (pos instanceof NoOpPosWrapper) ci.cancel();
	}

	@Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
	private void onBlockAdded(World world, BlockPos pos, BlockState state, boolean moved, CallbackInfo ci){
		if (pos instanceof NoOpPosWrapper) ci.cancel();
	}
}
