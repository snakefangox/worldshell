package net.snakefangox.worldshell.collision;

import net.minecraft.util.shape.SimpleVoxelShape;
import net.minecraft.util.shape.VoxelShape;
import net.snakefangox.worldshell.mixin.VoxelShapeMixin;

public class DenseVoxelShape extends SimpleVoxelShape {
	ShellCollisionHull hull;

	public DenseVoxelShape(VoxelShape voxelShape, ShellCollisionHull hull) {
		super(((VoxelShapeMixin) voxelShape).getVoxels());
		this.hull = hull;
	}
}
