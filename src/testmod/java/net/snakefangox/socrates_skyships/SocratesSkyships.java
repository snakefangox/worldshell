package net.snakefangox.socrates_skyships;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;

public class SocratesSkyships implements ModInitializer {
	public static final String MODID = "socrates_skyships";

	@Override
	public void onInitialize() {
		SRegister.register();
		BlockRenderLayerMap.INSTANCE.putBlock(SRegister.SHIPS_HELM, RenderLayer.getCutout());
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((e) -> e.add(SRegister.SHIPS_HELM));
	}
}
