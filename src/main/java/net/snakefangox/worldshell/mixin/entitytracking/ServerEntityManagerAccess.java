package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerEntityManager.class)
public interface ServerEntityManagerAccess {
	@Accessor
	SectionedEntityCache<? extends EntityLike> getCache();

	@Invoker
	void invokeEntityLeftSection(long sectionPos, EntityTrackingSection<? extends EntityLike> section);
}
