package net.snakefangox.worldshell.mixin.entitytracking;

import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.mixininterface.WorldShellEntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin implements WorldShellEntityTracker {

	private final TypeFilterableList<Entity> worldShellEntities = new TypeFilterableList<>(Entity.class);

	@Inject(method = "forEach(Ljava/util/function/Predicate;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
	private void forEach(Predicate<? super EntityLike> predicate, Consumer<? super EntityLike> action, CallbackInfo ci) {
		for (Entity entity : worldShellEntities) {
			if (predicate.test(entity)) action.accept(entity);
		}
	}

	@Inject(method = "forEach(Lnet/minecraft/util/TypeFilter;Ljava/util/function/Predicate;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
	private <T, U extends T> void forEach(TypeFilter<T, U> type, Predicate<? super U> filter, Consumer<? super U> action, CallbackInfo ci) {
		for (T object : worldShellEntities.getAllOfType(type.getBaseClass())) {
			U downCast = type.downcast(object);
			if (downCast != null && filter.test(downCast)) action.accept(downCast);
		}
	}

	@Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
	private void isEmpty(CallbackInfoReturnable<Boolean> cir) {
		if (!worldShellEntities.isEmpty()) cir.setReturnValue(false);
	}

	@Override
	public void addWorldShellEntity(WorldShellEntity entity) {
		worldShellEntities.add(entity);
	}

	@Override
	public boolean removeWorldShellEntity(WorldShellEntity entity) {
		return worldShellEntities.remove(entity);
	}
}
