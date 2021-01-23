package net.snakefangox.worldshell;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface DynamicDimGen {

	ServerWorld createDynamicDim(RegistryKey<World> worldRegistryKey, RegistryKey<DimensionType> dimensionTypeKey, ChunkGenerator chunkGenerator);
	ServerWorld createDynamicDim(RegistryKey<World> worldRegistryKey, DimensionOptions dimensionOptions);
}
