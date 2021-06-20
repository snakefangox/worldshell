package net.snakefangox.worldshell.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityChangeListener;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityTrackingDelegate implements EntityChangeListener {

	private final EntityChangeListener proxiedListener;
	private final WorldShellEntity entity;
	private final Set<Long> currentlyTracked = new HashSet<>();
	/** This pos is handled for us by the proxied listener */
	private long noopPos;

	public EntityTrackingDelegate(WorldShellEntity entity, EntityChangeListener proxiedListener) {
		this.entity = entity;
		this.proxiedListener = proxiedListener;
		updateExtendedTracking();
	}

	private void updateExtendedTracking() {
		noopPos = ChunkSectionPos.toLong(entity.getBlockPos());
		Set<Long> newlyTracked = getOccupiedChunkSections();
		Set<Long> old = currentlyTracked.stream().filter(l -> !newlyTracked.contains(l) || l == noopPos).collect(Collectors.toSet());
		old.forEach(l -> {
			if (l != noopPos) SidedEntityManagerHandler.removeWorldShellEntity(entity.world, entity, l);
		});
		newlyTracked.forEach(l -> {
			if (l != noopPos && !currentlyTracked.contains(l))
				SidedEntityManagerHandler.addWorldShellEntity(entity.world, entity, l);
		});
		currentlyTracked.removeAll(old);
		currentlyTracked.addAll(newlyTracked);
	}

	private Set<Long> getOccupiedChunkSections() {
		Set<Long> occupied = new HashSet<>();
		Box box = entity.getBoundingBox();
		int secMaxX = ChunkSectionPos.getSectionCoord(box.maxX);
		int secMaxY = ChunkSectionPos.getSectionCoord(box.maxY);
		int secMaxZ = ChunkSectionPos.getSectionCoord(box.maxZ);
		int secMinX = ChunkSectionPos.getSectionCoord(box.minX);
		int secMinY = ChunkSectionPos.getSectionCoord(box.minY);
		int secMinZ = ChunkSectionPos.getSectionCoord(box.minZ);

		for (int x = secMinX; x <= secMaxX; ++x) {
			for (int y = secMinY; y <= secMaxY; ++y) {
				for (int z = secMinZ; z <= secMaxZ; ++z) {
					long pos = ChunkSectionPos.asLong(x, y, z);
					if (pos != noopPos) occupied.add(pos);
				}
			}
		}
		return occupied;
	}

	@Override
	public void updateEntityPosition() {
		updateExtendedTracking();
		proxiedListener.updateEntityPosition();
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		currentlyTracked.forEach(l -> {
			if (l != noopPos) SidedEntityManagerHandler.removeWorldShellEntity(entity.world, entity, l);
		});
		proxiedListener.remove(reason);
	}
}
