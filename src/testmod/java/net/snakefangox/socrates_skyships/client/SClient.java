package net.snakefangox.socrates_skyships.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.worldshell.client.WorldShellEntityRenderer;

public class SClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(SRegister.AIRSHIP_TYPE, WorldShellEntityRenderer::new);
	}
}
