package net.snakefangox.worldshell.collision;

import net.minecraft.util.shape.VoxelShape;

/**
 * Indicates a box that would really rather handle it's own collision and shape.
 */
public interface SpecialBox {
	VoxelShape toVoxelShape();
}
