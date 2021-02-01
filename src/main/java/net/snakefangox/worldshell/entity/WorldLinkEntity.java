package net.snakefangox.worldshell.entity;

import java.util.Map;
import java.util.Optional;

import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.data.RelativeVec3d;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.storage.WorldShell;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * The basic entity that links to a shell, renders it's contents and handles interaction
 */
public class WorldLinkEntity extends Entity {

	private static final TrackedData<EntityDimensions> DIMENSIONS = DataTracker.registerData(WorldLinkEntity.class, WSNetworking.DIMENSIONS);
	private static final TrackedData<RelativeVec3d> BLOCK_OFFSET = DataTracker.registerData(WorldLinkEntity.class, WSNetworking.REL_VEC3D);

	private int shellId = 0;
	private final Optional<WorldShell> worldShell;

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

	public void updateWorldShell(BlockPos pos, BlockState state, CompoundTag tag) {
		worldShell.ifPresent(ws -> ws.setBlock(pos, state, tag));
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(getId());
		getBay().createClientPacket(world.getServer(), buf);
		ServerPlayNetworking.send(player, WSNetworking.SHELL_DATA, buf);
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(DIMENSIONS, getType().getDimensions());
		getDataTracker().startTracking(BLOCK_OFFSET, new RelativeVec3d(0, 0, 0));
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (DIMENSIONS.equals(data)) {
			dimensions = getDataTracker().get(DIMENSIONS);
		}
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		return getDataTracker().get(DIMENSIONS);
	}

	public void setDimensions(EntityDimensions entityDimensions) {
		getDataTracker().set(DIMENSIONS, entityDimensions);
	}

	public RelativeVec3d getBlockOffset() {
		return getDataTracker().get(BLOCK_OFFSET);
	}

	public void setBlockOffset(RelativeVec3d offset) {
		getDataTracker().set(BLOCK_OFFSET, offset);
	}

	public BlockPos getBlockFromPos(Vec3d pos) {
		return new BlockPos(pos.subtract(getPos()).subtract(getBlockOffset()));
	}

	public Vec3d getLocalCoord() {
		return getPos().add(getBlockOffset());
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		return super.interact(player, hand);
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setShellId(tag.getInt("shellId"));
		setBlockOffset(RelativeVec3d.fromTag(tag, "blockOffset"));
		float width = tag.getFloat("width");
		float height = tag.getFloat("height");
		setDimensions(new EntityDimensions(width, height, false));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("shellId", shellId);
		getBlockOffset().toTag(tag, "blockOffset");
		tag.putFloat("width", getDimensions(null).width);
		tag.putFloat("height", getDimensions(null).height);
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
