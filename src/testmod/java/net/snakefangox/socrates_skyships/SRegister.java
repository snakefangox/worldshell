package net.snakefangox.socrates_skyships;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.snakefangox.socrates_skyships.blocks.ShipsHelm;
import net.snakefangox.socrates_skyships.entities.AirShip;
import net.snakefangox.worldshell.entity.WorldShellEntityType;
import net.snakefangox.worldshell.entity.WorldShellSettings;
import net.snakefangox.worldshell.transfer.ConflictSolver;

public class SRegister {
	public static final ShipsHelm SHIPS_HELM = register("ships_helm", new ShipsHelm());
	public static final WorldShellSettings AIRSHIP_SETTINGS = new WorldShellSettings.Builder()
			.setConflictSolver(ConflictSolver.HARDNESS)
			.build();
	public static final EntityType<AirShip> AIRSHIP_TYPE = register("airship_type",
			new WorldShellEntityType<>(AirShip::new));

	private static <T extends Block> T register(String path, T block) {
		Registry.register(Registries.BLOCK, new Identifier(SocratesSkyships.MODID, path), block);
		Registry.register(Registries.ITEM, new Identifier(SocratesSkyships.MODID, path),
				new BlockItem(block, new FabricItemSettings()));
		return block;
	}

	private static <T extends EntityType<?>> T register(String path, T type) {
		return Registry.register(Registries.ENTITY_TYPE, new Identifier(SocratesSkyships.MODID, path), type);
	}

	public static void register() {
	}
}
