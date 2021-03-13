package net.snakefangox.worldshell.transfer;

import net.minecraft.server.world.ServerWorld;

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

}
