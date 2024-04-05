package net.snakefangox.worldshell.entity;

import net.minecraft.world.explosion.Explosion;
import net.snakefangox.worldshell.transfer.ConflictSolver;
import net.snakefangox.worldshell.transfer.RotationSolver;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Contains the settings for a WorldShell entity, this includes things like
 * whether explosions can affect it,
 * if players can interact with it and most importantly whether it is a simple
 * or complex worldshell entity.
 * <p>
 * Calling the default constructor will give you a standard complex or simple
 * worldshell entity.
 * For custom settings use the builder for sensible defaults and to avoid
 * incompatible settings.
 * <p>
 * There are Javadocs on each setting to explain it.
 * <p>
 * If you want something really custom, subclass this.
 * That's why the getter methods get extra data, it's for u!
 */
public interface WorldShellSettings {

	/**
	 * The number of frames a cached render of this worldshell is valid for.
	 * Cached renders can still move and will be updated but the outside world won't
	 * affect them.
	 * Lighting, Biome colour, etc won't change until the cache is refreshed.
	 */
	int updateFrames();

	/**
	 * If the worldshell should collide with things
	 */
	boolean doCollision(WorldShellEntity entity);

	/**
	 * What to do when the worldshell is destroyed, returning null will deconstruct
	 * the
	 * entity to the world respecting other settings.
	 */
	Consumer<WorldShellEntity> onDestroy(WorldShellEntity entity);

	/**
	 * If player interaction (attacks, right clicks) will be passed through to the
	 * world.
	 * If this returns false the interaction will fail
	 *
	 * @param isInteraction true for right click, false for attack
	 */
	boolean passthroughInteraction(WorldShellEntity entity, boolean isInteraction);

	/**
	 * If an explosion next to the worldshell should affect the blocks making up the
	 * worldshell.
	 */
	boolean passthroughExplosion(WorldShellEntity entity, float power, boolean fire, Explosion.DestructionType type);

	/**
	 * Returns the {@link RotationSolver} the worldshell should use during
	 * deconstruction
	 */
	RotationSolver getRotationSolver(WorldShellEntity entity);

	/**
	 * Returns the {@link ConflictSolver} the worldshell should use during
	 * deconstruction
	 */
	ConflictSolver getConflictSolver(WorldShellEntity entity);

	class Builder {
		private int updateFrames = 120;
		private boolean doCollision = true;
		@Nullable
		private Consumer<WorldShellEntity> onDestroy = null;
		private boolean passThroughInteract = true;
		private boolean passThroughAttack = true;
		private boolean passThroughExplosion = true;
		private RotationSolver rotationSolver = RotationSolver.ORIGINAL;
		private ConflictSolver conflictSolver = ConflictSolver.OVERWRITE;
		private boolean built = false;

		public Builder setUpdateFrames(int updateFrames) {
			check();
			this.updateFrames = updateFrames;
			return this;
		}

		private void check() {
			if (built)
				throw new RuntimeException("Editing completed builder");
		}

		public Builder setDoCollision(boolean doCollision) {
			check();
			this.doCollision = doCollision;
			return this;
		}

		public Builder setOnDestroy(Consumer<WorldShellEntity> onDestroy) {
			check();
			this.onDestroy = onDestroy;
			return this;
		}

		public Builder setPassThroughInteract(boolean passThroughInteract) {
			check();
			this.passThroughInteract = passThroughInteract;
			return this;
		}

		public Builder setPassThroughAttack(boolean passThroughAttack) {
			check();
			this.passThroughAttack = passThroughAttack;
			return this;
		}

		public Builder setPassThroughExplosion(boolean passThroughExplosion) {
			check();
			this.passThroughExplosion = passThroughExplosion;
			return this;
		}

		public Builder setRotationSolver(RotationSolver rotationSolver) {
			check();
			this.rotationSolver = rotationSolver;
			return this;
		}

		public Builder setConflictSolver(ConflictSolver conflictSolver) {
			check();
			this.conflictSolver = conflictSolver;
			return this;
		}

		public WorldShellSettings build() {
			check();
			built = true;
			return new WorldShellSettings() {
				@Override
				public int updateFrames() {
					return updateFrames;
				}

				@Override
				public boolean doCollision(WorldShellEntity entity) {
					return doCollision;
				}

				@Override
				public Consumer<WorldShellEntity> onDestroy(WorldShellEntity entity) {
					return onDestroy;
				}

				@Override
				public boolean passthroughInteraction(WorldShellEntity entity, boolean isInteraction) {
					return (passThroughInteract && isInteraction) || (passThroughAttack && !isInteraction);
				}

				@Override
				public boolean passthroughExplosion(WorldShellEntity entity, float power, boolean fire,
						Explosion.DestructionType type) {
					return passThroughExplosion;
				}

				@Override
				public RotationSolver getRotationSolver(WorldShellEntity entity) {
					return rotationSolver;
				}

				@Override
				public ConflictSolver getConflictSolver(WorldShellEntity entity) {
					return conflictSolver;
				}
			};
		}
	}
}
