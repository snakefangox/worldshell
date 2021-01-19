package net.snakefangox.worldshell.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.storage.ShellStorageData;

@Environment(EnvType.CLIENT)
public class WSClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(WSUniversal.WORLD_LINK_ENTITY_TYPE, WorldLinkRenderer::new);
	}
}
