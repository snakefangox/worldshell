package net.snakefangox.worldshell.entity;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.resource.featuretoggle.FeatureFlags;

/**
 * An entity class intended for use with {@link WorldShellEntity}s, containing
 * sensible defaults.
 * Feel free to not use this, but if you make your worldshells summonable I take
 * no responsibility.
 */
public class WorldShellEntityType<T extends WorldShellEntity> extends FabricEntityType<T> {
	public WorldShellEntityType(EntityFactory<T> factory) {
		this(factory, 5, 3, false);
	}

	public WorldShellEntityType(EntityFactory<T> factory, int maxTrackDistance, int trackTickInterval,
			Boolean alwaysUpdateVelocity) {
		super(factory, SpawnGroup.MISC, false, false, true, true, ImmutableSet.of(), EntityDimensions.changing(1, 1),
				maxTrackDistance, trackTickInterval, alwaysUpdateVelocity, FeatureFlags.VANILLA_FEATURES);
	}
}
