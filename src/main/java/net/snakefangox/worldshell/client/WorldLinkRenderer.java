package net.snakefangox.worldshell.client;

import java.util.Map;

import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

	protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShell worldShell = entity.getWorldShell();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		BlockEntityRenderDispatcher beRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
		matrices.push();
		worldShell.tickCache();
		if (!worldShell.isCacheValid()) {
			worldShell.getCache().reset();
			renderToCache(worldShell, renderManager, entity, yaw, tickDelta);
		}
		worldShell.getCache().draw(matrices);
		for (Map.Entry<BlockPos, BlockEntity> entry : worldShell.getBlockEntities()) {
			beRenderDispatcher.render(entry.getValue(), tickDelta, matrices, vertexConsumers);
		}
		matrices.pop();
	}

	private void renderToCache(WorldShell worldShell, BlockRenderManager renderManager, WorldLinkEntity entity, float yaw, float tickDelta) {
		MatrixStack matrices = new MatrixStack();
		WorldShellRenderCache renderCache = worldShell.getCache();
		for (Map.Entry<BlockPos, BlockState> entry : worldShell.getBlocks()) {
			BlockState bs = entry.getValue();
			FluidState fs = bs.getFluidState();
			BlockPos bp = entry.getKey();
			matrices.push();
			matrices.translate(bp.getX(), bp.getY(), bp.getZ());
			if (!fs.isEmpty()) {
				matrices.push();
				matrices.translate(-(bp.getX() & 15), -(bp.getY() & 15), -(bp.getZ() & 15));
				renderManager.renderFluid(bp, worldShell, renderCache.get(RenderLayers.getFluidLayer(fs)), fs);
				matrices.pop();
			}
			if (bs.getRenderType() != BlockRenderType.INVISIBLE) {
				renderManager.renderBlock(bs, bp, worldShell, matrices, renderCache.get(RenderLayers.getBlockLayer(bs)), true, entity.world.random);
			}
			matrices.pop();
		}
		renderCache.upload();
		worldShell.markCacheValid();
	}

	@Override
	public Vec3d getPositionOffset(WorldLinkEntity entity, float tickDelta) {
		return entity.getBlockOffset();
	}

	@Override
	public Identifier getTexture(WorldLinkEntity entity) {
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
}
