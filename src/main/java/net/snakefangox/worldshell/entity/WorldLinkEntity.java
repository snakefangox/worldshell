package net.snakefangox.worldshell.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WSUniversal;

public class WorldLinkEntity extends Entity {

    private int shellId = -1;

    public WorldLinkEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public WorldLinkEntity(World world) { super(WSUniversal.WORLD_LINK_ENTITY_TYPE, world); }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {

    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {

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
    }
}
