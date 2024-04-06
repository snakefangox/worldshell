package net.snakefangox.worldshell.mixin.server;

import com.google.common.collect.ImmutableList;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.snakefangox.worldshell.mixinextras.DynamicWorldGen;
import net.snakefangox.worldshell.mixinextras.GetShellTransferHandler;
import net.snakefangox.worldshell.transfer.ShellTransferHandler;
import net.snakefangox.worldshell.world.CreateWorldsEvent;
import net.snakefangox.worldshell.world.ServerWorldSupplier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask>
		implements CommandOutput, DynamicWorldGen, GetShellTransferHandler {

	@Unique
	private final ShellTransferHandler shellTransferHandler = new ShellTransferHandler();

	@Final
	@Shadow
	protected SaveProperties saveProperties;
	@Final
	@Shadow
	protected LevelStorage.Session session;
	@Final
	@Shadow
	private Executor workerExecutor;
	@Final
	@Shadow
	private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
	@Final
	@Shadow
	private Map<RegistryKey<World>, ServerWorld> worlds;

	public MinecraftServerMixin(String string) {
		super(string);
	}

	@Inject(method = "createWorlds(Lnet/minecraft/server/WorldGenerationProgressListener;)V", at = @At("TAIL"))
	protected void createWorlds(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		CreateWorldsEvent.EVENT.invoker().event((MinecraftServer) (Object) this);
	}

	@Shadow
	public @Nullable abstract ServerWorld getWorld(RegistryKey<World> key);

	@Override
	public ShellTransferHandler worldshell$getShellTransferHandler() {
		return shellTransferHandler;
	}

	@Override
	public ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey,
			DimensionOptions dimensionOptions) {
		return worldshell$createDynamicWorld(worldRegistryKey, dimensionOptions, ServerWorld::new);
	}

	@Override
	public ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey,
			RegistryKey<DimensionType> dimensionTypeKey, ChunkGenerator chunkGenerator) {
		return worldshell$createDynamicWorld(worldRegistryKey,
				new DimensionOptions(((MinecraftServer) (Object) this).getRegistryManager()
						.get(RegistryKeys.DIMENSION_TYPE).getEntry(dimensionTypeKey).get(), chunkGenerator));
	}

	@Override
	public ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey,
			DimensionOptions dimensionOptions, ServerWorldSupplier worldSupplier) {
		boolean isDebug = saveProperties.isDebugWorld();
		long seed = BiomeAccess.hashSeed(saveProperties.getGeneratorOptions().getSeed());
		ServerWorldProperties serverWorldProperties = saveProperties.getMainWorldProperties();
		UnmodifiableLevelProperties unmodifiableLevelProperties = new UnmodifiableLevelProperties(saveProperties,
				serverWorldProperties);
		ServerWorld serverWorld = worldSupplier.create((MinecraftServer) (Object) this, workerExecutor, session,
				unmodifiableLevelProperties, worldRegistryKey, dimensionOptions,
				worldGenerationProgressListenerFactory.create(0), isDebug, seed, ImmutableList.of(), false, null);
		getWorld(World.OVERWORLD).getWorldBorder()
				.addListener(new WorldBorderListener.WorldBorderSyncer(serverWorld.getWorldBorder()));
		worlds.put(worldRegistryKey, serverWorld);
		return serverWorld;
	}
}
