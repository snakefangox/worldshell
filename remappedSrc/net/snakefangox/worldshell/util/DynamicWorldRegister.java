package net.snakefangox.worldshell.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.snakefangox.worldshell.mixinextras.DynamicWorldGen;

/**
 * Static helper class to easily call world register functions on a server.
 */
public class DynamicWorldRegister {
	/**
	 * Convenience method to use a registered dimension type with {@link #createDynamicDim(RegistryKey, DimensionOptions)}
	 * Check there for more detailed usage instructions
	 *
	 * @param worldRegistryKey the key the world will be registered to
	 * @param dimensionTypeKey the key of the dimensiontype to use
	 * @param chunkGenerator   the chunkgenerator the world should use
	 * @return the world that was created
	 */
	public static ServerWorld createDynamicWorld(MinecraftServer server, RegistryKey<World> worldRegistryKey,
											   RegistryKey<DimensionType> dimensionTypeKey, ChunkGenerator chunkGenerator) {
		return ((DynamicWorldGen)server).createDynamicWorld(worldRegistryKey, dimensionTypeKey, chunkGenerator);
	}

	/**
	 * Registers a dimension while the server is running
	 * Once a dimension has been registered this way you should re-register it everytime the server starts unless it is intended to be temporary.<p>
	 * Some tips:<p>
	 * Biomes used in {@link net.minecraft.world.biome.source.BiomeSource}s should come from {@link net.minecraft.server.MinecraftServer#registryManager}<p>
	 * If you want a void world {@link net.snakefangox.worldshell.storage.EmptyChunkGenerator} is provided<p>
	 *
	 * @param worldRegistryKey the key the world will be registered to
	 * @param dimensionOptions the options used for this dimension
	 * @return the world that was created
	 */
	public static ServerWorld createDynamicWorld(MinecraftServer server, RegistryKey<World> worldRegistryKey, DimensionOptions dimensionOptions) {
		return ((DynamicWorldGen)server).createDynamicWorld(worldRegistryKey, dimensionOptions);
	}

	/**
	 * Registers a dimension while the server is running with a custom world class<p>
	 * This is weird and intended for creating <i>very</i> custom dimensions<p>
	 * Use only if you know what you're doing
	 *
	 * @param worldRegistryKey the key the world will be registered to
	 * @param dimensionOptions the options used for this dimension
	 * @param worldSupplier    a supplier of the world type needed
	 * @return the world that was created
	 */
	public static ServerWorld createDynamicWorld(MinecraftServer server, RegistryKey<World> worldRegistryKey,
											   DimensionOptions dimensionOptions, ServerWorldSupplier worldSupplier) {
		return ((DynamicWorldGen)server).createDynamicWorld(worldRegistryKey, dimensionOptions, worldSupplier);
	}
}
