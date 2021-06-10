package net.snakefangox.worldshell.entity;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;

public class EntityTypeWorldShell<T extends WorldShellEntity> extends FabricEntityType<T> {
	public EntityTypeWorldShell(EntityFactory<T> factory, SpawnGroup spawnGroup, boolean bl, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> spawnBlocks, EntityDimensions entityDimensions, int maxTrackDistance, int trackTickInterval, Boolean alwaysUpdateVelocity) {
		super(factory, spawnGroup, bl, summonable, fireImmune, spawnableFarFromPlayer, spawnBlocks, entityDimensions, maxTrackDistance, trackTickInterval, alwaysUpdateVelocity);
	}
}
