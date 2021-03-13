package net.snakefangox.worldshell.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.entity.WorldShellEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Worldshell works with a lot of different coordinate spaces and needs to convert between them,
 * This class contains a lot of static helper methods for doing so. <p>
 * Sorry for the repetition, Vec3i and Vec3d share no common child so I'm stuck with this huge mess.
 */
//TODO This needs to die
public class CoordUtil {
	public static BlockPos toLocal(BlockPos center, BlockPos pos) {
		return pos.subtract(center);
	}
}
