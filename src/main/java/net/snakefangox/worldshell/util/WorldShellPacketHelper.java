package net.snakefangox.worldshell.networking;

import java.util.List;
import java.util.Map;

import net.snakefangox.worldshell.data.RelativeBlockPos;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class WorldShellPacketHelper {

	public static PacketByteBuf writeBlocks(PacketByteBuf buf, Map<BlockState, List<BlockPos>> blockStateListMap, List<BlockEntity> blockEntities, RelativeBlockPos center) {
		buf.writeInt(blockStateListMap.size());
		for (Map.Entry<BlockState, List<BlockPos>> entry : blockStateListMap.entrySet()) {
			buf.writeInt(Block.getRawIdFromState(entry.getKey()));
			buf.writeVarInt(entry.getValue().size());
			for (BlockPos bp : entry.getValue()) buf.writeBlockPos(bp);
		}
		buf.writeInt(blockEntities.size());
		for (BlockEntity be : blockEntities) {
			buf.writeBlockPos(center.toLocal(be.getPos()));
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
				if (hasEntity) posBlockEntityMap.put(pos, ((BlockEntityProvider)state).createBlockEntity(pos, state));
			}
		}
		int beCount = buf.readInt();
		for (int i = 0; i < beCount; ++i)
			posBlockEntityMap.get(buf.readBlockPos()).fromTag(buf.readCompoundTag());
	}
}
