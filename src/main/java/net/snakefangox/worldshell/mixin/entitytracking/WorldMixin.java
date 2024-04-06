package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.snakefangox.worldshell.mixinextras.SingleMatchPredicateProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

	@ModifyVariable(method = "getOtherEntities", at = @At("HEAD"), argsOnly = true)
	public Predicate<? super Entity> getOtherEntities(Predicate<? super Entity> predicate) {
		return new SingleMatchPredicateProxy<>(predicate);
	}

	@ModifyVariable(method = "getEntitiesByType", at = @At("HEAD"), argsOnly = true)
	public <T extends Entity> Predicate<? super T> getEntitiesByType(Predicate<? super T> predicate) {
		return new SingleMatchPredicateProxy<>(predicate);
	}
}
