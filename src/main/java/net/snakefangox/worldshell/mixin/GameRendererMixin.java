package net.snakefangox.worldshell.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements SynchronousResourceReloadListener, AutoCloseable {

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "updateTargetedEntity", at = @At(value = "JUMP", ordinal = 9, shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void updateTargetedEntity(float tickDelta, CallbackInfo ci, Entity entity, double d, Vec3d vec3d, boolean bl, int i, double e, Vec3d vec3d2, Vec3d vec3d3, float f, Box box, EntityHitResult entityHitResult, Entity entity2, Vec3d vec3d4, double g) {
		if (entity2 instanceof WorldLinkEntity) {
			client.targetedEntity = entity2;
		}
	}

}
