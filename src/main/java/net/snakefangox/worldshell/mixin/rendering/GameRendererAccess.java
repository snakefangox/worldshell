package net.snakefangox.worldshell.mixin.rendering;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccess {
	@Invoker
	double invokeGetFov(Camera camera, float tickDelta, boolean changingFov);
}
