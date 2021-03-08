package net.snakefangox.worldshell.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.Microcosm;

import java.util.Map;
import java.util.Random;

/**
 * Static helper methods to render a {@link Microcosm} in different contexts.
 */
@Environment(EnvType.CLIENT)
public class WorldShellRender {

    public static void renderMicrocosm(WorldShellEntity wsEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        renderMicrocosm(wsEntity.getMicrocosm(), matrices, wsEntity.getRotation().toFloatQuat(), wsEntity.world.random, vertexConsumers, light);
    }

    public static void renderMicrocosm(Microcosm microcosm, MatrixStack matrices, Quaternion quaternion, Random random, VertexConsumerProvider vertexConsumers, int light) {
        BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockEntityRenderDispatcher beRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
        matrices.push();
        matrices.multiply(quaternion);
        microcosm.tickCache();
        if (!microcosm.isCacheValid()) {
            microcosm.getCache().reset();
            renderToCache(microcosm, renderManager, random);
        }
        microcosm.getCache().draw(matrices);
        for (Map.Entry<BlockPos, BlockEntity> entry : microcosm.getBlockEntities()) {
            matrices.push();
            BlockPos bp = entry.getKey();
            BlockEntity be = entry.getValue();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            beRenderDispatcher.renderEntity(be, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        matrices.pop();
    }

    private static void renderToCache(Microcosm microcosm, BlockRenderManager renderManager, Random random) {
        MatrixStack matrices = new MatrixStack();
        WorldShellRenderCache renderCache = microcosm.getCache();
        for (Map.Entry<BlockPos, BlockState> entry : microcosm.getBlocks()) {
            BlockState bs = entry.getValue();
            FluidState fs = bs.getFluidState();
            BlockPos bp = entry.getKey();
            matrices.push();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            if (!fs.isEmpty()) {
                matrices.push();
                matrices.translate(-(bp.getX() & 15), -(bp.getY() & 15), -(bp.getZ() & 15));
                renderManager.renderFluid(bp, microcosm, renderCache.get(RenderLayers.getFluidLayer(fs)), fs);
                matrices.pop();
            }
            if (bs.getRenderType() != BlockRenderType.INVISIBLE) {
                renderManager.renderBlock(bs, bp, microcosm, matrices, renderCache.get(RenderLayers.getBlockLayer(bs)), true, random);
            }
            matrices.pop();
        }
        renderCache.upload();
        microcosm.markCacheValid();
    }
}
