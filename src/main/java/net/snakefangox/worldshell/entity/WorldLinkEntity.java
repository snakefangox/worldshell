package net.snakefangox.worldshell.entity;

import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.storage.ShellStorageData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

/**
 * The basic entity that links to a shell, renders it's contents and handles interaction
 */
public class WorldLinkEntity extends Entity {

	private int shellId = -1;

	public WorldLinkEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public WorldLinkEntity(World world) {
		super(WSUniversal.WORLD_LINK_ENTITY_TYPE, world);
	}

	@Override
	protected void initDataTracker() {

	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setShellId(tag.getInt("shellId"));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("shellId", shellId);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	public int getShellId() {
		return shellId;
	}

	public void setShellId(int shellId) {
		this.shellId = shellId;
		if(!world.isClient()) ShellStorageData.getOrCreate(world.getServer()).getBay(shellId).linkEntity(this);
	}
}
