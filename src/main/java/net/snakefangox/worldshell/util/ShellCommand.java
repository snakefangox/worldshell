package net.snakefangox.worldshell.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.snakefangox.worldshell.WorldShell;
import net.snakefangox.worldshell.transfer.BlockBoxIterator;
import net.snakefangox.worldshell.transfer.WorldShellConstructor;

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
			BlockPos center = new BlockPos(box.minX, box.minY, box.minZ);
			WorldShellConstructor.create(source.getWorld(), WorldShell.WORLD_SHELL_ENTITY_TYPE, center, new BlockBoxIterator(box)).construct();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return 1;
	}
}
