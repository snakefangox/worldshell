package net.snakefangox.worldshell.transfer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.snakefangox.worldshell.mixinextras.GetShellTransferHandler;

import java.util.PriorityQueue;
import java.util.Queue;

public class ShellTransferHandler {

	private static final int IDEAL_MILLIS = 50;
	private static final int MAX_MILLIS = 200;
	private static final double TICK_PERCENT = 1.5;

	private final Queue<ShellTransferOperator> queue = new PriorityQueue<>();
	private long tickTime = 0;
	private long avgTickTime = IDEAL_MILLIS;

	private void onStartTick() {
		tickTime = System.currentTimeMillis();
	}

	private void onEndTick() {
		tickTime = Util.getMeasuringTimeMs() - tickTime;
		avgTickTime = (avgTickTime + tickTime) / 2;
		long maxTaskTime = (long) (avgTickTime * TICK_PERCENT);
		while (tickTime < MAX_MILLIS && tickTime < maxTaskTime) {
			if (process()) break;
			tickTime += System.currentTimeMillis() - tickTime;
		}
	}

	private boolean process() {
		if (queue.isEmpty()) return true;
		ShellTransferOperator operator = queue.poll();
		operator.performPass();
		if (operator.isFinished()) {
			queue.remove(operator);
		}else {
			operator.addTime(System.currentTimeMillis() - tickTime);
		}
		return false;
	}

	private void onStopping() {
		while (true) {
			if (process()) break;
		}
	}

	private void addOperator(ShellTransferOperator operator) {
		queue.add(operator);
	}

	public static void queueOperator(ShellTransferOperator operator) {
		((GetShellTransferHandler) operator.getWorld().getServer()).worldshell$getShellTransferHandler().addOperator(operator);
	}

	public static void serverStartTick(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onStartTick();
	}

	public static void serverEndTick(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onEndTick();
	}

	public static void serverStopping(MinecraftServer server) {
		((GetShellTransferHandler) server).worldshell$getShellTransferHandler().onStopping();
	}
}
