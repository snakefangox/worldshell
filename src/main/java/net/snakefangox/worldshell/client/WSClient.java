package net.snakefangox.worldshell.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.mixin.WorldRendererMixin;
import net.snakefangox.worldshell.util.CoordUtil;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WSClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WSUniversal.WORLD_LINK_ENTITY_TYPE, WorldLinkRenderer::new);
		WSNetworking.registerClientPackets();
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(WSClient::renderBlockOutline);
	}

	public static boolean renderBlockOutline(WorldRenderContext renderCtx, @Nullable HitResult hitResult) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && client.targetedEntity instanceof WorldLinkEntity) {
			WorldLinkEntity entity = (WorldLinkEntity) client.targetedEntity;
			BlockHitResult wsResult = entity.raycastToWorldShell(client.player);
			if (wsResult.getType() != HitResult.Type.MISS) {
				Vec3d cam = renderCtx.camera().getPos();
				BlockPos pos = CoordUtil.linkEntityToWorld(CoordUtil.BP_ZERO, entity, wsResult.getBlockPos());
				VertexConsumer vertexConsumer = renderCtx.consumers().getBuffer(RenderLayer.getLines());
				((WorldRendererMixin) renderCtx.worldRenderer()).invokeDrawBlockOutline(renderCtx.matrixStack(), vertexConsumer,
						renderCtx.camera().getFocusedEntity(), cam.x, cam.y, cam.z, pos,
						entity.getWorldShell().getBlockState(wsResult.getBlockPos()));
			}
		}
		return true;
	}
}
