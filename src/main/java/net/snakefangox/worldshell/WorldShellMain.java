package net.snakefangox.worldshell;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.snakefangox.worldshell.collision.WorldshellCollisionHandler;
import net.snakefangox.worldshell.math.Quaternion;
import net.snakefangox.worldshell.math.Vector3d;
import net.snakefangox.worldshell.storage.EmptyChunkGenerator;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.transfer.ShellTransferHandler;
import net.snakefangox.worldshell.util.DynamicWorldRegister;
import net.snakefangox.worldshell.world.CreateWorldsEvent;
import net.snakefangox.worldshell.world.ShellStorageWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldShellMain implements ModInitializer {

	public static final String MODID = "worldshell";
	public static final Logger LOGGER = LogManager.getLogger();
	private static final String COLLISION_LIB_NAME = "worldshell_collision";

	public static final RegistryKey<World> STORAGE_DIM = RegistryKey.of(Registry.WORLD_KEY,
			new Identifier(MODID, "shell_storage"));

	public static final RegistryKey<DimensionType> STORAGE_DIM_TYPE = RegistryKey.of(Registry.DIMENSION_TYPE_KEY,
			new Identifier(MODID, "shell_storage_type"));

	static {
		System.loadLibrary(COLLISION_LIB_NAME);
	}

	public static ServerWorld getStorageDim(MinecraftServer server) {
		return server.getWorld(WorldShellMain.STORAGE_DIM);
	}

	@Override
	public void onInitialize() {
		registerStorageDim();
		WSNetworking.registerServerPackets();
		CreateWorldsEvent.EVENT.register(this::registerShellStorageDimension);
		ServerLifecycleEvents.SERVER_STARTED.register(ShellTransferHandler::serverStartTick);
		ServerTickEvents.START_SERVER_TICK.register(ShellTransferHandler::serverStartTick);
		ServerTickEvents.END_SERVER_TICK.register(ShellTransferHandler::serverEndTick);
		ServerLifecycleEvents.SERVER_STOPPING.register(ShellTransferHandler::serverStopping);

		WorldshellCollisionHandler collisionHandler = new WorldshellCollisionHandler();
		System.out.println(
				"Should hit: " + collisionHandler.intersects(new Vector3d(), new Quaternion(),
						new Vector3d(0.5, 0.5, 0.5), new Vector3d(0.5, 0.5, 0.5), new Vector3d(0.5, 0.5, 0.5)));
		System.out.println(
				"Shouldn't hit: " + collisionHandler.intersects(new Vector3d(0, 2.0, 0), new Quaternion(),
						new Vector3d(0.5, 0.5, 0.5), new Vector3d(0.5, 0.5, 0.5), new Vector3d(0.5, 0.5, 0.5)));

		LOGGER.info("Moving worlds and fudging collision!");
		LOGGER.info("(Worldshell has loaded successfully)");
	}

	public void registerStorageDim() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MODID, "empty"), EmptyChunkGenerator.CODEC);
	}

	public void registerShellStorageDimension(MinecraftServer server) {
		ChunkGenerator chunkGenerator = new EmptyChunkGenerator(
				server.getRegistryManager().get(Registry.STRUCTURE_SET_KEY),
				new FixedBiomeSource(server.getRegistryManager()
						.get(Registry.BIOME_KEY).getOrCreateEntry(BiomeKeys.THE_VOID)));
		DimensionOptions options = new DimensionOptions(
				server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getOrCreateEntry(STORAGE_DIM_TYPE),
				chunkGenerator);
		ShellStorageWorld world = (ShellStorageWorld) DynamicWorldRegister.createDynamicWorld(server, STORAGE_DIM,
				options, ShellStorageWorld::new);
		world.setCachedBayData(ShellStorageData.getOrCreate(server));
	}
}
