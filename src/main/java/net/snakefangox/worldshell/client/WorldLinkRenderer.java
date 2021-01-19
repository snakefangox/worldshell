package net.snakefangox.worldshell.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.snakefangox.worldshell.entity.WorldLinkEntity;

public class WorldLinkRenderer extends EntityRenderer<WorldLinkEntity> {

    protected WorldLinkRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WorldLinkEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(WorldLinkEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}
