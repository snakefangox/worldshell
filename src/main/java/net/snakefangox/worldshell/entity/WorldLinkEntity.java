package net.snakefangox.worldshell.entity;

import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartEntity;
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox;
import io.github.stuff_stuffs.multipart_entities.common.util.OrientedBox;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.storage.ShellBay;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.storage.WorldShell;
import net.snakefangox.worldshell.util.CoordUtil;
import net.snakefangox.worldshell.util.WSNbtHelper;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;

import java.util.*;

/**
 * The basic entity that links to a shell, renders it's contents and handles interaction
 */
public class WorldLinkEntity extends Entity implements MultipartEntity {

	private static final TrackedData<EntityDimensions> DIMENSIONS = DataTracker.registerData(WorldLinkEntity.class, WSNetworking.DIMENSIONS);
	private static final TrackedData<Vec3d> BLOCK_OFFSET = DataTracker.registerData(WorldLinkEntity.class, WSNetworking.VEC3D);
	private static final TrackedData<EulerAngle> ROTATION_VELOCITY = DataTracker.registerData(WorldLinkEntity.class, TrackedDataHandlerRegistry.ROTATION);

	private int shellId = 0;
	private final WorldShell worldShell = new WorldShell(this, 120 /*TODO set to builder*/);
	private CompoundOrientedBox collisionBox = new CompoundOrientedBox(super.getBoundingBox(), Collections.emptyList());
	private final ShellCollisionHull hull = new ShellCollisionHull(this);

	public WorldLinkEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public void initializeWorldShell(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap, List<WorldShell.ShellTickInvoker<?>> tickers) {
		worldShell.setWorld(stateMap, entityMap, tickers);
		setCollisionBox(stateMap);
	}

	public void setCollisionBox(Map<BlockPos, BlockState> stateMap) {
		List<OrientedBox> boxes = new ArrayList<>();
		double xOff = getPos().x + getBlockOffset().x;
		double yOff = getPos().y + getBlockOffset().y;
		double zOff = getPos().z + getBlockOffset().z;
		for (Map.Entry<BlockPos, BlockState> entry : stateMap.entrySet()) {
			BlockPos bp = entry.getKey();
			List<Box> lBoxes = CoordUtil.getTransformedBoxesFromVoxelShape(entry.getValue()
					.getCollisionShape(worldShell, entry.getKey(), ShapeContext.absent()), xOff + bp.getX(), yOff + bp.getY(), zOff + bp.getZ());
			lBoxes.forEach(b -> boxes.add(new OrientedBox(b)));
		}
		collisionBox = new CompoundOrientedBox(super.getBoundingBox().expand(dimensions.width, dimensions.height, dimensions.width), boxes);
	}

	public void updateWorldShell(BlockPos pos, BlockState state, CompoundTag tag) {
		worldShell.setBlock(pos, state, tag, getEntityWorld());
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		Optional<ShellBay> bay = getBay();
		if (bay.isPresent()) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getId());
			bay.get().createClientPacket(world.getServer(), buf);
			ServerPlayNetworking.send(player, WSNetworking.SHELL_DATA, buf);
		}
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(DIMENSIONS, getType().getDimensions());
		getDataTracker().startTracking(BLOCK_OFFSET, new Vec3d(0, 0, 0));
		getDataTracker().startTracking(ROTATION_VELOCITY, new EulerAngle(0, 0, 0));
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (DIMENSIONS.equals(data)) {
			dimensions = getDataTracker().get(DIMENSIONS);
		}
	}

	@Override
	public CompoundOrientedBox getBoundingBox() {
		return collisionBox;
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		return getDataTracker().get(DIMENSIONS);
	}

	public void setDimensions(EntityDimensions entityDimensions) {
		getDataTracker().set(DIMENSIONS, entityDimensions);
	}

	public Vec3d getBlockOffset() {
		return getDataTracker().get(BLOCK_OFFSET);
	}

	public void setBlockOffset(Vec3d offset) {
		getDataTracker().set(BLOCK_OFFSET, offset);
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (world.isClient()) {
			return handleInteraction(player, hand, false);
		}
		return super.interact(player, hand);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		if (world.isClient() && attacker instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) attacker;
			handleInteraction(player, Hand.MAIN_HAND, true);
		}
		return super.handleAttack(attacker);
	}

	public ActionResult handleInteraction(PlayerEntity player, Hand hand, boolean attack) {
		BlockHitResult rayCastResult = raycastToWorldShell(player);
		if (rayCastResult.getType() == HitResult.Type.BLOCK) {
			if (attack) {
				ClientPlayNetworking.send(WSNetworking.SHELL_INTERACT, WorldShellPacketHelper.writeInteract(this, rayCastResult, hand, true));
				return worldShell.getBlockState(rayCastResult.getBlockPos()).onUse(world, player, hand, rayCastResult);
			} else {
				ClientPlayNetworking.send(WSNetworking.SHELL_INTERACT, WorldShellPacketHelper.writeInteract(this, rayCastResult, hand, false));
				worldShell.getBlockState(rayCastResult.getBlockPos()).onBlockBreakStart(world, rayCastResult.getBlockPos(), player);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}

	public BlockHitResult raycastToWorldShell(PlayerEntity player) {
		Vec3d cameraPosVec = player.getCameraPosVec(1.0F);
		Vec3d rotationVec = player.getRotationVec(1.0F);
		Vec3d extendedVec = CoordUtil.worldToLinkEntityRotated(this,
				cameraPosVec.add(rotationVec.x * 4.5F, rotationVec.y * 4.5F, rotationVec.z * 4.5F));
		RaycastContext rayCtx = new RaycastContext(CoordUtil.worldToLinkEntityRotated(this, cameraPosVec),
				extendedVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
		return worldShell.raycast(rayCtx);
	}

	public void passthroughExplosion(double x, double y, double z, float power, boolean fire, Explosion.DestructionType type) {
		if (world.isClient()) return;
		Optional<ShellBay> bay = getBay();
		if (bay.isPresent()) {
			Vec3d newExp = CoordUtil.toGlobal(bay.get().getCenter(), CoordUtil.worldToLinkEntityRotated(this, new Vec3d(x, y, z)));
			WSUniversal.getStorageDim(world.getServer()).createExplosion(null, newExp.x, newExp.y, newExp.z, power, fire, type);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isClient) {
			worldShell.tick();
		}
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
		setShellId(tag.getInt("shellId"));
		setBlockOffset(WSNbtHelper.getVec3d(tag, "blockOffset"));
		float width = tag.getFloat("width");
		float height = tag.getFloat("height");
		setDimensions(new EntityDimensions(width, height, false));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("shellId", shellId);
		WSNbtHelper.putVec3d(tag, getBlockOffset(), "blockOffset");
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

	public Optional<ShellBay> getBay() {
		return Optional.ofNullable(ShellStorageData.getOrCreate(world.getServer()).getBay(shellId));
	}

	public WorldShell getWorldShell() {
		return worldShell;
	}
}
