package net.snakefangox.worldshell.storage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

public class EmptyChunkGenerator extends ChunkGenerator {
	public static final VerticalBlockSample EMPTY = new VerticalBlockSample(-1, new BlockState[0]);

    public static final Codec<EmptyChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            ChunkGenerator.createStructureSetRegistryGetter(instance).and(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter((generator) -> generator.biomeSource)
            ).apply(instance, instance.stable(EmptyChunkGenerator::new))
	);

	public EmptyChunkGenerator(Registry<StructureSet> structureSets, BiomeSource biomeSource) {
		super(structureSets, Optional.empty(), biomeSource);
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}
	
	@Override
	public void carve(
			ChunkRegion chunkRegion,
			long seed,
			NoiseConfig noiseConfig,
			BiomeAccess world,
			StructureAccessor structureAccessor,
			Chunk chunk,
			GenerationStep.Carver carverStep) {
		
	}
	
	@Override
	public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
	
	}
	
	
	@Override
	public void populateEntities(ChunkRegion region) {

	}

	@Override
	public int getWorldHeight() {
		return 0;
	}
	
	@Override
	public CompletableFuture<Chunk> populateNoise(
			Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
		return CompletableFuture.completedFuture(chunk);
	}
	
	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinimumY() {
		return 0;
	}
	
	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
		return 0;
	}
	
	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		return EMPTY;
	}
	
	@Override
	public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
	
	}
}
