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

	public static final RegistryKey<World> STORAGE_DIM = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MODID, "shell_storage"));

	public static final RegistryKey<DimensionType> STORAGE_DIM_TYPE = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MODID, "shell_storage_type"));

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
		LOGGER.info("Moving blocks and fudging collision!");
		LOGGER.info("(Worldshell has started successfully)");
	}

	public void registerStorageDim() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MODID, "empty"), EmptyChunkGenerator.CODEC);
	}

	public void registerShellStorageDimension(MinecraftServer server) {
		ChunkGenerator chunkGenerator = new EmptyChunkGenerator(server.getRegistryManager().get(Registry.STRUCTURE_SET_KEY), new FixedBiomeSource(server.getRegistryManager()
				.get(Registry.BIOME_KEY).getOrCreateEntry(BiomeKeys.THE_VOID)));
		DimensionOptions options = new DimensionOptions(
				server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getOrCreateEntry(STORAGE_DIM_TYPE),
				chunkGenerator
		);
		ShellStorageWorld world = (ShellStorageWorld) DynamicWorldRegister.createDynamicWorld(server, STORAGE_DIM, options, ShellStorageWorld::new);
		world.setCachedBayData(ShellStorageData.getOrCreate(server));
	}
}
