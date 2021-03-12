package net.snakefangox.worldshell.transfer;

import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.LocalSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Handles the entire creation process for your {@link net.snakefangox.worldshell.entity.WorldShellEntity}
 * including moving blocks to the correct places, registering a bay if needed and spawning the entity.
 * Simply create a new one with  the needed data (including a pre-spawn callback if you want) and then call
 * {@link WorldShellConstructor#construct()} on it. This process can take several ticks to finish for large entities.
 * Should you need to do something after the entity is spawned you can provide a callback or hold onto the result
 * and check to see if it is finished.
 * <p>
 * You can use any BlockPos iterator
 * but you should consider making your own if you have complex rules for your WorldShellEntities.
 * Scanning the world, constructing a List and then passing it's iterator can require more memory and allocations than
 * scanning with the iterator and Mutable BlockPos directly especially with large WorldShellEntities.
 * The abstract {@link WorldIterator} and a few implementations of it are provided but
 * you can use any iterator.
 * <p>
 * <i>Wait did you say Mutable BlockPos?</i> Yes, question asking void.
 * The BlockPos returned from your iterator will <i>always</i> be copied before next is called again
 * so it is most efficient (and completely safe) to return a Mutable BlockPos from it.
 * <p>
 * Also because the Java gods command me so:
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public final class WorldShellConstructor<T extends WorldShellEntity> implements LocalSpace, Comparable<WorldShellConstructor<?>> {
	private final ServerWorld world;
	private final EntityType<T> entityType;
	private final BlockPos center;
	private final Iterator<BlockPos> iterator;
	/** Useful for getting custom data into your entity before it is spawned */
	private final Consumer<T> preSpawnCallback;

	private Result<T> result;
	private int timeSpent = 0;

	public static <U extends WorldShellEntity> WorldShellConstructor<U> create(ServerWorld world, EntityType<U> entityType, BlockPos center, Iterator<BlockPos> iterator) {
		return new WorldShellConstructor<>(world, entityType, center, iterator, null);
	}

	public static <U extends WorldShellEntity> WorldShellConstructor<U> create(ServerWorld world, EntityType<U> entityType, BlockPos center,
																			   Iterator<BlockPos> iterator, Consumer<U> preSpawnCallback) {
		return new WorldShellConstructor<>(world, entityType, center, iterator, preSpawnCallback);
	}

	private WorldShellConstructor(ServerWorld world, EntityType<T> entityType, BlockPos center, Iterator<BlockPos> iterator, Consumer<T> preSpawnCallback) {
		this.world = world;
		this.entityType = entityType;
		this.center = center;
		this.iterator = iterator;
		this.preSpawnCallback = preSpawnCallback;
	}

	/**
	 * Begins constructing the entity
	 *
	 * @param postSpawnCallback is called after the entity is spawned and will always get a finished result
	 * @return a result object that will contain the entity after it is spawned
	 */
	public Result<T> construct(Consumer<Result<T>> postSpawnCallback) {
		result = new Result<>(postSpawnCallback);
		ShellTransferHandler.queueConstructor(this);
		return result;
	}

	/**
	 * Begins constructing the entity
	 *
	 * @return a result object that will contain the entity after it is spawned
	 */
	public Result<T> construct() {
		return construct(null);
	}

	@Override
	public int compareTo(@NotNull WorldShellConstructor<?> o) {
		return timeSpent - o.timeSpent;
	}

	void addTime(int amount) {
		timeSpent += amount;
	}

	ServerWorld getWorld() {
		return world;
	}

	T getEntity(World world) {
		return entityType.create(world);
	}

	BlockPos getCenter() {
		return center;
	}

	Iterator<BlockPos> getIterator() {
		return iterator;
	}

	void preSpawnCallback(T entity) {
		if (preSpawnCallback != null)
			preSpawnCallback.accept(entity);
	}

	@Override
	public double getLocalX() {
		return center.getX();
	}

	@Override
	public double getLocalY() {
		return center.getY();
	}

	@Override
	public double getLocalZ() {
		return center.getZ();
	}

	/** Returned from the construct method, will contain the entity after it is spawned */
	public static class Result<T extends WorldShellEntity> {
		private T result;
		private final Consumer<Result<T>> postSpawnCallback;

		public Result(Consumer<Result<T>> postSpawnCallback) {
			this.postSpawnCallback = postSpawnCallback;
		}

		public boolean isFinished() {
			return result != null;
		}

		@Nullable
		public T get() {
			return result;
		}

		private void complete(T r) {
			result = r;
			if (postSpawnCallback != null)
				postSpawnCallback.accept(this);
		}
	}
}
