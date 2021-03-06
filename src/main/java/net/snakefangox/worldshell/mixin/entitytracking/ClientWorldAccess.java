package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientWorld.class)
public interface ClientWorldAccess {
	@Accessor
	ClientEntityManager<Entity> getEntityManager();
}
