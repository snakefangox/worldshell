package net.snakefangox.socrates_skyships;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.snakefangox.socrates_skyships.blocks.ShipsHelm;
import net.snakefangox.socrates_skyships.entities.AirShip;
import net.snakefangox.worldshell.entity.WorldShellEntityType;
import net.snakefangox.worldshell.entity.WorldShellSettings;
import net.snakefangox.worldshell.transfer.ConflictSolver;

public class SRegister {
	public static final ShipsHelm SHIPS_HELM = register("ships_helm", new ShipsHelm());
	public static final WorldShellSettings AIRSHIP_SETTINGS = new WorldShellSettings.Builder(true, true)
			.setConflictSolver(ConflictSolver.HARDNESS)
			.setPassThroughInteract(false)
			.setPassThroughAttack(false)
			.build();
	public static final EntityType<AirShip> AIRSHIP_TYPE = register("airship_type", new WorldShellEntityType<>(AirShip::new));

	private static <T extends Block> T register(String path, T block) {
		Registry.register(Registry.BLOCK, new Identifier(SocratesSkyships.MODID, path), block);
		Registry.register(Registry.ITEM, new Identifier(SocratesSkyships.MODID, path), new BlockItem(block, new FabricItemSettings().group(ItemGroup.TRANSPORTATION)));
		return block;
	}

	private static <T extends EntityType<?>> T register(String path, T type) {
		return Registry.register(Registry.ENTITY_TYPE, new Identifier(SocratesSkyships.MODID, path), type);
	}

	public static void register() {
	}
}
