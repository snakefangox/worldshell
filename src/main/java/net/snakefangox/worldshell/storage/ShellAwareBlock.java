package net.snakefangox.worldshell.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;

/**
 * Should be implemented by any block you want to receive worldshell related events and notifications.
 * Simply implement any of the default methods here and they will be called when appropriate by the
 * Worldshell or it's constructor.
 */
public interface ShellAwareBlock {
	default void onUseInShell(World world, WorldShellEntity entity, PlayerEntity player, Hand hand, BlockHitResult hit){}
}
