package net.snakefangox.worldshell.mixin;

import net.minecraft.util.math.Box;
import net.snakefangox.worldshell.collision.SpecialBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Box.class)
public abstract class BoxMixin {

	// Vanilla assumes boxes can collide either way. In this case they're wrong
	// Thanks Stuff-Stuffs
	@Inject(method = "intersects(Lnet/minecraft/util/math/Box;)Z", at = @At("HEAD"), cancellable = true)
	private void intersects(Box box, CallbackInfoReturnable<Boolean> cir) {
		if (box instanceof SpecialBox) {
			cir.setReturnValue(box.intersects((Box) (Object) this));
		}
	}
}
