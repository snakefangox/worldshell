package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Box;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import net.snakefangox.worldshell.world.ProxyWorld;
import org.jetbrains.annotations.Nullable;

public class ShellCollisionSpliterator extends BlockCollisionSpliterator {
	public ShellCollisionSpliterator(ProxyWorld world, Box box) {
		super(world, null, box);
	}

	@Nullable
	@Override
	public BlockView getChunk(int x, int z) {
		return world;
	}
}
