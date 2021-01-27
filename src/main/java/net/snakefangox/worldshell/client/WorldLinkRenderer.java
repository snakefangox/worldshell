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
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

    protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        WorldShell worldShell = entity.getWorldShell().get();
        BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
        for (Map.Entry<BlockPos, BlockState> entry : worldShell.getBlocks()){
            BlockState bs = entry.getValue();
            if (bs.getRenderType() == BlockRenderType.INVISIBLE) continue;
            BlockPos bp = entry.getKey();
            matrices.push();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            renderManager.renderBlock(bs, bp, worldShell, matrices, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(bs)), true, entity.world.random);
            matrices.pop();
        }
    }

    @Override
    public Identifier getTexture(WorldLinkEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
