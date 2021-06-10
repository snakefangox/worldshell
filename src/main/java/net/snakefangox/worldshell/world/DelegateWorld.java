package net.snakefangox.worldshell.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagManager;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.level.ColorResolver;
import net.snakefangox.worldshell.storage.Microcosm;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.*;
import java.util.stream.Stream;

public class DelegateWorld extends World {

	private final World proxiedWorld;
	private final Microcosm proxiedShell;

	public DelegateWorld(World proxiedWorld, Microcosm proxiedShell) {
		super(null, null, proxiedWorld.getDimension(), null, proxiedWorld.isClient, false, 0);
		this.proxiedWorld = proxiedWorld;
		this.proxiedShell = proxiedShell;
	}

	@Override
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		return proxiedShell.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return proxiedShell.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return proxiedShell.getFluidState(pos);
	}

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return proxiedShell.getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return proxiedShell.getLightingProvider();
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return proxiedShell.getColor(pos, colorResolver);
	}

	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return proxiedShell.getLightLevel(type, pos);
	}

	@Override
	public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
		return proxiedShell.getBaseLightLevel(pos, ambientDarkness);
	}

	@Override
	public boolean isSkyVisible(BlockPos pos) {
		return proxiedShell.isSkyVisible(pos);
	}

	@Override
	public int getHeight() {
		return proxiedShell.getHeight();
	}

	@Override
	public int getBottomY() {
		return proxiedShell.getBottomY();
	}

	@Override
	public int getLuminance(BlockPos pos) {
		return proxiedShell.getLuminance(pos);
	}

	@Override
	public int getMaxLightLevel() {
		return proxiedShell.getMaxLightLevel();
	}

	@Override
	public Stream<BlockState> getStatesInBox(Box box) {
		return proxiedShell.getStatesInBox(box);
	}

	@Override
	public BlockHitResult raycast(BlockStateRaycastContext context) {
		return proxiedShell.raycast(context);
	}

	@Override
	public BlockHitResult raycast(RaycastContext context) {
		return proxiedShell.raycast(context);
	}

	@Override
	@Nullable
	public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
		return proxiedShell.raycastBlock(start, end, pos, shape, state);
	}

	@Override
	public double getDismountHeight(VoxelShape blockCollisionShape, Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
		return proxiedShell.getDismountHeight(blockCollisionShape, belowBlockCollisionShapeGetter);
	}

	@Override
	public double getDismountHeight(BlockPos pos) {
		return proxiedShell.getDismountHeight(pos);
	}

	public static <T, C> T raycast(Vec3d start, Vec3d end, C context, BiFunction<C, BlockPos, T> blockHitFactory, Function<C, T> missFactory) {
		return BlockView.raycast(start, end, context, blockHitFactory, missFactory);
	}

	@Override
	public int getTopY() {
		return proxiedShell.getTopY();
	}

	@Override
	public int countVerticalSections() {
		return proxiedShell.countVerticalSections();
	}

	@Override
	public int getBottomSectionCoord() {
		return proxiedShell.getBottomSectionCoord();
	}

	@Override
	public int getTopSectionCoord() {
		return proxiedShell.getTopSectionCoord();
	}

	@Override
	public boolean isOutOfHeightLimit(BlockPos pos) {
		return proxiedShell.isOutOfHeightLimit(pos);
	}

	@Override
	public boolean isOutOfHeightLimit(int y) {
		return proxiedShell.isOutOfHeightLimit(y);
	}

	@Override
	public int getSectionIndex(int y) {
		return proxiedShell.getSectionIndex(y);
	}

	@Override
	public int sectionCoordToIndex(int coord) {
		return proxiedShell.sectionCoordToIndex(coord);
	}

	@Override
	public int sectionIndexToCoord(int index) {
		return proxiedShell.sectionIndexToCoord(index);
	}

	@Override
	public boolean isClient() {
		return proxiedWorld.isClient();
	}

	@Override
	@Nullable
	public MinecraftServer getServer() {
		return proxiedWorld.getServer();
	}

	@Override
	public boolean isInBuildLimit(BlockPos pos) {
		return proxiedWorld.isInBuildLimit(pos);
	}

	public static boolean isValid(BlockPos pos) {
		return World.isValid(pos);
	}

	@Override
	public WorldChunk getWorldChunk(BlockPos pos) {
		return proxiedWorld.getWorldChunk(pos);
	}

	@Override
	public WorldChunk getChunk(int i, int j) {
		return proxiedWorld.getChunk(i, j);
	}

	@Override
	@Nullable
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
		return proxiedWorld.getChunk(chunkX, chunkZ, leastStatus, create);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
		return proxiedWorld.setBlockState(pos, state, flags);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
		return proxiedWorld.setBlockState(pos, state, flags, maxUpdateDepth);
	}

	@Override
	public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
		proxiedWorld.onBlockChanged(pos, oldBlock, newBlock);
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean move) {
		return proxiedWorld.removeBlock(pos, move);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
		return proxiedWorld.breakBlock(pos, drop, breakingEntity, maxUpdateDepth);
	}

	@Override
	public void addBlockBreakParticles(BlockPos pos, BlockState state) {
		proxiedWorld.addBlockBreakParticles(pos, state);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state) {
		return proxiedWorld.setBlockState(pos, state);
	}

	@Override
	public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		proxiedWorld.updateListeners(pos, oldState, newState, flags);
	}

	@Override
	public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
		proxiedWorld.scheduleBlockRerenderIfNeeded(pos, old, updated);
	}

	@Override
	public void updateNeighborsAlways(BlockPos pos, Block block) {
		proxiedWorld.updateNeighborsAlways(pos, block);
	}

	@Override
	public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
		proxiedWorld.updateNeighborsExcept(pos, sourceBlock, direction);
	}

	@Override
	public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos neighborPos) {
		proxiedWorld.updateNeighbor(pos, sourceBlock, neighborPos);
	}

	@Override
	public int getTopY(Heightmap.Type heightmap, int x, int z) {
		return proxiedWorld.getTopY(heightmap, x, z);
	}

	@Override
	public boolean isDay() {
		return proxiedWorld.isDay();
	}

	@Override
	public boolean isNight() {
		return proxiedWorld.isNight();
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		proxiedWorld.playSound(player, pos, sound, category, volume, pitch);
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		proxiedWorld.playSound(player, x, y, z, sound, category, volume, pitch);
	}

	@Override
	public void playSoundFromEntity(@Nullable PlayerEntity player, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		proxiedWorld.playSoundFromEntity(player, entity, sound, category, volume, pitch);
	}

	@Override
	public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean bl) {
		proxiedWorld.playSound(x, y, z, sound, category, volume, pitch, bl);
	}

	@Override
	public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		proxiedWorld.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		proxiedWorld.addParticle(parameters, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		proxiedWorld.addImportantParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		proxiedWorld.addImportantParticle(parameters, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
	}

	@Override
	public float getSkyAngleRadians(float tickDelta) {
		return proxiedWorld.getSkyAngleRadians(tickDelta);
	}

	@Override
	public void addBlockEntityTicker(BlockEntityTickInvoker ticker) {
		proxiedWorld.addBlockEntityTicker(ticker);
	}

	@Override
	public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) {
		proxiedWorld.tickEntity(tickConsumer, entity);
	}

	@Override
	public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, Explosion.DestructionType destructionType) {
		return proxiedWorld.createExplosion(entity, x, y, z, power, destructionType);
	}

	@Override
	public Explosion createExplosion(@Nullable Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
		return proxiedWorld.createExplosion(entity, x, y, z, power, createFire, destructionType);
	}

	@Override
	public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
		return proxiedWorld.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
	}

	@Override
	public String asString() {
		return proxiedWorld.asString();
	}

	@Override
	public void addBlockEntity(BlockEntity blockEntity) {
		proxiedWorld.addBlockEntity(blockEntity);
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		proxiedWorld.removeBlockEntity(pos);
	}

	@Override
	public boolean canSetBlock(BlockPos pos) {
		return proxiedWorld.canSetBlock(pos);
	}

	@Override
	public boolean isDirectionSolid(BlockPos pos, Entity entity, Direction direction) {
		return proxiedWorld.isDirectionSolid(pos, entity, direction);
	}

	@Override
	public boolean isTopSolid(BlockPos pos, Entity entity) {
		return proxiedWorld.isTopSolid(pos, entity);
	}

	@Override
	public void calculateAmbientDarkness() {
		proxiedWorld.calculateAmbientDarkness();
	}

	@Override
	public void setMobSpawnOptions(boolean spawnMonsters, boolean spawnAnimals) {
		proxiedWorld.setMobSpawnOptions(spawnMonsters, spawnAnimals);
	}

	@Override
	public void close() throws IOException {
		proxiedWorld.close();
	}

	@Override
	@Nullable
	public BlockView getChunkAsView(int chunkX, int chunkZ) {
		return proxiedWorld.getChunkAsView(chunkX, chunkZ);
	}

	@Override
	public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
		return proxiedWorld.getOtherEntities(except, box, predicate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
		return proxiedWorld.getEntitiesByType(filter, box, predicate);
	}

	@Override
	@Nullable
	public Entity getEntityById(int id) {
		return proxiedWorld.getEntityById(id);
	}

	@Override
	public void markDirty(BlockPos pos) {
		proxiedWorld.markDirty(pos);
	}

	@Override
	public int getSeaLevel() {
		return proxiedWorld.getSeaLevel();
	}

	@Override
	public int getReceivedStrongRedstonePower(BlockPos pos) {
		return proxiedWorld.getReceivedStrongRedstonePower(pos);
	}

	@Override
	public boolean isEmittingRedstonePower(BlockPos pos, Direction direction) {
		return proxiedWorld.isEmittingRedstonePower(pos, direction);
	}

	@Override
	public int getEmittedRedstonePower(BlockPos pos, Direction direction) {
		return proxiedWorld.getEmittedRedstonePower(pos, direction);
	}

	@Override
	public boolean isReceivingRedstonePower(BlockPos pos) {
		return proxiedWorld.isReceivingRedstonePower(pos);
	}

	@Override
	public int getReceivedRedstonePower(BlockPos pos) {
		return proxiedWorld.getReceivedRedstonePower(pos);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void disconnect() {
		proxiedWorld.disconnect();
	}

	@Override
	public long getTime() {
		return proxiedWorld.getTime();
	}

	@Override
	public long getTimeOfDay() {
		return proxiedWorld.getTimeOfDay();
	}

	@Override
	public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
		return proxiedWorld.canPlayerModifyAt(player, pos);
	}

	@Override
	public void sendEntityStatus(Entity entity, byte status) {
		proxiedWorld.sendEntityStatus(entity, status);
	}

	@Override
	public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
		proxiedWorld.addSyncedBlockEvent(pos, block, type, data);
	}

	@Override
	public WorldProperties getLevelProperties() {
		return proxiedWorld.getLevelProperties();
	}

	@Override
	public GameRules getGameRules() {
		return proxiedWorld.getGameRules();
	}

	@Override
	public float getThunderGradient(float delta) {
		return proxiedWorld.getThunderGradient(delta);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void setThunderGradient(float thunderGradient) {
		proxiedWorld.setThunderGradient(thunderGradient);
	}

	@Override
	public float getRainGradient(float delta) {
		return proxiedWorld.getRainGradient(delta);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void setRainGradient(float rainGradient) {
		proxiedWorld.setRainGradient(rainGradient);
	}

	@Override
	public boolean isThundering() {
		return proxiedWorld.isThundering();
	}

	@Override
	public boolean isRaining() {
		return proxiedWorld.isRaining();
	}

	@Override
	public boolean hasRain(BlockPos pos) {
		return proxiedWorld.hasRain(pos);
	}

	@Override
	public boolean hasHighHumidity(BlockPos pos) {
		return proxiedWorld.hasHighHumidity(pos);
	}

	@Override
	@Nullable
	public MapState getMapState(String id) {
		return proxiedWorld.getMapState(id);
	}

	@Override
	public void putMapState(String id, MapState state) {
		proxiedWorld.putMapState(id, state);
	}

	@Override
	public int getNextMapId() {
		return proxiedWorld.getNextMapId();
	}

	@Override
	public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
		proxiedWorld.syncGlobalEvent(eventId, pos, data);
	}

	@Override
	public CrashReportSection addDetailsToCrashReport(CrashReport report) {
		return proxiedWorld.addDetailsToCrashReport(report);
	}

	@Override
	public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
		proxiedWorld.setBlockBreakingInfo(entityId, pos, progress);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, @Nullable NbtCompound tag) {
		proxiedWorld.addFireworkParticle(x, y, z, velocityX, velocityY, velocityZ, tag);
	}

	@Override
	public Scoreboard getScoreboard() {
		return proxiedWorld.getScoreboard();
	}

	@Override
	public void updateComparators(BlockPos pos, Block block) {
		proxiedWorld.updateComparators(pos, block);
	}

	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos pos) {
		return proxiedWorld.getLocalDifficulty(pos);
	}

	@Override
	public int getAmbientDarkness() {
		return proxiedWorld.getAmbientDarkness();
	}

	@Override
	public void setLightningTicksLeft(int lightningTicksLeft) {
		proxiedWorld.setLightningTicksLeft(lightningTicksLeft);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return proxiedWorld.getWorldBorder();
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		proxiedWorld.sendPacket(packet);
	}

	@Override
	public DimensionType getDimension() {
		return proxiedWorld.getDimension();
	}

	@Override
	public RegistryKey<World> getRegistryKey() {
		return proxiedWorld.getRegistryKey();
	}

	@Override
	public Random getRandom() {
		return proxiedWorld.getRandom();
	}

	@Override
	public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
		return proxiedWorld.testBlockState(pos, state);
	}

	@Override
	public RecipeManager getRecipeManager() {
		return proxiedWorld.getRecipeManager();
	}

	@Override
	public TagManager getTagManager() {
		return proxiedWorld.getTagManager();
	}

	@Override
	public BlockPos getRandomPosInChunk(int x, int y, int z, int i) {
		return proxiedWorld.getRandomPosInChunk(x, y, z, i);
	}

	@Override
	public boolean isSavingDisabled() {
		return proxiedWorld.isSavingDisabled();
	}

	@Override
	public Profiler getProfiler() {
		return proxiedWorld.getProfiler();
	}

	@Override
	public Supplier<Profiler> getProfilerSupplier() {
		return proxiedWorld.getProfilerSupplier();
	}

	@Override
	public BiomeAccess getBiomeAccess() {
		return proxiedWorld.getBiomeAccess();
	}

	@Override
	protected EntityLookup<Entity> getEntityLookup() {
		return null;
	}

	@Override
	public long getLunarTime() {
		return proxiedWorld.getLunarTime();
	}

	@Override
	public TickScheduler<Block> getBlockTickScheduler() {
		return proxiedWorld.getBlockTickScheduler();
	}

	@Override
	public TickScheduler<Fluid> getFluidTickScheduler() {
		return proxiedWorld.getFluidTickScheduler();
	}

	@Override
	public Difficulty getDifficulty() {
		return proxiedWorld.getDifficulty();
	}

	@Override
	public ChunkManager getChunkManager() {
		return proxiedWorld.getChunkManager();
	}

	@Override
	public boolean isChunkLoaded(int chunkX, int chunkZ) {
		return proxiedWorld.isChunkLoaded(chunkX, chunkZ);
	}

	@Override
	public void updateNeighbors(BlockPos pos, Block block) {
		proxiedWorld.updateNeighbors(pos, block);
	}

	@Override
	public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
		proxiedWorld.syncWorldEvent(player, eventId, pos, data);
	}

	@Override
	public void syncWorldEvent(int eventId, BlockPos pos, int data) {
		proxiedWorld.syncWorldEvent(eventId, pos, data);
	}

	@Override
	public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {
		proxiedWorld.emitGameEvent(entity, event, pos);
	}

	@Override
	public void emitGameEvent(GameEvent event, BlockPos pos) {
		proxiedWorld.emitGameEvent(event, pos);
	}

	@Override
	public void emitGameEvent(GameEvent event, Entity emitter) {
		proxiedWorld.emitGameEvent(event, emitter);
	}

	@Override
	public void emitGameEvent(@Nullable Entity entity, GameEvent event, Entity emitter) {
		proxiedWorld.emitGameEvent(entity, event, emitter);
	}

	@Override
	public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
		return proxiedWorld.getEntityCollisions(entity, box, predicate);
	}

	@Override
	public boolean intersectsEntities(@Nullable Entity except, VoxelShape shape) {
		return proxiedWorld.intersectsEntities(except, shape);
	}

	@Override
	public BlockPos getTopPosition(Heightmap.Type heightmap, BlockPos pos) {
		return proxiedWorld.getTopPosition(heightmap, pos);
	}

	@Override
	public DynamicRegistryManager getRegistryManager() {
		return proxiedWorld.getRegistryManager();
	}

	@Override
	public Optional<RegistryKey<Biome>> getBiomeKey(BlockPos pos) {
		return proxiedWorld.getBiomeKey(pos);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByClass(Class<T> entityClass, Box box, Predicate<? super T> predicate) {
		return proxiedWorld.getEntitiesByClass(entityClass, box, predicate);
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return proxiedWorld.getPlayers();
	}

	@Override
	public List<Entity> getOtherEntities(@Nullable Entity except, Box box) {
		return proxiedWorld.getOtherEntities(except, box);
	}

	@Override
	public <T extends Entity> List<T> getNonSpectatingEntities(Class<T> entityClass, Box box) {
		return proxiedWorld.getNonSpectatingEntities(entityClass, box);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate<Entity> targetPredicate) {
		return proxiedWorld.getClosestPlayer(x, y, z, maxDistance, targetPredicate);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
		return proxiedWorld.getClosestPlayer(entity, maxDistance);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
		return proxiedWorld.getClosestPlayer(x, y, z, maxDistance, ignoreCreative);
	}

	@Override
	public boolean isPlayerInRange(double x, double y, double z, double range) {
		return proxiedWorld.isPlayerInRange(x, y, z, range);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
		return proxiedWorld.getClosestPlayer(targetPredicate, entity);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
		return proxiedWorld.getClosestPlayer(targetPredicate, entity, x, y, z);
	}

	@Override
	@Nullable
	public PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
		return proxiedWorld.getClosestPlayer(targetPredicate, x, y, z);
	}

	@Override
	@Nullable
	public <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
		return proxiedWorld.getClosestEntity(entityClass, targetPredicate, entity, x, y, z, box);
	}

	@Override
	@Nullable
	public <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
		return proxiedWorld.getClosestEntity(entityList, targetPredicate, entity, x, y, z);
	}

	@Override
	public List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
		return proxiedWorld.getPlayers(targetPredicate, entity, box);
	}

	@Override
	public <T extends LivingEntity> List<T> getTargets(Class<T> entityClass, TargetPredicate targetPredicate, LivingEntity targetingEntity, Box box) {
		return proxiedWorld.getTargets(entityClass, targetPredicate, targetingEntity, box);
	}

	@Override
	@Nullable
	public PlayerEntity getPlayerByUuid(UUID uuid) {
		return proxiedWorld.getPlayerByUuid(uuid);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return proxiedWorld.getBiome(pos);
	}

	@Override
	public Stream<BlockState> getStatesInBoxIfLoaded(Box box) {
		return proxiedWorld.getStatesInBoxIfLoaded(box);
	}

	@Override
	public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
		return proxiedWorld.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
	}

	@Override
	public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
		return proxiedWorld.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
	}

	@Override
	public boolean isAir(BlockPos pos) {
		return proxiedWorld.isAir(pos);
	}

	@Override
	public boolean isSkyVisibleAllowingSea(BlockPos pos) {
		return proxiedWorld.isSkyVisibleAllowingSea(pos);
	}

	@Override
	@Deprecated
	public float getBrightness(BlockPos pos) {
		return proxiedWorld.getBrightness(pos);
	}

	@Override
	public int getStrongRedstonePower(BlockPos pos, Direction direction) {
		return proxiedWorld.getStrongRedstonePower(pos, direction);
	}

	@Override
	public Chunk getChunk(BlockPos pos) {
		return proxiedWorld.getChunk(pos);
	}

	@Override
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
		return proxiedWorld.getChunk(chunkX, chunkZ, status);
	}

	@Override
	public boolean isWater(BlockPos pos) {
		return proxiedWorld.isWater(pos);
	}

	@Override
	public boolean containsFluid(Box box) {
		return proxiedWorld.containsFluid(box);
	}

	@Override
	public int getLightLevel(BlockPos pos) {
		return proxiedWorld.getLightLevel(pos);
	}

	@Override
	public int getLightLevel(BlockPos pos, int ambientDarkness) {
		return proxiedWorld.getLightLevel(pos, ambientDarkness);
	}

	@Override
	@Deprecated
	public boolean isPosLoaded(int i, int j) {
		return proxiedWorld.isPosLoaded(i, j);
	}

	@Override
	@Deprecated
	public boolean isChunkLoaded(BlockPos pos) {
		return proxiedWorld.isChunkLoaded(pos);
	}

	@Override
	@Deprecated
	public boolean isRegionLoaded(BlockPos min, BlockPos max) {
		return proxiedWorld.isRegionLoaded(min, max);
	}

	@Override
	@Deprecated
	public boolean isRegionLoaded(int i, int minY, int j, int k, int maxY, int l) {
		return proxiedWorld.isRegionLoaded(i, minY, j, k, maxY, l);
	}

	@Override
	@Deprecated
	public boolean isRegionLoaded(int i, int j, int k, int l) {
		return proxiedWorld.isRegionLoaded(i, j, k, l);
	}

	@Override
	public boolean canPlace(BlockState state, BlockPos pos, ShapeContext context) {
		return proxiedWorld.canPlace(state, pos, context);
	}

	@Override
	public boolean intersectsEntities(Entity entity) {
		return proxiedWorld.intersectsEntities(entity);
	}

	@Override
	public boolean isSpaceEmpty(Box box) {
		return proxiedWorld.isSpaceEmpty(box);
	}

	@Override
	public boolean isSpaceEmpty(Entity entity) {
		return proxiedWorld.isSpaceEmpty(entity);
	}

	@Override
	public boolean isSpaceEmpty(Entity entity, Box box) {
		return proxiedWorld.isSpaceEmpty(entity, box);
	}

	@Override
	public boolean isSpaceEmpty(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
		return proxiedWorld.isSpaceEmpty(entity, box, predicate);
	}

	@Override
	public Stream<VoxelShape> getCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
		return proxiedWorld.getCollisions(entity, box, predicate);
	}

	@Override
	public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, Box box) {
		return proxiedWorld.getBlockCollisions(entity, box);
	}

	@Override
	public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, Box box, BiPredicate<BlockState, BlockPos> biPredicate) {
		return proxiedWorld.getBlockCollisions(entity, box, biPredicate);
	}

	@Override
	public Biome getBiomeForNoiseGen(ChunkPos chunkPos) {
		return proxiedWorld.getBiomeForNoiseGen(chunkPos);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop) {
		return proxiedWorld.breakBlock(pos, drop);
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity) {
		return proxiedWorld.breakBlock(pos, drop, breakingEntity);
	}

	@Override
	public boolean spawnEntity(Entity entity) {
		return proxiedWorld.spawnEntity(entity);
	}

	@Override
	public float getMoonSize() {
		return proxiedWorld.getMoonSize();
	}

	@Override
	public float getSkyAngle(float tickDelta) {
		return proxiedWorld.getSkyAngle(tickDelta);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public int getMoonPhase() {
		return proxiedWorld.getMoonPhase();
	}
}
