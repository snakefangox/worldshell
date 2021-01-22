package net.snakefangox.worldshell;

import java.util.Random;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;

public interface DynamicDimGen {

	/**
	 *
	 * @param dimensionOptionsRegistryKey
	 * @param rand
	 * @return
	 */
	ServerWorld createDynamicDim(RegistryKey<DimensionOptions> dimensionOptionsRegistryKey, Random rand);
}
