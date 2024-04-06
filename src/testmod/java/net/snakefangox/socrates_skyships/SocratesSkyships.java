package net.snakefangox.socrates_skyships;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class SocratesSkyships implements ModInitializer {
	public static final String MODID = "socrates_skyships";

	@Override
	public void onInitialize() {
		SRegister.register();
		BlockRenderLayerMap.INSTANCE.putBlock(SRegister.SHIPS_HELM, RenderLayer.getCutout());
	}
}
