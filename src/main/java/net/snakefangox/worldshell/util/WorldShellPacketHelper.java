package net.snakefangox.worldshell.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.LocalSpace;
import net.snakefangox.worldshell.storage.Microcosm;

import java.util.List;
import java.util.Map;

public class WorldShellPacketHelper {

	public static PacketByteBuf writeBlock(PacketByteBuf buf, World world, BlockPos pos, WorldShellEntity entity, LocalSpace local) {
		buf.writeInt(entity.getId());
		buf.writeLong(local.toLocal(pos).asLong());
		buf.writeInt(Block.getRawIdFromState(world.getBlockState(pos)));
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null) {
			BlockEntityUpdateS2CPacket packet = blockEntity.toUpdatePacket();
			if (packet != null && packet.getNbt() != null) {
				buf.writeNbt(overwritePos(packet.getNbt(), pos));
				return buf;
			}
		}
		buf.writeByte(0);
		return buf;
	}

	private static NbtCompound overwritePos(NbtCompound tag, BlockPos pos) {
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		return tag;
	}

	public static PacketByteBuf writeBlocks(PacketByteBuf buf, Map<BlockState, List<BlockPos>> blockStateListMap, List<BlockEntity> blockEntities, LocalSpace local) {
		buf.writeInt(blockStateListMap.size());
		for (Map.Entry<BlockState, List<BlockPos>> entry : blockStateListMap.entrySet()) {
			buf.writeInt(Block.getRawIdFromState(entry.getKey()));
			buf.writeVarInt(entry.getValue().size());
			for (BlockPos bp : entry.getValue()) buf.writeBlockPos(local.toLocal(bp));
		}
		buf.writeInt(blockEntities.size());
		for (BlockEntity be : blockEntities) {
			BlockPos pos = local.toLocal(be.getPos());
			buf.writeBlockPos(pos);
			BlockEntityUpdateS2CPacket packet = be.toUpdatePacket();
			buf.writeNbt(overwritePos(packet.getNbt(), pos));
		}
		return buf;
	}

	public static PacketByteBuf writeInteract(WorldShellEntity entity, BlockHitResult hitResult, Hand hand, boolean interactType) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(entity.getId());
		buf.writeBlockHitResult(hitResult);
		buf.writeEnumConstant(hand);
		buf.writeBoolean(interactType);
		return buf;
	}

	public static void readBlocks(PacketByteBuf buf, Map<BlockPos, BlockState> posBlockStateMap, Map<BlockPos, BlockEntity> posBlockEntityMap, List<Microcosm.ShellTickInvoker> tickers) {
		int stateCount = buf.readInt();
		for (int i = 0; i < stateCount; ++i) {
			BlockState state = Block.getStateFromRawId(buf.readInt());
			int posCount = buf.readVarInt();
			boolean hasEntity = state.getBlock() instanceof BlockEntityProvider;
			for (int j = 0; j < posCount; ++j) {
				BlockPos pos = buf.readBlockPos();
				posBlockStateMap.put(pos, state);
				if (hasEntity) {
					BlockEntity be = ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state);
					if (be != null) {
						posBlockEntityMap.put(pos, be);
						be.setCachedState(posBlockStateMap.get(pos));
					}
				}
			}
		}
		int beCount = buf.readInt();
		for (int i = 0; i < beCount; ++i) {
			BlockPos bp = buf.readBlockPos();
			NbtCompound nbt = buf.readNbt();
			if (nbt != null && posBlockEntityMap.containsKey(bp)) {
				posBlockEntityMap.get(bp).readNbt(nbt);
			}
		}
	}
}
