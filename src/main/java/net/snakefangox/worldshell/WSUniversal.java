package net.snakefangox.worldshell;

import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.mixininterface.DynamicDimGen;
import net.snakefangox.worldshell.storage.EmptyChunkGenerator;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.storage.ShellStorageWorld;
import net.snakefangox.worldshell.util.ShellCommand;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

public class WSUniversal implements ModInitializer {

	public static final String MODID = "worldshell";
	public static WSConfig CONFIG;

	public static final RegistryKey<World> STORAGE_DIM = RegistryKey.of(Registry.DIMENSION, new Identifier(MODID, "shell_storage"));
	public static final EntityType<WorldLinkEntity> WORLD_LINK_ENTITY_TYPE = FabricEntityTypeBuilder.<WorldLinkEntity>create(SpawnGroup.MISC, WorldLinkEntity::new)
					.dimensions(EntityDimensions.changing(1, 1)).build();
	public static final Block PLACEHOLDER = new Block(FabricBlockSettings.of(Material.BARRIER).strength(0, 0).nonOpaque().breakInstantly().dropsNothing());

	@Override
	public void onInitialize() {
		AutoConfig.register(WSConfig.class, Toml4jConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(WSConfig.class).getConfig();
		registerStorageDim();
		Registry.register(Registry.ENTITY_TYPE, new Identifier(MODID, "worldlink"), WORLD_LINK_ENTITY_TYPE);
		Registry.register(Registry.BLOCK, new Identifier(MODID, "placeholder"), PLACEHOLDER);
		CommandRegistrationCallback.EVENT.register(this::registerCommands);
		CreateWorldsEvent.EVENT.register(this::registerShellStorageDimension);
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, boolean b) {
		ShellCommand.register(serverCommandSourceCommandDispatcher);
	}

	public void registerStorageDim() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MODID, "empty"), EmptyChunkGenerator.CODEC);
	}

	public void registerShellStorageDimension(MinecraftServer server) {
		Supplier<DimensionType> typeSupplier = () -> server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY)
						.get(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MODID, "empty_type")));
		ChunkGenerator chunkGenerator = new EmptyChunkGenerator(new FixedBiomeSource(server.getRegistryManager()
						.get(Registry.BIOME_KEY).get(BiomeKeys.THE_VOID)));
		DimensionOptions options = new DimensionOptions(typeSupplier, chunkGenerator);
		ShellStorageWorld world = (ShellStorageWorld) ((DynamicDimGen) server).createDynamicDim(STORAGE_DIM, options, ShellStorageWorld::new);
		world.setCachedBayData(ShellStorageData.getOrCreate(server));
	}
}
