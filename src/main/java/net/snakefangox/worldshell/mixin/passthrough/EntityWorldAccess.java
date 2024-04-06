package net.snakefangox.worldshell.mixin.passthrough;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(Entity.class)
public interface EntityWorldAccess {
    @Accessor("world")
    void setWorld(World world);
}
