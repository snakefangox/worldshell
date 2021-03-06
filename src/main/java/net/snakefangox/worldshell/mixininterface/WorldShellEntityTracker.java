package net.snakefangox.worldshell.mixininterface;

import net.snakefangox.worldshell.entity.WorldShellEntity;

public interface WorldShellEntityTracker {
	void addWorldShellEntity(WorldShellEntity entity);
	boolean removeWorldShellEntity(WorldShellEntity entity);
}
