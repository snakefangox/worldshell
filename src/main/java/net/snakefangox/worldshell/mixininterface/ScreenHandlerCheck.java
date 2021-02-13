package net.snakefangox.worldshell.mixininterface;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.world.ShellStorageWorld;

import java.util.Optional;
import java.util.UUID;

public class ScreenHandlerCheck {

	private static GameProfile fakeGameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
	private static FakePlayerEntity fakePlayer;

	public static boolean checkScreenHandler(ScreenHandler screenHandler, PlayerEntity player) {
		if (!player.world.isClient() && screenHandler.slots.size() > 0) {
			Inventory inventory = screenHandler.slots.get(0).inventory;
			if (inventory instanceof BlockEntity && ((BlockEntity) inventory).getWorld() instanceof ShellStorageWorld) {
				BlockEntity be = (BlockEntity) inventory;
				BlockPos pos = be.getPos();
				ShellStorageWorld storageWorld = (ShellStorageWorld) be.getWorld();
				return checkScreenHandler(storageWorld, pos, screenHandler, player);
			}
		}
		return screenHandler.canUse(player);
	}

	public static boolean checkScreenHandler(ShellStorageWorld storageWorld, BlockPos pos, ScreenHandler screenHandler, PlayerEntity player) {
		ShellStorageData data = storageWorld.getCachedBayData();
		ShellBay bay = data.getBay(data.getBayIdFromPos(pos));
		Optional<WorldLinkEntity> entity = bay.getLinkedEntity();
		if (entity.isPresent()) {
			Vec3d vec = CoordUtil.worldToLinkEntity(entity.get(), player.getPos());
			BlockPos center = bay.getCenter();
			return screenHandler.canUse(getOrCreateFakePlayer(storageWorld,
					new BlockPos(vec.x + center.getX(), vec.y + center.getY(), vec.z + center.getZ())));
		}
		return screenHandler.canUse(player);
	}

	private static PlayerEntity getOrCreateFakePlayer(World storageWorld, BlockPos pos) {
		if (fakePlayer == null) {
			fakePlayer = new FakePlayerEntity(storageWorld, pos, 0, fakeGameProfile);
		}
		fakePlayer.setPosition(pos.getX(), pos.getY(), pos.getZ());
		return fakePlayer;
	}

	private static class FakePlayerEntity extends PlayerEntity {

		public FakePlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
			super(world, pos, yaw, profile);
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
