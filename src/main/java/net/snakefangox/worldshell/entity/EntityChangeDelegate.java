package net.snakefangox.worldshell.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class EntityChangeDelegate implements EntityChangeListener {

	private final EntityChangeListener proxiedListener;
	private final WorldLinkEntity entity;
	private final Set<EntityTrackingSection<EntityLike>> currentlyTracked = new HashSet<>();

	public EntityChangeDelegate(WorldLinkEntity entity, EntityChangeListener proxiedListener) {
		this.entity = entity;
		this.proxiedListener = proxiedListener;
	}

	@Override
	public void updateEntityPosition() {
		Box box = entity.getBoundingBox();
		Stream<ChunkSectionPos> stream = ChunkSectionPos.stream((int) box.minX, (int) box.minY, (int) box.minZ,
				(int) Math.ceil(box.maxX), (int) Math.ceil(box.maxY), (int) Math.ceil(box.maxZ));
		
		proxiedListener.updateEntityPosition();
	}

	@Override
	public void remove(Entity.RemovalReason reason) {

		proxiedListener.remove(reason);
	}
}
