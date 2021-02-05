package net.snakefangox.worldshell.util;

import net.snakefangox.worldshell.entity.WorldLinkEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastHelper {

	public static RaycastContext raycastFromPlayer(PlayerEntity playerEntity, WorldLinkEntity worldLinkEntity) {
		Vec3d cameraPosVec = worldLinkEntity.toWorldShellCoords(playerEntity.getCameraPosVec(1.0F));
		Vec3d rotationVec = playerEntity.getRotationVec(1.0F);
		Vec3d extendedVec = worldLinkEntity.toWorldShellCoords(cameraPosVec.add(rotationVec.x * 4.5F, rotationVec.y * 4.5F, rotationVec.z * 4.5F));
		return new RaycastContext(cameraPosVec, extendedVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, playerEntity);
	}
}
