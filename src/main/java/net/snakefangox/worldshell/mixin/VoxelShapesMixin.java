package net.snakefangox.worldshell.mixin;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.snakefangox.worldshell.collision.SpecialBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VoxelShapes.class)
public abstract class VoxelShapesMixin {

	@Inject(method = "cuboid(Lnet/minecraft/util/math/Box;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
	private static void cuboid(Box box, CallbackInfoReturnable<VoxelShape> cir) {
		if (box instanceof SpecialBox)
			cir.setReturnValue(((SpecialBox) box).toVoxelShape());
	}
}
