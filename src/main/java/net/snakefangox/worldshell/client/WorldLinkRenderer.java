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
import net.snakefangox.worldshell.entity.WorldShellEntity;

@Environment(EnvType.CLIENT)
public class WorldLinkRenderer extends EntityRenderer<WorldShellEntity> {

	protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public Vec3d getPositionOffset(WorldShellEntity entity, float tickDelta) {
		return entity.getBlockOffset();
	}

	@Override
	public void render(WorldShellEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShellRender.renderMicrocosm(entity, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(WorldShellEntity entity) {
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
}
