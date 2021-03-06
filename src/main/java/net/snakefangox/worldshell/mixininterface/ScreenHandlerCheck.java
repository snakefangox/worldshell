package net.snakefangox.worldshell.mixininterface;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.world.ShellStorageWorld;

import java.util.UUID;

public class ScreenHandlerCheck {

	private static final GameProfile fakeGameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
	private static FakePlayerEntity fakePlayer;

	public static boolean checkScreenHandler(ScreenHandler screenHandler, PlayerEntity player) {
		boolean canUse = screenHandler.canUse(player);
		if (!player.world.isClient() && !canUse) {
			MinecraftServer server = player.world.getServer();
			World world = WSUniversal.getStorageDim(server);
			return screenHandler.canUse(getOrCreateFakePlayer(world, player.getPos()));
		}
		return canUse;
	}

	private static PlayerEntity getOrCreateFakePlayer(World storageWorld, Vec3d pos) {
		if (fakePlayer == null) {
			fakePlayer = new FakePlayerEntity(storageWorld, CoordUtil.BP_ZERO, 0, fakeGameProfile);
		}
		fakePlayer.setPosition(pos.x, pos.y, pos.z);
		return fakePlayer;
	}

	private static class FakePlayerEntity extends PlayerEntity {

		public FakePlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
			super(world, pos, yaw, profile);
		}

		@Override
		public double squaredDistanceTo(Vec3d vector) {
			return getTransformedDistance(vector.x, vector.y, vector.z);
		}

		@Override
		public double squaredDistanceTo(double x, double y, double z) {
			return getTransformedDistance(x, y, z);
		}

		public double getTransformedDistance(double x, double y, double z) {
			ShellStorageData data = ((ShellStorageWorld) world).getCachedBayData();
			Bay bay = data.getBay(data.getBayIdFromPos(new BlockPos(x, y, z)));
			if (bay != null && bay.getLinkedEntity().isPresent()) {
				Vec3d temp = CoordUtil.worldToLinkEntity(bay.getLinkedEntity().get(), getPos());
				Vec3d vec = CoordUtil.toGlobal(bay.getCenter(), temp);
				setPosition(vec.x, vec.y, vec.z);
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
