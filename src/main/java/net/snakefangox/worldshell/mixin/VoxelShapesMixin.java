package net.snakefangox.worldshell.mixin;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.snakefangox.worldshell.collision.DenseVoxelShape;
import net.snakefangox.worldshell.collision.HullDelegate;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VoxelShapes.class)
public abstract class VoxelShapesMixin {
	@Shadow
	public static VoxelShape cuboidUnchecked(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return null;
	}

	@Inject(method = "cuboid(Lnet/minecraft/util/math/Box;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
	private static void cuboid(Box box, CallbackInfoReturnable<VoxelShape> cir) {
		if (box instanceof ShellCollisionHull || box instanceof HullDelegate)
			cir.setReturnValue(new DenseVoxelShape(cuboidUnchecked(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)));
	}
}
