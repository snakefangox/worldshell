package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientEntityManager.class)
public interface ClientEntityManagerAccess {
	@Accessor
	SectionedEntityCache<? extends EntityLike> getCache();

	@Invoker
	void invokeRemoveIfEmpty(long pos, EntityTrackingSection<? extends EntityLike> section);
}
