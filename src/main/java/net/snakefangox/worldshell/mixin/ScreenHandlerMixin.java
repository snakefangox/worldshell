package net.snakefangox.worldshell.mixin;

import net.snakefangox.worldshell.mixininterface.ScreenHandlerCheck;
import net.snakefangox.worldshell.storage.ShellStorageWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

	@Inject(method = "canUse(Lnet/minecraft/screen/ScreenHandlerContext;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/Block;)Z", at = @At("HEAD"),
					cancellable = true)
	private static void canUse(ScreenHandlerContext context, PlayerEntity player, Block block, CallbackInfoReturnable<Boolean> cir) {
		context.run((world, blockPos) -> {
			if (world instanceof ShellStorageWorld) {
				boolean value = ScreenHandlerCheck.checkScreenHandler((ShellStorageWorld) world, blockPos, player.currentScreenHandler, player);
				cir.setReturnValue(value);
				cir.cancel();
			}
		});
	}
}
