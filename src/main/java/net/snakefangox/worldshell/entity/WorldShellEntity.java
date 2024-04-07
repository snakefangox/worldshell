package net.snakefangox.worldshell.entity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.explosion.Explosion;
import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.WorldShellMain;
import net.snakefangox.worldshell.collision.EntityBounds;
import net.snakefangox.worldshell.collision.ShellCollisionHull;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.storage.Bay;
import net.snakefangox.worldshell.storage.LocalSpace;
import net.snakefangox.worldshell.storage.Microcosm;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.transfer.WorldShellDeconstructor;
import net.snakefangox.worldshell.util.WSNbtHelper;
import net.snakefangox.worldshell.util.WorldShellPacketHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The basic entity that links to a shell, renders its contents and handles
 * interaction.
 * This will need to be extended by you and provided with a valid entity
 * constructor (i.e. {@link Entity#Entity(EntityType, World)}).
 */
public abstract class WorldShellEntity extends Entity implements LocalSpace {

	private static final TrackedData<EntityBounds> ENTITY_BOUNDS = DataTracker.registerData(WorldShellEntity.class,
			WSNetworking.BOUNDS);
	private static final TrackedData<Vec3d> BLOCK_OFFSET = DataTracker.registerData(WorldShellEntity.class,
			WSNetworking.VEC3D);
	private static final TrackedData<Quaternion> ROTATION = DataTracker.registerData(WorldShellEntity.class,
			WSNetworking.QUATERNION);

	private final WorldShellSettings settings;
	private final Microcosm microcosm;
	private final ShellCollisionHull hull;

	private int shellId = 0;
	private Quaternion inverseRotation = Quaternion.IDENTITY;

	public WorldShellEntity(EntityType<?> type, World world, WorldShellSettings shellSettings) {
		super(type, world);
		this.settings = shellSettings;
		microcosm = world.isClient() ? new Microcosm(world, this, settings.updateFrames()) : new Microcosm(world, this);
		hull = new ShellCollisionHull(this);
	}

	public void initializeWorldShell(Map<BlockPos, BlockState> stateMap, Map<BlockPos, BlockEntity> entityMap,
			List<Microcosm.ShellTickInvoker> tickers) {
		microcosm.setWorld(stateMap, entityMap, tickers);
		hull.onWorldshellUpdate();
	}

	public void updateWorldShell(BlockPos pos, BlockState state, NbtCompound tag) {
		microcosm.setBlock(pos, state, tag);
		hull.onWorldshellUpdate();
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(ENTITY_BOUNDS, new EntityBounds(1, 1, 1, false));
		getDataTracker().startTracking(BLOCK_OFFSET, new Vec3d(0, 0, 0));
		getDataTracker().startTracking(ROTATION, new Quaternion());
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		if (getWorld().isClient())
			return;
		getBay().ifPresent(b -> b.setLoadForChunks(getServer(), false));
		if (reason.shouldDestroy()) {
			Consumer<WorldShellEntity> onDestroy = settings.onDestroy(this);
			if (onDestroy != null) {
				onDestroy.accept(this);
			} else {
				WorldShellDeconstructor.create(this, settings.getRotationSolver(this), settings.getConflictSolver(this))
						.deconstruct();
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClient) {
			microcosm.tick();
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound tag) {
		setShellId(tag.getInt("shellId"));
		setBlockOffset(WSNbtHelper.getVec3d(tag, "blockOffset"));
		float length = tag.getFloat("length");
		float width = tag.getFloat("width");
		float height = tag.getFloat("height");
		setDimensions(new EntityBounds(length, height, width, false));
		setRotation(WSNbtHelper.getQuaternion(tag, "rotation"));
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound tag) {
		tag.putInt("shellId", shellId);
		WSNbtHelper.putVec3d(tag, getBlockOffset(), "blockOffset");
		tag.putFloat("length", getDimensions().length);
		tag.putFloat("width", getDimensions().width);
		tag.putFloat("height", getDimensions().height);
		WSNbtHelper.putQuaternion(tag, "rotation", getRotation());
	}

	public Vec3d getBlockOffset() {
		return getDataTracker().get(BLOCK_OFFSET);
	}

	public EntityBounds getDimensions() {
		return getDataTracker().get(ENTITY_BOUNDS);
	}

	@Override
	public Quaternion getRotation() {
		return getDataTracker().get(ROTATION);
	}

	@Override
	public Quaternion getInverseRotation() {
		return inverseRotation;
	}

	public void setRotation(Quaternion quaternion) {
		getDataTracker().set(ROTATION, quaternion);
		inverseRotation = quaternion.inverse();
		if (hull != null)
			hull.onWorldshellRotate();
	}

	public void setDimensions(EntityBounds entityBounds) {
		getDataTracker().set(ENTITY_BOUNDS, entityBounds);
	}

	public void setBlockOffset(Vec3d offset) {
		getDataTracker().set(BLOCK_OFFSET, offset);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (getWorld().isClient()) {
			return handleInteraction(player, hand, true);
		}
		return super.interact(player, hand);
	}

	@Override
	public boolean isCollidable() {
		return settings.doCollision(this);
	}

	@Override
	public boolean handleAttack(Entity attacker) {
		if (getWorld().isClient() && attacker instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) attacker;
			handleInteraction(player, Hand.MAIN_HAND, false);
		}
		return super.handleAttack(attacker);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (ENTITY_BOUNDS.equals(data)) {
			dimensions = getDataTracker().get(ENTITY_BOUNDS);
			hull.onWorldshellUpdate();
		} else if (ROTATION.equals(data)) {
			inverseRotation = getDataTracker().get(ROTATION).inverse();
			if (hull != null)
				hull.onWorldshellRotate();
		}
	}

	@Override
	public Box getBoundingBox() {
		return hull.getDelegateBox();
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		Optional<Bay> bay = getBay();
		if (bay.isPresent()) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(getId());
			bay.get().createClientPacket(getWorld().getServer(), buf);
			ServerPlayNetworking.send(player, WSNetworking.SHELL_DATA, buf);
		}
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		return getDataTracker().get(ENTITY_BOUNDS);
	}

	@Override
	public final void setPos(double x, double y, double z) {
		super.setPos(x, y, z);
		if (hull != null)
			hull.onWorldshellUpdate();
	}

	@Override
	public void setChangeListener(EntityChangeListener listener) {
		super.setChangeListener(new EntityTrackingDelegate(this, listener));
	}

	@Override
	public boolean canHit() {
		return true;
	}

	protected ActionResult handleInteraction(PlayerEntity player, Hand hand, boolean interact) {
		if (!settings.passthroughInteraction(this, interact))
			return ActionResult.PASS;
		BlockHitResult rayCastResult = raycastToWorldShell(player);
		if (rayCastResult.getType() == HitResult.Type.BLOCK) {
			if (interact) {
				ClientPlayNetworking.send(WSNetworking.SHELL_INTERACT,
						WorldShellPacketHelper.writeInteract(this, rayCastResult, hand, true));
				return microcosm.getBlockState(rayCastResult.getBlockPos()).onUse(getWorld(), player, hand,
						rayCastResult);
			} else {
				ClientPlayNetworking.send(WSNetworking.SHELL_INTERACT,
						WorldShellPacketHelper.writeInteract(this, rayCastResult, hand, false));
				microcosm.getBlockState(rayCastResult.getBlockPos()).onBlockBreakStart(getWorld(),
						rayCastResult.getBlockPos(), player);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}

	public BlockHitResult raycastToWorldShell(PlayerEntity player) {
		Vec3d cameraPosVec = player.getCameraPosVec(1.0F);
		Vec3d rotationVec = player.getRotationVec(1.0F);
		Vec3d extendedVec = toLocal(cameraPosVec.x + rotationVec.x * 4.5F, cameraPosVec.y + rotationVec.y * 4.5F,
				cameraPosVec.z + rotationVec.z * 4.5F);
		RaycastContext rayCtx = new RaycastContext(toLocal(cameraPosVec),
				extendedVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
		return microcosm.raycast(rayCtx);
	}

	public Optional<Bay> getBay() {
		return Optional.ofNullable(ShellStorageData.getOrCreate(getServer()).getBay(shellId));
	}

	public void passThroughExplosion(double x, double y, double z, float power, boolean fire,
			Explosion.DestructionType type) {
		if (getServer() != null || !settings.passthroughExplosion(this, power, fire, type))
			return;
		getBay().ifPresent(bay -> {
			var storageWorld = WorldShellMain.getStorageDim(getServer());
			Vec3d newExp = globalToGlobal(bay, x, y, z);
			Explosion explosion = new Explosion(storageWorld, null, Explosion.createDamageSource(storageWorld, null),
					null, newExp.x, newExp.y, newExp.z, power, fire, type, ParticleTypes.EXPLOSION,
					ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
			explosion.collectBlocksAndDamageEntities();
			explosion.affectWorld(false);
		});
	}

	public int getShellId() {
		return shellId;
	}

	public void setShellId(int shellId) {
		if (shellId > 0) {
			this.shellId = shellId;
			getBay().ifPresent(bay -> bay.linkEntity(this));
		}
	}

	public Microcosm getMicrocosm() {
		return microcosm;
	}

	@Override
	public double getLocalX() {
		return getX() + getBlockOffset().x;
	}

	@Override
	public double getLocalY() {
		return getY() + getBlockOffset().y;
	}

	@Override
	public double getLocalZ() {
		return getZ() + getBlockOffset().z;
	}
}
