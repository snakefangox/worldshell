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
public class WorldShellEntityRenderer<T extends WorldShellEntity> extends EntityRenderer<T> {

	protected WorldShellEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public Vec3d getPositionOffset(T entity, float tickDelta) {
		return entity.getBlockOffset();
	}

	@Override
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		WorldShellRender.renderMicrocosm(entity, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(T entity) {
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
}
