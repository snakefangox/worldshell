package net.snakefangox.worldshell.mixinextras;

import net.snakefangox.worldshell.entity.WorldShellEntity;

public interface WorldShellEntityTracker {
	void worldshell$addWorldShellEntity(WorldShellEntity entity);

	boolean worldshell$removeWorldShellEntity(WorldShellEntity entity);
}
