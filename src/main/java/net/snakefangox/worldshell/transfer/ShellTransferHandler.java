package net.snakefangox.worldshell.transfer;

import net.minecraft.server.MinecraftServer;
import net.snakefangox.worldshell.mixinextras.GetShellTransferHandler;

import java.util.PriorityQueue;
import java.util.Queue;

public class ShellTransferHandler {

	private static final int IDEAL_MILLIS = 50;

	Queue<WorldShellConstructor<?>> queue = new PriorityQueue<>();
	long tickTime = 0;
	long avgTickTime = IDEAL_MILLIS;

	private void onStartTick() {
		tickTime = System.currentTimeMillis();
	}

	private void onEndTick() {
		long timeSpent = System.currentTimeMillis() - tickTime;
		avgTickTime = (avgTickTime + timeSpent) / 2;

	}

	private void onStopping() {

	}

	private void addConstructor(WorldShellConstructor<?> constructor) {
		queue.add(constructor);
	}

	public static void queueConstructor(WorldShellConstructor<?> constructor) {
		((GetShellTransferHandler) constructor.getWorld().getServer()).worldshell$getShellTransferHandler().addConstructor(constructor);
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
