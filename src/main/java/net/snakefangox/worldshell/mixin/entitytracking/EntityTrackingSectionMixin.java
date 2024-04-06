package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.mixinextras.WorldShellEntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin implements WorldShellEntityTracker {

	@Unique
	private final TypeFilterableList<Entity> worldShellEntities = new TypeFilterableList<>(Entity.class);

	@Inject(method = "forEach(Lnet/minecraft/util/math/Box;Lnet/minecraft/util/function/LazyIterationConsumer;)Lnet/minecraft/util/function/LazyIterationConsumer$NextIteration;", at = @At("TAIL"))
	private void forEach(Box box, LazyIterationConsumer<Entity> consumer, CallbackInfoReturnable<LazyIterationConsumer.NextIteration> cir) {
		for (Entity entity : worldShellEntities) {
			if (entity.getBoundingBox().intersects(box))
				consumer.accept(entity);
		}
	}

	@Inject(method = "forEach(Lnet/minecraft/util/TypeFilter;Lnet/minecraft/util/math/Box;Lnet/minecraft/util/function/LazyIterationConsumer;)Lnet/minecraft/util/function/LazyIterationConsumer$NextIteration;", at = @At("TAIL"))
	private <T extends EntityLike, U extends T> void forEach(TypeFilter<T, U> type, Box box, LazyIterationConsumer<? super U> consumer, CallbackInfoReturnable<LazyIterationConsumer.NextIteration> cir) {
		for (T object : worldShellEntities.getAllOfType(type.getBaseClass())) {
			U downCast = type.downcast(object);
			if (downCast != null && !downCast.getBoundingBox().intersects(box))
				consumer.accept(downCast);
		}
	}

	@Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
	private void isEmpty(CallbackInfoReturnable<Boolean> cir) {
		if (!worldShellEntities.isEmpty())
			cir.setReturnValue(false);
	}

	@Override
	public void worldshell$addWorldShellEntity(WorldShellEntity entity) {
		worldShellEntities.add(entity);
	}

	@Override
	public boolean worldshell$removeWorldShellEntity(WorldShellEntity entity) {
		return worldShellEntities.remove(entity);
	}
}
