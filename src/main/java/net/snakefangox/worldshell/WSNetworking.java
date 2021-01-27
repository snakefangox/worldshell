package net.snakefangox.worldshell;

import java.util.HashMap;
import java.util.Map;

import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class WSNetworking {

	public static final Identifier SHELL_DATA = new Identifier(WSUniversal.MODID, "data");

	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(SHELL_DATA, WSNetworking::handleShellData);
	}

	private static void handleShellData(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		Map<BlockPos, BlockState> stateMap = new HashMap<>();
		Map<BlockPos, BlockEntity> entityMap = new HashMap<>();
		WorldShellPacketHelper.readBlocks(buf, stateMap, entityMap);
		client.execute(() -> {
			Entity entity = client.world.getEntityById(entityID);
			if (entity instanceof WorldLinkEntity) {
				((WorldLinkEntity) entity).initializeWorldShell(stateMap, entityMap);
			}
		});
	}

	public static final TrackedDataHandler<Box> BOX = new TrackedDataHandler<Box>() {

		@Override
		public void write(PacketByteBuf data, Box box) {
			data.writeDouble(box.maxX);
			data.writeDouble(box.maxY);
			data.writeDouble(box.maxZ);
			data.writeDouble(box.minX);
			data.writeDouble(box.minY);
			data.writeDouble(box.minZ);
		}

		@Override
		public Box read(PacketByteBuf data) {
			double maxX = data.readDouble();
			double maxY = data.readDouble();
			double maxZ = data.readDouble();
			double minX = data.readDouble();
			double minY = data.readDouble();
			double minZ = data.readDouble();
			return new Box(maxX, maxY, maxZ, minX, minY, minZ);
		}

		@Override
		public Box copy(Box b) {
			return new Box(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
		}
	};

	//TODO Check I still need this
	static {
		TrackedDataHandlerRegistry.register(BOX);
	}

}
