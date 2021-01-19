package net.snakefangox.worldshell.mixin;

import net.minecraft.class_5577;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {

    @Invoker("getEntityIdMap")
    public class_5577<Entity> getEntityIdMap();

}
