package net.snakefangox.worldshell.client;

import java.util.Map;

import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

	protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShell worldShell = entity.getWorldShell().get();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		matrices.push();
		for (Map.Entry<BlockPos, BlockState> entry : worldShell.getBlocks()) {
			BlockState bs = entry.getValue();
			FluidState fs = bs.getFluidState();
			BlockPos bp = entry.getKey();
			matrices.push();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            if (!fs.isEmpty()) {
                matrices.push();
                matrices.translate(-(bp.getX() & 15), -(bp.getY() & 15), -(bp.getZ() & 15));
                renderManager.renderFluid(bp, worldShell, vertexConsumers.getBuffer(RenderLayers.getFluidLayer(fs)), fs);
                matrices.pop();
            }
            if (bs.getRenderType() != BlockRenderType.INVISIBLE) {
                renderManager.renderBlock(bs, bp, worldShell, matrices, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(bs)), true, entity.world.random);
            }
			matrices.pop();
		}
		matrices.pop();
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
