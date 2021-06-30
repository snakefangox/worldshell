package net.snakefangox.worldshell.transfer;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WorldShellMain;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.LocalSpace;
import net.snakefangox.worldshell.storage.ShellStorageData;

public final class WorldShellDeconstructor extends ShellTransferOperator {

	private final int shellId;
	private final RotationSolver rotationSolver;
	private final ConflictSolver conflictSolver;
	private final LocalSpace noRotLocalSpace;
	private final Quaternion rotation;
	private final BlockRotation blockRotation;

	private Stage stage = Stage.SETUP;

	private ShellStorageData shellStorage;
	private Bay bay;
	private World shellWorld;
	private BlockBoxIterator iterator;
	private Runnable postDeconstructCallback;

	private WorldShellDeconstructor(ServerWorld world, int shellId, RotationSolver rotationSolver, ConflictSolver conflictSolver, LocalSpace localSpace) {
		super(world);
		this.shellId = shellId;
		this.rotationSolver = rotationSolver;
		this.conflictSolver = conflictSolver;
		this.noRotLocalSpace = LocalSpace.of(localSpace.getLocalX(), localSpace.getLocalY(), localSpace.getLocalZ());
		this.rotation = localSpace.getInverseRotation();
		this.blockRotation = getBlockRotation(rotation);
	}

	public static WorldShellDeconstructor create(ServerWorld world, int shellId, RotationSolver rotationSolver, ConflictSolver conflictSolver, LocalSpace localSpace) {
		return new WorldShellDeconstructor(world, shellId, rotationSolver, conflictSolver, localSpace);
	}

	public static WorldShellDeconstructor create(WorldShellEntity entity, RotationSolver rotationSolver, ConflictSolver conflictSolver) {
		if (!(entity.world instanceof ServerWorld))
			throw new RuntimeException("Trying to create WorldShellDeconstructor on client");
		return new WorldShellDeconstructor((ServerWorld) entity.world, entity.getShellId(), rotationSolver, conflictSolver,
				LocalSpace.of(entity.getLocalX(), entity.getLocalY(), entity.getLocalZ()));
	}

	/** Begins deconstructing the bay */
	public void deconstruct() {
		deconstruct(null);
	}

	/**
	 * Begins deconstructing the bay
	 *
	 * @param postDeconstructCallback is called after the bay is returned to the world
	 */
	public void deconstruct(Runnable postDeconstructCallback) {
		this.postDeconstructCallback = postDeconstructCallback;
		ShellTransferHandler.queueOperator(this);
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
				place();
				break;
			case REMOVE:
				remove();
				break;
		}
	}

	@Override
	protected LocalSpace getLocalSpace() {
		return bay;
	}

	@Override
	protected LocalSpace getRemoteSpace() {
		return noRotLocalSpace;
	}

	private void setup() {
		shellStorage = ShellStorageData.getOrCreate(getWorld().getServer());
		bay = shellStorage.getBay(shellId);
		shellWorld = WorldShellMain.getStorageDim(getWorld().getServer());
		iterator = BlockBoxIterator.of(bay.getBounds());
		stage = Stage.PLACE;
	}

	private void place() {
		int i = 0;
		while (iterator.hasNext() && i < MAX_OPS) {
			transferBlock(shellWorld, getWorld(), iterator.next(), false, rotationSolver, rotation, blockRotation, conflictSolver);
			++i;
		}
		if (!iterator.hasNext()) stage = Stage.REMOVE;
	}

	private void remove() {
		shellStorage.freeBay(shellId, this);
		if (postDeconstructCallback != null) postDeconstructCallback.run();
		stage = Stage.FINISHED;
	}

	public boolean isRemoving() {
		return stage == Stage.REMOVE;
	}

	private enum Stage {
		SETUP, PLACE, REMOVE, FINISHED
	}
}
