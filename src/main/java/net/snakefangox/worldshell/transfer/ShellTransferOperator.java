package net.snakefangox.worldshell.transfer;

import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

/**
 * Shell operations can take a while. This is a basic interface that the {@link ShellTransferHandler}
 * can use to perform them over time.
 */
public interface ShellTransferOperator extends Comparable<ShellTransferOperator> {

	int getTime();

	void addTime(long amount);

	ServerWorld getWorld();

	boolean isFinished();

	void performPass();

	default int compareTo(@NotNull ShellTransferOperator o) {
		return getTime() - o.getTime();
	}

}
