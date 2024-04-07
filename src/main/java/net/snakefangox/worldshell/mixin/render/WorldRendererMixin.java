package net.snakefangox.worldshell.mixin.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements SynchronousResourceReloader, AutoCloseable {

    @Final
    @Shadow
    private MinecraftClient client;

    @Final
    @Shadow
    private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
                        GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        HitResult target = client.crosshairTarget;

        if (renderBlockOutline && target instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof WorldShellEntity shellEntity) {
            BlockHitResult hitResult = shellEntity.raycastToWorldShell(client.player);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockState blockState = shellEntity.getMicrocosm().getBlockState(hitResult.getBlockPos());

                if (!blockState.isAir()) {
                    Vec3d cam = camera.getPos();
                    VertexConsumer vertexConsumer = bufferBuilders.getEntityVertexConsumers().getBuffer(RenderLayer.getLines());

                    matrices.push();
                    Quaternion rot = shellEntity.getRotation();
                    Quaternionf quat = new Quaternionf((float) rot.getX(), (float) rot.getY(), (float) rot.getZ(), (float) rot.getW());

                    BlockPos lPos = hitResult.getBlockPos();
                    Vec3d pos = shellEntity.toGlobal((double) lPos.getX(), lPos.getY(), lPos.getZ());
                    matrices.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
                    matrices.multiply(quat);
                    drawBlockOutline(matrices, vertexConsumer, camera.getFocusedEntity(), 0, 0, 0, BlockPos.ORIGIN, blockState);
                    matrices.pop();
                }
            }
        }
    }

    @Shadow
    protected abstract void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state);
}
