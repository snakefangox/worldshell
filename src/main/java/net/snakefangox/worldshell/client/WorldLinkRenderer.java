package net.snakefangox.worldshell.client;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
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
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.WorldShell;

import java.util.Map;

public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

	protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShellRender.renderWorldShell(entity.getWorldShell(), matrices, entity.world.random, vertexConsumers, light);
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
