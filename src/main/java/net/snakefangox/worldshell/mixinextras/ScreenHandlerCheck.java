package net.snakefangox.worldshell.mixinextras;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShell;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.world.ShellStorageWorld;

import java.util.UUID;

public class ScreenHandlerCheck {

	private static final GameProfile fakeGameProfile = new GameProfile(UUID.fromString("9ab93073-2db5-4511-9d3c-af9fe6ec37af"), "FakePlayer");
	private static FakePlayerEntity fakePlayer;

	public static boolean checkScreenHandler(ScreenHandler screenHandler, PlayerEntity player) {
		boolean canUse = screenHandler.canUse(player);
		if (!player.world.isClient() && !canUse) {
			MinecraftServer server = player.world.getServer();
			World world = WorldShell.getStorageDim(server);
			return screenHandler.canUse(getOrCreateFakePlayer(world, player.getPos()));
		}
		return canUse;
	}

	private static PlayerEntity getOrCreateFakePlayer(World storageWorld, Vec3d pos) {
		if (fakePlayer == null) {
			fakePlayer = new FakePlayerEntity(storageWorld, BlockPos.ORIGIN, 0, fakeGameProfile);
		}
		fakePlayer.setPosition(pos.x, pos.y, pos.z);
		return fakePlayer;
	}

	private static class FakePlayerEntity extends PlayerEntity {

		public FakePlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
			super(world, pos, yaw, profile);
		}

		@Override
		public double squaredDistanceTo(double x, double y, double z) {
			return getTransformedDistance(x, y, z);
		}

		@Override
		public double squaredDistanceTo(Vec3d vector) {
			return getTransformedDistance(vector.x, vector.y, vector.z);
		}

		public double getTransformedDistance(double x, double y, double z) {
			ShellStorageData data = ((ShellStorageWorld) world).getCachedBayData();
			Bay bay = data.getBay(data.getBayIdFromPos(new BlockPos(x, y, z)));
			if (bay != null && bay.getLinkedEntity().isPresent()) {
				WorldShellEntity entity = bay.getLinkedEntity().get();
				Vec3d pos = getPos();
				double newX = entity.globalToGlobalX(bay, pos.x, pos.y, pos.z);
				double newY = entity.globalToGlobalY(bay, pos.x, pos.y, pos.z);
				double newZ = entity.globalToGlobalZ(bay, pos.x, pos.y, pos.z);
				setPosition(newX, newY, newZ);
			}
			return super.squaredDistanceTo(x, y, z);
		}

		@Override
		public boolean isSpectator() {
			return false;
		}

		@Override
		public boolean isCreative() {
			return false;
		}
	}
}
