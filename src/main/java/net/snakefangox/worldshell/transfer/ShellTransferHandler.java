package net.snakefangox.worldshell.transfer;

import net.minecraft.server.MinecraftServer;
import net.snakefangox.worldshell.WorldShellMain;
import net.snakefangox.worldshell.mixinextras.GetShellTransferHandler;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Handles tasks that would be awkward to do all at once,
 * mostly constructing and deconstructing WorldShellEntities.
 * Without this creating large worldshells could crash servers off the watchdog
 * and trying to create a worldshell from a tick event would be a dice roll.
 */
public class ShellTransferHandler {

	private static final int IDEAL_MILLIS = 50;
	private static final int MAX_MILLIS = 200;
	private static final double TICK_PERCENT = 1.5;
	private static final int MAX_CLEANUP_TIME = 60000;

	private final Queue<ShellTransferOperator> queue = new PriorityQueue<>();
	private long tickTime = 0;
	private long avgTickTime = IDEAL_MILLIS;

	public static void queueOperator(ShellTransferOperator operator) {
		((GetShellTransferHandler) operator.getWorld().getServer()).worldshell$getShellTransferHandler().addOperator(operator);
	}

	private void addOperator(ShellTransferOperator operator) {
		queue.add(operator);
	}

	public static void serverStartTick(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onStartTick();
	}

	private void onStartTick() {
		tickTime = System.currentTimeMillis();
	}

	public static void serverEndTick(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onEndTick();
	}

	private void onEndTick() {
		tickTime = System.currentTimeMillis() - tickTime;
		avgTickTime = (avgTickTime + tickTime) / 2;
		if (queue.isEmpty()) return;
		long maxTaskTime = (long) (avgTickTime * TICK_PERCENT);
		while (tickTime < MAX_MILLIS && tickTime < maxTaskTime) {
			process();
			tickTime += System.currentTimeMillis() - tickTime;
		}
	}

	private void process() {
		ShellTransferOperator operator = queue.peek();
		operator.performPass();
		if (operator.isFinished()) {
			queue.remove(operator);
		} else {
			operator.addTime(System.currentTimeMillis() - tickTime);
		}
	}

	public static void serverStopping(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onStopping();
	}

	private void onStopping() {
		if (queue.isEmpty()) return;

		int cleanupSeconds = (MAX_CLEANUP_TIME / 1000);
		WorldShellMain.LOGGER.warn("Worldshell tasks remain in queue. Delaying shutdown for at most " + cleanupSeconds + " seconds");

		tickTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - tickTime < MAX_CLEANUP_TIME && !queue.isEmpty()) {
			process();
		}
		if (System.currentTimeMillis() - tickTime >= MAX_CLEANUP_TIME)
			WorldShellMain.LOGGER.error("Clean up not finished in " + cleanupSeconds + " seconds. Data may be lost");
	}
}
