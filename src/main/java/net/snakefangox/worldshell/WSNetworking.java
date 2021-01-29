package net.snakefangox.worldshell;

import java.util.HashMap;
import java.util.Map;

import net.snakefangox.worldshell.data.RelativeVec3d;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class WSNetworking {

	public static final Identifier SHELL_DATA = new Identifier(WSUniversal.MODID, "data");
	public static final Identifier SHELL_UPDATE = new Identifier(WSUniversal.MODID, "update");

	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(SHELL_DATA, WSNetworking::handleShellData);
		ClientPlayNetworking.registerGlobalReceiver(SHELL_UPDATE, WSNetworking::handleShellUpdate);
	}

	private static void handleShellUpdate(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		BlockPos pos = BlockPos.fromLong(buf.readLong());
		BlockState state = Block.getStateFromRawId(buf.readInt());
		CompoundTag tag = buf.readCompoundTag();
		client.execute(() -> {
			Entity entity = client.world.getEntityById(entityID);
			if (entity instanceof WorldLinkEntity) {
				((WorldLinkEntity) entity).updateWorldShell(pos, state, tag);
			}
		});
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

	public static final TrackedDataHandler<EntityDimensions> DIMENSIONS = new TrackedDataHandler<EntityDimensions>() {

		@Override
		public void write(PacketByteBuf data, EntityDimensions object) {
			data.writeFloat(object.width);
			data.writeFloat(object.height);
			data.writeBoolean(object.fixed);
		}

		@Override
		public EntityDimensions read(PacketByteBuf buf) {
			float w = buf.readFloat();
			float h = buf.readFloat();
			boolean f = buf.readBoolean();
			return new EntityDimensions(w, h, f);
		}

		@Override
		public EntityDimensions copy(EntityDimensions object) {
			return object;
		}
	};

	public static final TrackedDataHandler<RelativeVec3d> REL_VEC3D = new TrackedDataHandler<RelativeVec3d>() {

		@Override
		public void write(PacketByteBuf data, RelativeVec3d object) {
			data.writeDouble(object.x);
			data.writeDouble(object.y);
			data.writeDouble(object.z);
		}

		@Override
		public RelativeVec3d read(PacketByteBuf buf) {
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			return new RelativeVec3d(x, y, z);
		}

		@Override
		public RelativeVec3d copy(RelativeVec3d object) {
			return object;
		}
	};

	static {
		TrackedDataHandlerRegistry.register(DIMENSIONS);
		TrackedDataHandlerRegistry.register(REL_VEC3D);
	}

}
