package net.snakefangox.worldshell.util;

import java.util.List;
import java.util.Map;

import net.snakefangox.worldshell.entity.WorldLinkEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldShellPacketHelper {

	public static PacketByteBuf writeBlock(PacketByteBuf buf, World world, BlockPos pos, WorldLinkEntity entity, BlockPos center) {
		buf.writeInt(entity.getId());
		buf.writeLong(CoordUtil.toLocal(center, pos).asLong());
		buf.writeInt(Block.getRawIdFromState(world.getBlockState(pos)));
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null) {
			BlockEntityUpdateS2CPacket packet = blockEntity.toUpdatePacket();
			if (packet != null && packet.getCompoundTag() != null) {
				buf.writeCompoundTag(packet.getCompoundTag());
				return buf;
			}
		}
		buf.writeByte(0);
		return buf;
	}

	public static PacketByteBuf writeBlocks(PacketByteBuf buf, Map<BlockState, List<BlockPos>> blockStateListMap, List<BlockEntity> blockEntities, BlockPos center) {
		buf.writeInt(blockStateListMap.size());
		for (Map.Entry<BlockState, List<BlockPos>> entry : blockStateListMap.entrySet()) {
			buf.writeInt(Block.getRawIdFromState(entry.getKey()));
			buf.writeVarInt(entry.getValue().size());
			for (BlockPos bp : entry.getValue()) buf.writeBlockPos(CoordUtil.toLocal(center, bp));
		}
		buf.writeInt(blockEntities.size());
		for (BlockEntity be : blockEntities) {
			buf.writeBlockPos(CoordUtil.toLocal(center, be.getPos()));
			BlockEntityUpdateS2CPacket packet = be.toUpdatePacket();
			buf.writeCompoundTag(packet.getCompoundTag());
		}
		return buf;
	}

	public static void readBlocks(PacketByteBuf buf, Map<BlockPos, BlockState> posBlockStateMap, Map<BlockPos, BlockEntity> posBlockEntityMap) {
		int stateCount = buf.readInt();
		for (int i = 0; i < stateCount; ++i) {
			BlockState state = Block.getStateFromRawId(buf.readInt());
			int posCount = buf.readVarInt();
			boolean hasEntity = state.getBlock() instanceof BlockEntityProvider;
			for (int j = 0; j < posCount; ++j) {
				BlockPos pos = buf.readBlockPos();
				posBlockStateMap.put(pos, state);
				if (hasEntity) {
					posBlockEntityMap.put(pos, ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state));
				}
			}
		}
		int beCount = buf.readInt();
		for (int i = 0; i < beCount; ++i) {
			BlockPos bp = buf.readBlockPos();
			CompoundTag tag = buf.readCompoundTag();
			if (tag != null && posBlockEntityMap.containsKey(bp)) {
				posBlockEntityMap.get(bp).fromTag(tag);
			}
		}
	}
}
