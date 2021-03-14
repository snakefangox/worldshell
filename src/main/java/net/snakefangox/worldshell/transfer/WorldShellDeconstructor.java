package net.snakefangox.worldshell.transfer;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShell;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.ShellStorageData;

public class WorldShellDeconstructor implements ShellTransferOperator {

	private final ServerWorld world;
	private final int shellId;
	private final RotationSolver rotationSolver;
	private final ConflictSolver conflictSolver;

	private int timeSpent = 0;
	private Stage stage = Stage.SETUP;

	private ShellStorageData shellStorage;
	private Bay bay;
	private World shellWorld;

	public static WorldShellDeconstructor create(ServerWorld world, int shellId, RotationSolver rotationSolver, ConflictSolver conflictSolver) {
		return new WorldShellDeconstructor(world, shellId, rotationSolver, conflictSolver);
	}

	private WorldShellDeconstructor(ServerWorld world, int shellId, RotationSolver rotationSolver, ConflictSolver conflictSolver) {
		this.world = world;
		this.shellId = shellId;
		this.rotationSolver = rotationSolver;
		this.conflictSolver = conflictSolver;
	}

	@Override
	public int getTime() {
		return timeSpent;
	}

	@Override
	public void addTime(long amount) {
		timeSpent += amount;
	}

	@Override
	public ServerWorld getWorld() {
		return world;
	}

	@Override
	public boolean isFinished() {
		return stage == Stage.FINISHED;
	}

	@Override
	public void performPass() {
		switch (stage) {
			case SETUP:
				setup();
				break;
			case PLACE:
				break;
			case REMOVE:
				break;
			case FINISHED:
				break;
		}
	}

	private void setup() {
		shellStorage = ShellStorageData.getOrCreate(world);
		bay = shellStorage.getBay(shellId);
		shellWorld = WorldShell.getStorageDim(world.getServer());
	}

	private enum Stage {
		SETUP, PLACE, REMOVE, FINISHED
	}
}
