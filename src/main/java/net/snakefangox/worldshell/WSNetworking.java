package net.snakefangox.worldshell;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.util.CoordUtil;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class WSNetworking {

	public static final Identifier SHELL_DATA = new Identifier(WSUniversal.MODID, "data");
	public static final Identifier SHELL_UPDATE = new Identifier(WSUniversal.MODID, "update");
	public static final Identifier SHELL_INTERACT = new Identifier(WSUniversal.MODID, "interact");

	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(SHELL_DATA, WSNetworking::handleShellData);
		ClientPlayNetworking.registerGlobalReceiver(SHELL_UPDATE, WSNetworking::handleShellUpdate);
	}

	public static void registerServerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(SHELL_INTERACT, WSNetworking::handleShellInteract);
	}

	private static void handleShellInteract(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		BlockHitResult hit = buf.readBlockHitResult();
		Hand hand = buf.readEnumConstant(Hand.class);
		boolean interact = buf.readBoolean();
		server.execute(() -> {
			Entity entity = player.world.getEntityById(entityID);
			if (entity instanceof WorldLinkEntity) {
				Optional<ShellBay> bay = ((WorldLinkEntity) entity).getBay();
				if (bay.isPresent()) {
					World world = server.getWorld(WSUniversal.STORAGE_DIM);
					BlockHitResult gHit = new BlockHitResult(CoordUtil.toGlobal(bay.get().getCenter(), hit.getPos()),
									hit.getSide(), CoordUtil.toGlobal(bay.get().getCenter(), hit.getBlockPos()), hit.isInsideBlock());
					if (interact) {
						world.getBlockState(gHit.getBlockPos()).onUse(world, player, hand, gHit);
					} else {
						world.getBlockState(gHit.getBlockPos()).onBlockBreakStart(world, gHit.getBlockPos(), player);
					}
				}
			}
		});
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

	public static final TrackedDataHandler<Vec3d> VEC3D = new TrackedDataHandler<Vec3d>() {

		@Override
		public void write(PacketByteBuf data, Vec3d object) {
			data.writeDouble(object.x);
			data.writeDouble(object.y);
			data.writeDouble(object.z);
		}

		@Override
		public Vec3d read(PacketByteBuf buf) {
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			return new Vec3d(x, y, z);
		}

		@Override
		public Vec3d copy(Vec3d object) {
			return object;
		}
	};

	static {
		TrackedDataHandlerRegistry.register(DIMENSIONS);
		TrackedDataHandlerRegistry.register(VEC3D);
	}

}
