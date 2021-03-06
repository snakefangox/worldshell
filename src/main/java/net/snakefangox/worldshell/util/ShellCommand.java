package net.snakefangox.worldshell.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.snakefangox.worldshell.entity.WorldLinkEntity;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShellCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("transfertoshell")
				.then(argument("from", BlockPosArgumentType.blockPos()).then(argument("to", BlockPosArgumentType.blockPos())
						.executes((commandCtx) -> execute(commandCtx.getSource(),
								new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandCtx, "from"), BlockPosArgumentType.getLoadedBlockPos(commandCtx, "to")))))));
	}

	private static int execute(ServerCommandSource source, BlockBox box) {
		try {
			List<BlockPos> blockPosList = new ArrayList<>();
			ShellTransferHandler.forEachInBox(box, (bp) -> {
				if (!source.getWorld().isAir(bp)) blockPosList.add(bp.toImmutable());
			});
			if (blockPosList.size() > 0) {
				WorldLinkEntity entity = ShellTransferHandler.transferToShell(source.getWorld(), blockPosList.get(0), blockPosList);
				source.getWorld().spawnEntity(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}
}
