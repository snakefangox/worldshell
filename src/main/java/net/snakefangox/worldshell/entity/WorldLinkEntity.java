package net.snakefangox.worldshell.entity;

import java.util.Map;
import java.util.Optional;

import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.storage.WorldShell;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * The basic entity that links to a shell, renders it's contents and handles interaction
 */
public class WorldLinkEntity extends Entity {

	private int shellId = 0;
	private Optional<WorldShell> worldShell;

	public WorldLinkEntity(World world) {
		this(WSUniversal.WORLD_LINK_ENTITY_TYPE, world);
	}

	public WorldLinkEntity(EntityType<?> type, World world) {
		super(type, world);
		worldShell = world.isClient() ? Optional.of(new WorldShell(this)) : Optional.empty();
	}

	public void initializeWorldShell(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap) {
		worldShell.ifPresent(ws -> ws.setWorld(stateMap, entityMap));
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(getId());
		getBay().createClientPacket(world.getServer(), buf);
		ServerPlayNetworking.send(player, WSNetworking.SHELL_DATA, buf);
	}

	@Override
	protected void initDataTracker() {}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		//TODO fix this
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
		if (!world.isClient() && shellId > 0) {
			ShellStorageData.getOrCreate(world.getServer()).getBay(shellId).linkEntity(this);
		}
	}

	public ShellBay getBay() {
		return ShellStorageData.getOrCreate(world.getServer()).getBay(shellId);
	}

	public Optional<WorldShell> getWorldShell() {
		return worldShell;
	}
}
