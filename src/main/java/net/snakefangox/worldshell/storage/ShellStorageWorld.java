package net.snakefangox.worldshell.storage;

import java.util.List;
import java.util.concurrent.Executor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

public class ShellStorageWorld extends ServerWorld {

	public ShellStorageWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<Spawner> spawners, boolean shouldTickTime) {
		super(server, workerExecutor, session, properties, registryKey, dimensionType, worldGenerationProgressListener, chunkGenerator, debugWorld, l, spawners, shouldTickTime);
	}

}
