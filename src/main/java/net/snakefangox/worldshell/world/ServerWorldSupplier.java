package net.snakefangox.worldshell.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;

import java.util.List;
import java.util.concurrent.Executor;

@FunctionalInterface
public interface ServerWorldSupplier {
	ServerWorld create(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session,
			ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions,
			WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed,
			List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState);
}
