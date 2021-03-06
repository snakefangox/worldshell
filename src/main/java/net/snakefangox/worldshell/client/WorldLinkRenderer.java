package net.snakefangox.worldshell.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.entity.WorldLinkEntity;

@Environment(EnvType.CLIENT)
public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

	protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public Vec3d getPositionOffset(WorldLinkEntity entity, float tickDelta) {
		return entity.getBlockOffset();
	}

	@Override
	public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShellRender.renderWorldShell(entity.getWorldShell(), matrices, entity.getRotation().toFloatQuat(), entity.world.random, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(WorldLinkEntity entity) {
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
}
